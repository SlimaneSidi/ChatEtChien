import java.util.*;

// 3 neurones sigmoides (1 par type)
// Chaine de traitement complete : lecture des images, extraction des
// caracteristiques HOG (Histogramme de Gradients Orientes), miroir horizontal,
// melange, apprentissage de 3 neurones.
// TYPE : 0 = chat, 1 = chien, 2 = wild
//
// javac neurone/*.java *.java
// java -cp ".;neurone" ChaineTraitImage

public class ChaineTraitImage
{
    // HOG travaille sur le gradient : on lit les images en niveaux de gris.
    static final boolean NIVEAUX_DE_GRIS = true;
    static final String DIR_TRAIN = "../dataset_groupe_9/train";
    static final String DIR_TEST  = "../dataset_groupe_9/test";
    static final float ETA        = 0.001f;
    static final float MSE_LIMITE = 0.075f;
    static final long  SEED       = 11L;
    static final String DIR_MODELE = "../modeles";     // dossier des sauvegardes
    static final boolean FORCER_ENTRAINEMENT = true;
    static final String[] TYPE = {"chat", "chien", "wild"};
    static final int NB_TYPE   = TYPE.length;

    // Determine le label binaire d'une image a partir de son chemin (chat = 1, sinon = 0)
    static int labelChat(String chemin) {
        return chemin.contains("/cat/") || chemin.contains("\\cat\\") ? 1 : 0;
    }

    // Determine le type réel d'une image avec son dossier
    static int typeReel(String chemin) {
        if (chemin.contains("/cat/") || chemin.contains("\\cat\\")) return 0;
        if (chemin.contains("/dog/") || chemin.contains("\\dog\\")) return 1;
        return 2; // wild
    }

    // type predit = indice du neurone qui produit la plus grande sortie (argmax)
    static int predictionType(iNeurone[] neurones, float[] entrees) {
        int meilleur = 0;
        float maxSortie = Float.NEGATIVE_INFINITY;
        for (int k = 0; k < neurones.length; ++k) {
            neurones[k].metAJour(entrees);
            float s = neurones[k].sortie();
            if (s > maxSortie) { maxSortie = s; meilleur = k; }
        }
        return meilleur;
    }

    // Chemin du fichier de sauvegarde du neurone d'un type
    static String fichierModele(int type) {
        return DIR_MODELE + "/modele_" + TYPE[type] + ".txt";
    }

    // si existent deja = true
    static boolean modelesExistent() {
        for (int k = 0; k < NB_TYPE; ++k)
            if (!new java.io.File(fichierModele(k)).exists()) return false;
        return true;
    }

    // Caracteristiques HOG d'une image (entrees du neurone). Point d'entree unique
    // partage avec l'UI pour garantir le meme pretraitement a l'apprentissage et au test.
    static float[] caracteristiques(Image im) {
        return Hog.calcule(im.donnees(), im.largeur(), im.hauteur());
    }

    // Miroir horizontal d'une image en niveaux de gris (colonne j -> largeur-1-j).
    // Sert d'augmentation de donnees : on recalcule ensuite le HOG sur l'image inversee.
    static int[] miroirGris(int[] src, int largeur, int hauteur) {
        int[] dst = new int[src.length];
        for (int i = 0; i < hauteur; ++i)
            for (int j = 0; j < largeur; ++j)
                dst[i * largeur + (largeur-1 - j)] = src[i * largeur + j];
        return dst;
    }

    public static void main(String[] args)
    {
        Neurone.fixeCoefApprentissage(ETA);
        iNeurone[] neurones = new iNeurone[NB_TYPE];
        final int tailleEntree;        // taille image
        int totalIterations = 0;       // 0 si on charge un modele
        int nbImagesTrain  = 0;        // pareil

        // forcer l'entrainement
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
            // ciblesTrain[k][i] = 1 si l'image i est du type k
            float[][] ciblesTrain  = new float[NB_TYPE][M];
            int[] nbPartype = new int[NB_TYPE];
            long t0 = System.currentTimeMillis();
            for (int i = 0; i < N; ++i) {
                int type = typeReel(cheminsTrain.get(i));
                Image im = new Image(cheminsTrain.get(i), type, NIVEAUX_DE_GRIS);
                int[] gris = im.donnees();
                entreesTrain[i]   = Hog.calcule(gris, im.largeur(), im.hauteur());
                // Image miroir ajoutée à la 2e moitie du tab (HOG recalcule sur l'inverse)
                int[] grisMiroir  = miroirGris(gris, im.largeur(), im.hauteur());
                entreesTrain[N+i] = Hog.calcule(grisMiroir, im.largeur(), im.hauteur());
                // Cible 1 pour le neurone du bon type, 0 others
                for (int k = 0; k < NB_TYPE; ++k) {
                    float cible = (type == k) ? 1f : 0f;
                    ciblesTrain[k][i]   = cible;
                    ciblesTrain[k][N+i] = cible;
                }
                nbPartype[type] += 2;
                if ((i+1) % 1000 == 0)
                    System.out.printf("    %d / %d images chargees%n", i+1, N);
            }

            long dt = System.currentTimeMillis() - t0;
            System.out.printf("    %d images chargees (%d originales + %d miroirs) en %.1f s%n",
                              M, N, N, dt/1000.0);
            for (int k = 0; k < NB_TYPE; ++k)
                System.out.printf("      %-5s : %d images%n", TYPE[k], nbPartype[k]);

            // Creation des 3 neurones sigmoides (un par type)
            tailleEntree = entreesTrain[0].length;
            System.out.println("[2/4] Creation des 3 neurones sigmoides");
            System.out.printf("    %d entrees (caracteristiques HOG)%n", tailleEntree);
            for (int k = 0; k < NB_TYPE; ++k)
                neurones[k] = new NeuroneSigmoide(tailleEntree);

            // Apprentissage
            System.out.printf("[3/4] Apprentissage (eta=%.4f, MSElimite=%.3f)...%n",
                              ETA, MSE_LIMITE);
            long tA = System.currentTimeMillis();
            for (int k = 0; k < NB_TYPE; ++k) {
                System.out.printf("    -- Neurone %s --%n", TYPE[k]);
                totalIterations += neurones[k].apprentissage(entreesTrain, ciblesTrain[k], MSE_LIMITE);
            }
            long dtA = System.currentTimeMillis() - tA;
            System.out.printf("    Apprentissage termine en %.1f s (%d iterations cumulees)%n",
                              dtA/1000.0, totalIterations);

            // Sauvegarde des 3 neurones
            new java.io.File(DIR_MODELE).mkdirs();
            for (int k = 0; k < NB_TYPE; ++k)
                neurones[k].sauvegarde(fichierModele(k));
        }
        else
        {
            System.out.println("[1/4] Chargement des sauvegardes");
            List<String> echantillon = Image.listeFichiers(DIR_TEST);
            if (echantillon == null || echantillon.isEmpty()) {
                System.err.println("Aucun fichier trouve dans " + DIR_TEST);
                return;
            }
            Image ref = new Image(echantillon.get(0), 0, NIVEAUX_DE_GRIS);
            tailleEntree = Hog.taille(ref.largeur(), ref.hauteur());
            System.out.printf("    %d entrees attendues par neurone%n", tailleEntree);
            for (int k = 0; k < NB_TYPE; ++k) {
                neurones[k] = new NeuroneSigmoide(tailleEntree);
                neurones[k].chargement(fichierModele(k));
            }
        }

        // Test sur la BDD (argmax des 3 sorties)
        System.out.println("[4/4] Evaluation sur la base de donnée...");
        List<String> cheminsTest = Image.listeFichiers(DIR_TEST);
        int[][] conf = new int[NB_TYPE][NB_TYPE]; // conf[reel][predit]
        for (String chemin : cheminsTest) {
            int reel = typeReel(chemin);
            Image im = new Image(chemin, reel, NIVEAUX_DE_GRIS);
            float[] e = caracteristiques(im);
            int predit = predictionType(neurones, e);
            conf[reel][predit]++;
        }

        int total = 0, bonnes = 0;
        for (int r = 0; r < NB_TYPE; ++r)
            for (int p = 0; p < NB_TYPE; ++p) {
                total += conf[r][p];
                if (r == p) bonnes += conf[r][p];
            }
        double moyenne = 100.0 * bonnes / total;

        System.out.println("================ RESULTATS ================");
        System.out.printf("Base de test : %d images%n", total);
        System.out.println("lignes = reel, colonnes = predit :");
        System.out.printf("            %8s %8s %8s%n", TYPE[0], TYPE[1], TYPE[2]);
        for (int r = 0; r < NB_TYPE; ++r)
            System.out.printf("  %-8s %8d %8d %8d%n",
                TYPE[r], conf[r][0], conf[r][1], conf[r][2]);

        double sommePrecision = 0, sommeRappel = 0;
        System.out.println("Par type :");
        for (int k = 0; k < NB_TYPE; ++k) {
            int tp = conf[k][k];
            int predits = 0, reels = 0;
            for (int x = 0; x < NB_TYPE; ++x) { predits += conf[x][k]; reels += conf[k][x]; }
            double prec = predits == 0 ? 0 : 100.0 * tp / predits;
            double rapp = reels   == 0 ? 0 : 100.0 * tp / reels;
            sommePrecision += prec; sommeRappel += rapp;
            System.out.printf("  %-5s  precision=%.2f %%  rappel=%.2f %%%n", TYPE[k], prec, rapp);
        }
        double precision = sommePrecision / NB_TYPE;
        double rappel    = sommeRappel    / NB_TYPE;
        System.out.printf("moyenne générale : %.2f %%%n", moyenne);
        System.out.printf("Precision générale : %.2f %%", precision);
        System.out.printf("Rappel général : %.2f %%%n", rappel);
        System.out.println("===========================================");

        // print des resultats dans Results.md
        String type = "HOG Miroir";
        String neurone = "NeuroneSigmoide x" + NB_TYPE;
        enregistreResultats(type, neurone, nbImagesTrain, totalIterations, ETA, MSE_LIMITE,
                            moyenne, precision, rappel);

        // UI
        UserInterface.start(cheminsTest, neurones);
    }

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
                            } catch (NumberFormatException ignore) {}
                        }
                    }
                }
            }
            try (java.io.FileWriter w = new java.io.FileWriter(f, true)) {
                if (!entetePresente) w.write(entete);
                String date = java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss dd-MM-yyyy"));
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
