import java.util.*;

// Neurone choisi : sigmoide.
// Chaine de traitement complete : lecture des images, normalisation,
// melange, apprentissage avec un neurone.
// Label : 1 = chat, 0 = autre (chien ou wild).
//
// javac neurone/*.java *.java
// java -cp .;neurone ChaineTraitImage

public class ChaineTraitImage
{
    static final boolean NIVEAUX_DE_GRIS = true;
    static final String DIR_TRAIN = "../dataset_groupe_9/train";
    static final String DIR_TEST  = "../dataset_groupe_9/test";
    static final float ETA        = 0.001f;
    static final float MSE_LIMITE = 0.1f;
    static final float SEUIL_DECISION = 0.5f;
    static final long  SEED       = 42L;

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
        n.apprentissage(entreesTrain, ciblesTrain, MSE_LIMITE);
        long dtA = System.currentTimeMillis() - tA;
        System.out.printf("    Apprentissage termine en %.1f s%n", dtA/1000.0);

        // Evaluation sur le jeu de test
        System.out.println("[4/4] Evaluation sur le jeu de test...");
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
        double baseline  = 100.0 * Math.max(vp+fn, vn+fp) / total;

        System.out.println("================ RESULTATS ================");
        System.out.printf("Base de données : %d images%n", total);
        System.out.println("Matrice de confusion :");
        System.out.println("                  predit=chat  predit=autre");
        System.out.printf("  reel=chat       %6d        %6d%n", vp, fn);
        System.out.printf("  reel=autre      %6d        %6d%n", fp, vn);
        System.out.printf("Accuracy  : %.2f %%%n", accuracy);
        System.out.printf("Precision : %.2f %% (parmi predits chat, combien vrais)%n", precision);
        System.out.printf("Rappel    : %.2f %% (parmi vrais chats, combien retrouves)%n", rappel);
        System.out.printf("Baseline (classifieur trivial 'tout autre') : %.2f %%%n", baseline);
        System.out.println("===========================================");


        // Interface UI

        UserInterface.start(cheminsTest, n);
    }
}
