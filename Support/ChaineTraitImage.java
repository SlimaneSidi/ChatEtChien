import java.util.*;

// Neurone choisi : sigmoide.
// Chaine de traitement complete : lecture des images, normalisation,
// melange, apprentissage avec un neurone.
// Label : 1 = chat, 0 = autre (chien ou wild).
//
// javac neurone/*.java *.java
// java -cp ".;neurone" ChaineTraitImage

public class ChaineTraitImage
{
    static final boolean NIVEAUX_DE_GRIS = false;
    static final String DIR_TRAIN = "../dataset_groupe_9/train";
    static final String DIR_TEST  = "../dataset_groupe_9/test";
    static final float ETA        = 0.001f;
    static final float MSE_LIMITE = 0.01f;
    static final float SEUIL_DECISION = 0.7f;
    static final long  SEED       = 67L;

    // Determine le label binaire d'une image a partir de son chemin (chat = 1, sinon = 0)
    static int labelChat(String chemin) {
        return chemin.contains("/cat/") || chemin.contains("\\cat\\") ? 1 : 0;
    }

    // Normalise les pixels de [0, 255] vers [0, 1]
    static float[] normalise(int[] donnees) {
        float[] f = new float[donnees.length];
        for (int i = 0; i < donnees.length; ++i)
            f[i] = donnees[i] / 255.0f;
        return f;
    }

    public static void main(String[] args)
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
        float[][] entreesTrain = new float[N][];
        float[]   ciblesTrain  = new float[N];
        int nbChat = 0;
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < N; ++i) {
            int lbl = labelChat(cheminsTrain.get(i));
            Image im = new Image(cheminsTrain.get(i), lbl, NIVEAUX_DE_GRIS);
            entreesTrain[i] = normalise(im.donnees());
            ciblesTrain[i]  = lbl;
            if (lbl == 1) ++nbChat;
            if ((i+1) % 1000 == 0)
                System.out.printf("    %d / %d images chargees%n", i+1, N);
        }

        long dt = System.currentTimeMillis() - t0;
        System.out.printf("    %d images chargees en %.1f s (%d chats, %d non-chats)%n",
                          N, dt/1000.0, nbChat, N-nbChat);

        // Creation du neurone
        final int tailleEntree = entreesTrain[0].length;
        System.out.println("[2/4] Creation du neurone sigmoide");
        System.out.printf("    %d entrees (= %dx%d pixels)%n",
                          tailleEntree, 64, 64);
        Neurone.fixeCoefApprentissage(ETA);
        iNeurone n = new NeuroneSigmoide(tailleEntree);

        // Apprentissage avec sigmoide
        System.out.printf("[3/4] Apprentissage (eta=%.4f, MSElimite=%.3f)...%n",
                          ETA, MSE_LIMITE);
        long tA = System.currentTimeMillis();
        int nbIterations = n.apprentissage(entreesTrain, ciblesTrain, MSE_LIMITE);
        long dtA = System.currentTimeMillis() - tA;
        System.out.printf("    Apprentissage termine en %.1f s%n", dtA/1000.0);

        // Evaluation sur le jeu de test
        System.out.println("[4/4] Evaluation sur la base de donnée...");
        List<String> cheminsTest = Image.listeFichiers(DIR_TEST);
        int vp = 0, vn = 0, fp = 0, fn = 0; // vrai positif, vrai negatif, faux positif, faux negatif (positif = predit chat, negatif = predit autre)
        for (String chemin : cheminsTest) {
            int vraiLbl = labelChat(chemin);
            Image im = new Image(chemin, vraiLbl, NIVEAUX_DE_GRIS);
            float[] e = normalise(im.donnees());
            n.metAJour(e);
            int predit = n.sortie() >= SEUIL_DECISION ? 1 : 0;
            if      (vraiLbl == 1 && predit == 1) ++vp;
            else if (vraiLbl == 0 && predit == 0) ++vn;
            else if (vraiLbl == 0 && predit == 1) ++fp;
            else                                  ++fn;
        }

        int total = vp + vn + fp + fn;
        double accuracy  = 100.0 * (vp + vn) / total;
        double precision = vp + fp == 0 ? 0 : 100.0 * vp / (vp + fp);
        double rappel    = vp + fn == 0 ? 0 : 100.0 * vp / (vp + fn);
        //double baseline  = 100.0 * Math.max(vp+fn, vn+fp) / total;

        System.out.println("================ RESULTATS ================");
        System.out.printf("Base de données : %d images%n", total);
        System.out.println("Matrice de confusion :");
        System.out.println("                  predit=chat  predit=autre");
        System.out.printf("  reel=chat       %6d        %6d%n", vp, fn);
        System.out.printf("  reel=autre      %6d        %6d%n", fp, vn);
        System.out.printf("Accuracy  : %.2f %%%n", accuracy);
        System.out.printf("Precision : %.2f %% (parmi predits chat, combien vrais)%n", precision);
        System.out.printf("Rappel    : %.2f %% (parmi vrais chats, combien retrouves)%n", rappel);
        System.out.println("===========================================");

        // Journalisation des resultats (ajout en fin de Rapport/Results.md, sans ecraser)
        String type = NIVEAUX_DE_GRIS ? "NiveauDeGris" : "RGB";
        String neurone = n.getClass().getSimpleName();
        enregistreResultats(type, neurone, N, nbIterations, ETA, MSE_LIMITE,
                            accuracy, precision, rappel);


        // Interface UI

        UserInterface.start(cheminsTest, n);
    }

    // Ajoute une ligne de resultats a Rapport/Results.md.
    // Cree le fichier (et l'entete du tableau) s'il n'existe pas ou est vide,
    // sinon ajoute simplement une ligne sans effacer le contenu existant.
    // L'id est auto-incremente a partir du plus grand id deja present.
    static void enregistreResultats(String type, String neurone, int nbImagesTrain,
                                    int iterations, float eta, float mseLimite,
                                    double accuracy, double precision, double rappel) {
        final String chemin = "../Rapport/Results.md";
        final String entete =
            "# Resultats des executions\n\n" +
            "| id | date | type | neurone | images_train | iterations | eta | mse_limite | accuracy (%) | precision (%) | rappel (%) |\n" +
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
                    eta, mseLimite, accuracy, precision, rappel));
            }
            System.out.printf("Resultats ajoutes a %s (id=%d)%n", chemin, prochainId);
        } catch (java.io.IOException e) {
            System.err.println("Impossible d'ecrire les resultats dans " + chemin);
            e.printStackTrace();
        }
    }
}
