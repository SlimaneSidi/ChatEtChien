import java.util.*;

// Neurones choisis : 3 sigmoides en "un contre tous" (one-vs-all).
// Chaine de traitement complete : lecture des images, normalisation, miroir
// horizontal (augmentation), melange, apprentissage de 3 neurones.
// Classes : 0 = chat, 1 = chien, 2 = wild. La classe predite est l'argmax des 3 sorties.
//
// javac neurone/*.java *.java
// java -cp ".;neurone" ChaineTraitImage

public class ChaineTraitImage
{
    static final boolean NIVEAUX_DE_GRIS = false;
    static final String DIR_TRAIN = "../dataset_groupe_9/train";
    static final String DIR_TEST  = "../dataset_groupe_9/test";
    static final float ETA        = 0.001f;
    static final float MSE_LIMITE = 0.1f;
    static final long  SEED       = 67L;
    static final String DIR_MODELE = "../modeles";     // dossier des neurones sauvegardes
    static final boolean FORCER_ENTRAINEMENT = false;  // true = reapprendre meme si des modeles existent deja

    // Les 3 classes, dans l'ordre des neurones (indice = classe)
    static final String[] CLASSES = {"chat", "chien", "wild"};
    static final int NB_CLASSES   = CLASSES.length;

    // Determine le label binaire d'une image a partir de son chemin (chat = 1, sinon = 0)
    static int labelChat(String chemin) {
        return chemin.contains("/cat/") || chemin.contains("\\cat\\") ? 1 : 0;
    }

    // Determine la classe reelle d'une image a partir de son chemin : 0 = chat, 1 = chien, 2 = wild
    static int classeReelle(String chemin) {
        if (chemin.contains("/cat/") || chemin.contains("\\cat\\")) return 0;
        if (chemin.contains("/dog/") || chemin.contains("\\dog\\")) return 1;
        return 2; // wild
    }

    // Classe predite = indice du neurone qui produit la plus grande sortie (argmax)
    static int predictionClasse(iNeurone[] neurones, float[] entrees) {
        int meilleur = 0;
        float maxSortie = Float.NEGATIVE_INFINITY;
        for (int k = 0; k < neurones.length; ++k) {
            neurones[k].metAJour(entrees);
            float s = neurones[k].sortie();
            if (s > maxSortie) { maxSortie = s; meilleur = k; }
        }
        return meilleur;
    }

    // Chemin du fichier de sauvegarde du neurone d'une classe
    static String fichierModele(int classe) {
        return DIR_MODELE + "/modele_" + CLASSES[classe] + ".txt";
    }

    // Vrai si les 3 fichiers de modele existent deja sur le disque
    static boolean modelesExistent() {
        for (int k = 0; k < NB_CLASSES; ++k)
            if (!new java.io.File(fichierModele(k)).exists()) return false;
        return true;
    }

    // Normalise les pixels de 0 à 255 vers 0 à 1
    static float[] normalise(int[] donnees) {
        float[] f = new float[donnees.length];
        for (int i = 0; i < donnees.length; ++i)
            f[i] = donnees[i] / 255.0f;
        return f;
    }

    // Inverse la colonne j -> largeur-1-j en conservant l'ordre des type
    // type = 1 en niveaux de gris, 3 en RGB
    static float[] miroirHorizontal(float[] src, int largeur, int hauteur, boolean niveauxDeGris) {
        final int type = niveauxDeGris ? 1 : 3;  
        float[] dst = new float[src.length];
        for (int i = 0; i < hauteur; ++i) {
            for (int j = 0; j < largeur; ++j) {
                final int idxSrc = (i * largeur + j) * type;
                final int idxDst = (i * largeur + (largeur-1 - j))  * type;
                for (int c = 0; c < type; ++c)
                    dst[idxDst + c] = src[idxSrc + c];
            }
        }
        return dst;
    }

    public static void main(String[] args)
    {
        Neurone.fixeCoefApprentissage(ETA);
        iNeurone[] neurones = new iNeurone[NB_CLASSES];
        final int tailleEntree;        // nombre d'entrees par neurone (= taille d'une image)
        int totalIterations = 0;       // 0 si on charge un modele (pas de reapprentissage)
        int nbImagesTrain  = 0;        // 0 si on charge un modele

        // On entraine si on le force, ou s'il manque au moins un fichier de modele
        boolean entrainer = FORCER_ENTRAINEMENT || !modelesExistent();

        if (entrainer)
        {
            System.out.println("[1/4] Chargement de la base de donnees...");
            List<String> cheminsTrain = Image.listeFichiers(DIR_TRAIN);
            if (cheminsTrain == null || cheminsTrain.isEmpty()) {
                System.err.println("Aucun fichier trouve dans " + DIR_TRAIN);
                return;
            }

            // Melange des chemins AVANT chargement
            Collections.shuffle(cheminsTrain, new Random(SEED));

            final int N = cheminsTrain.size();
            final int M = 2 * N; // image + son miroir
            nbImagesTrain = M;
            float[][] entreesTrain = new float[M][];
            // Une cible binaire par classe (one-vs-all) : ciblesTrain[k][i] = 1 si l'image i est de la classe k
            float[][] ciblesTrain  = new float[NB_CLASSES][M];
            int[] nbParClasse = new int[NB_CLASSES];
            long t0 = System.currentTimeMillis();
            for (int i = 0; i < N; ++i) {
                int classe = classeReelle(cheminsTrain.get(i));
                Image im = new Image(cheminsTrain.get(i), classe, NIVEAUX_DE_GRIS);
                float[] e = normalise(im.donnees());
                entreesTrain[i]   = e;
                // Image miroir ajoutée à la 2e moitie du tab
                entreesTrain[N+i] = miroirHorizontal(e, im.largeur(), im.hauteur(), NIVEAUX_DE_GRIS);
                // Cible 1 pour le neurone de la bonne classe, 0 pour les autres (image + miroir)
                for (int k = 0; k < NB_CLASSES; ++k) {
                    float cible = (classe == k) ? 1f : 0f;
                    ciblesTrain[k][i]   = cible;
                    ciblesTrain[k][N+i] = cible;
                }
                nbParClasse[classe] += 2;
                if ((i+1) % 1000 == 0)
                    System.out.printf("    %d / %d images chargees%n", i+1, N);
            }

            long dt = System.currentTimeMillis() - t0;
            System.out.printf("    %d images chargees (%d originales + %d miroirs) en %.1f s%n",
                              M, N, N, dt/1000.0);
            for (int k = 0; k < NB_CLASSES; ++k)
                System.out.printf("      %-5s : %d images%n", CLASSES[k], nbParClasse[k]);

            // Creation des 3 neurones sigmoides (un par classe, one-vs-all)
            tailleEntree = entreesTrain[0].length;
            System.out.println("[2/4] Creation des 3 neurones sigmoides");
            System.out.printf("    %d entrees (= %dx%d pixels x3 RGB)%n",
                              tailleEntree, 64, 64);
            for (int k = 0; k < NB_CLASSES; ++k)
                neurones[k] = new NeuroneSigmoide(tailleEntree);

            // Apprentissage : chaque neurone apprend sa classe contre toutes les autres
            System.out.printf("[3/4] Apprentissage (eta=%.4f, MSElimite=%.3f)...%n",
                              ETA, MSE_LIMITE);
            long tA = System.currentTimeMillis();
            for (int k = 0; k < NB_CLASSES; ++k) {
                System.out.printf("    -- Neurone %s --%n", CLASSES[k]);
                totalIterations += neurones[k].apprentissage(entreesTrain, ciblesTrain[k], MSE_LIMITE);
            }
            long dtA = System.currentTimeMillis() - tA;
            System.out.printf("    Apprentissage termine en %.1f s (%d iterations cumulees)%n",
                              dtA/1000.0, totalIterations);

            // Sauvegarde des 3 neurones appris (cree le dossier au besoin)
            new java.io.File(DIR_MODELE).mkdirs();
            for (int k = 0; k < NB_CLASSES; ++k)
                neurones[k].sauvegarde(fichierModele(k));
        }
        else
        {
            // Modeles deja entraines : on les recharge sans reapprendre
            System.out.println("[1/4] Chargement des sauvegardes");
            // La taille d'entree est deduite d'une image quelconque de la base de test
            List<String> echantillon = Image.listeFichiers(DIR_TEST);
            if (echantillon == null || echantillon.isEmpty()) {
                System.err.println("Aucun fichier trouve dans " + DIR_TEST);
                return;
            }
            tailleEntree = new Image(echantillon.get(0), 0, NIVEAUX_DE_GRIS).taille();
            System.out.printf("    %d entrees attendues par neurone%n", tailleEntree);
            for (int k = 0; k < NB_CLASSES; ++k) {
                neurones[k] = new NeuroneSigmoide(tailleEntree);
                neurones[k].chargement(fichierModele(k));
            }
        }

        // Evaluation sur le jeu de test : classe predite = argmax des 3 sorties
        System.out.println("[4/4] Evaluation sur la base de donnée...");
        List<String> cheminsTest = Image.listeFichiers(DIR_TEST);
        int[][] conf = new int[NB_CLASSES][NB_CLASSES]; // conf[reel][predit]
        for (String chemin : cheminsTest) {
            int reel = classeReelle(chemin);
            Image im = new Image(chemin, reel, NIVEAUX_DE_GRIS);
            float[] e = normalise(im.donnees());
            int predit = predictionClasse(neurones, e);
            conf[reel][predit]++;
        }

        // Matrice de confusion 3x3 + accuracy globale
        int total = 0, bonnes = 0;
        for (int r = 0; r < NB_CLASSES; ++r)
            for (int p = 0; p < NB_CLASSES; ++p) {
                total += conf[r][p];
                if (r == p) bonnes += conf[r][p];
            }
        double moyenne = 100.0 * bonnes / total;

        System.out.println("================ RESULTATS ================");
        System.out.printf("Base de test : %d images%n", total);
        System.out.println("Matrice de confusion (lignes = reel, colonnes = predit) :");
        System.out.printf("            %8s %8s %8s%n", CLASSES[0], CLASSES[1], CLASSES[2]);
        for (int r = 0; r < NB_CLASSES; ++r)
            System.out.printf("  %-8s %8d %8d %8d%n",
                CLASSES[r], conf[r][0], conf[r][1], conf[r][2]);

        // Precision / rappel par classe + macro-moyenne (moyenne simple sur les 3 classes)
        double sommePrecision = 0, sommeRappel = 0;
        System.out.println("Par classe :");
        for (int k = 0; k < NB_CLASSES; ++k) {
            int tp = conf[k][k];
            int predits = 0, reels = 0;
            for (int x = 0; x < NB_CLASSES; ++x) { predits += conf[x][k]; reels += conf[k][x]; }
            double prec = predits == 0 ? 0 : 100.0 * tp / predits;
            double rapp = reels   == 0 ? 0 : 100.0 * tp / reels;
            sommePrecision += prec; sommeRappel += rapp;
            System.out.printf("  %-5s  precision=%.2f %%  rappel=%.2f %%%n", CLASSES[k], prec, rapp);
        }
        double precision = sommePrecision / NB_CLASSES; // macro-moyenne
        double rappel    = sommeRappel    / NB_CLASSES;
        System.out.printf("Accuracy globale : %.2f %%%n", moyenne);
        System.out.printf("Macro precision  : %.2f %%   Macro rappel : %.2f %%%n", precision, rappel);
        System.out.println("===========================================");

        // Journalisation des resultats (ajout en fin de Rapport/Results.md, sans ecraser)
        String type = (NIVEAUX_DE_GRIS ? "NiveauDeGris" : "RGB") + " Miroir";
        String neurone = "NeuroneSigmoide x" + NB_CLASSES + " (chat/chien/wild)";
        enregistreResultats(type, neurone, nbImagesTrain, totalIterations, ETA, MSE_LIMITE,
                            moyenne, precision, rappel);


        // Interface UI

        UserInterface.start(cheminsTest, neurones);
    }

    // Ajoute une ligne de resultats a Rapport/Results.md.
    // Cree le fichier (et l'entete du tableau) s'il n'existe pas ou est vide,
    // sinon ajoute simplement une ligne sans effacer le contenu existant.
    // L'id est auto-incremente a partir du plus grand id deja present.
    static void enregistreResultats(String type, String neurone, int nbImagesTrain,
                                    int iterations, float eta, float mseLimite,
                                    double moyenne, double precision, double rappel) {
        final String chemin = "../Rapport/Results.md";
        final String entete =
            "# Resultats des executions\n\n" +
            "| id | date | type | neurone | images_train | iterations | eta | mse_limite | moyenne (%) | precision (%) | rappel (%) |\n" +
            "|----|------|------|---------|--------------|------------|-----|------------|--------------|---------------|------------|\n";
        try {
            java.io.File f = new java.io.File(chemin);
            int prochainId = 1;
            boolean entetePresente = false;
            if (f.exists() && f.length() > 0) {
                for (String ligne : java.nio.file.Files.readAllLines(f.toPath())) {
                    String t = ligne.trim();
                    if (t.startsWith("| id ")) {
                        entetePresente = true;
                    } else if (t.startsWith("|")) {
                        String[] cols = t.split("\\|");
                        if (cols.length > 1) {
                            try {
                                int id = Integer.parseInt(cols[1].trim());
                                if (id >= prochainId) prochainId = id + 1;
                            } catch (NumberFormatException ignore) { /* separateur ou entete */ }
                        }
                    }
                }
            }
            try (java.io.FileWriter w = new java.io.FileWriter(f, true)) { // true = ajout
                if (!entetePresente) w.write(entete);
                String date = java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                w.write(String.format(java.util.Locale.US,
                    "| %d | %s | %s | %s | %d | %d | %.4f | %.3f | %.2f | %.2f | %.2f |%n",
                    prochainId, date, type, neurone, nbImagesTrain, iterations,
                    eta, mseLimite, moyenne, precision, rappel));
            }
            System.out.printf("Resultats ajoutes a %s (id=%d)%n", chemin, prochainId);
        } catch (java.io.IOException e) {
            System.err.println("Impossible d'ecrire les resultats dans " + chemin);
            e.printStackTrace();
        }
    }
}
