import java.util.*;

public class ChaineTraitImage
{
    static final boolean NIVEAUX_DE_GRIS = true; 
    static final String DIR_TRAIN = "../dataset_Groupe_9/train";
    static final String DIR_TEST  = "../dataset_Groupe_9/test";
    static final float ETA        = 0.001f;
    static final float MSE_LIMITE = 0.1f;
    static final float SEUIL_DECISION = 0.5f;
    static final long  SEED       = 42L;

    // =========================================================
    // 🎛️ INTERRUPTEURS DE TEST (Pour comparer les performances)
    // =========================================================
    static final boolean ACTIVER_HISTOGRAMME   = false;  // Teste l'égalisation
    static final boolean ACTIVER_NORMALISATION = true; // Teste la standardisation
    // =========================================================

    public static NeuroneHistogrm egaliseur = new NeuroneHistogrm();
    public static NeuroneNormalis normaliseur; 

    static int labelChat(String chemin) {
        return chemin.contains("/cat/") || chemin.contains("\\cat\\") ? 1 : 0;
    }

    // Méthode unifiée pour l'interface UI et le jeu de Test
    public static float[] pretraiter(int[] donnees) {
        int[] pixels = donnees;
        
        // 1. Histogramme (si activé)
        if (ACTIVER_HISTOGRAMME) {
            pixels = egaliseur.egaliser(donnees);
        }
        
        float[] f = new float[donnees.length];
        
        // 2. Normalisation Globale vs Division par 255
        if (ACTIVER_NORMALISATION && normaliseur != null) {
            double[] srcDouble = new double[donnees.length];
            for (int i = 0; i < donnees.length; ++i) srcDouble[i] = pixels[i];
            
            double[] resDouble = normaliseur.normaliser(srcDouble);
            
            for (int i = 0; i < donnees.length; ++i) f[i] = (float) resDouble[i];
        } else {
            // Si la normalisation globale est désactivée, on replie sur la méthode classique (0 à 1)
            for (int i = 0; i < donnees.length; ++i) f[i] = pixels[i] / 255.0f;
        }
        return f;
    }

    public static void main(String[] args)
    {
        System.out.println("[1/4] Chargement de la base de donnees et pretraitement...");
        List<String> cheminsTrain = Image.listeFichiers(DIR_TRAIN);
        if (cheminsTrain == null || cheminsTrain.isEmpty()) {
            System.err.println("Aucun fichier trouve dans " + DIR_TRAIN);
            return;
        }

        Collections.shuffle(cheminsTrain, new Random(SEED));

        final int N = cheminsTrain.size();
        int nbChat = 0;
        long t0 = System.currentTimeMillis();

        int[][] pixelsEtape1 = new int[N][];
        float[] ciblesTrain  = new float[N];

        // --- ETAPE A : Chargement + Histogramme ---
        for (int i = 0; i < N; ++i) {
            int lbl = labelChat(cheminsTrain.get(i));
            Image im = new Image(cheminsTrain.get(i), lbl, NIVEAUX_DE_GRIS);
            
            if (ACTIVER_HISTOGRAMME) {
                pixelsEtape1[i] = egaliseur.egaliser(im.donnees());
            } else {
                pixelsEtape1[i] = im.donnees();
            }
            
            ciblesTrain[i]  = lbl;
            if (lbl == 1) ++nbChat;
            if ((i+1) % 1000 == 0) System.out.printf("    %d / %d images traitees (Etape 1)%n", i+1, N);
        }

        final int tailleEntree = pixelsEtape1[0].length;
        float[][] entreesTrain = new float[N][tailleEntree];

        // --- ETAPE B : Calcul des stats et Normalisation ---
        if (ACTIVER_NORMALISATION) {
            System.out.println("    Calcul des statistiques globales du dataset...");
            double sommePixels = 0;
            long totalElements = (long) N * tailleEntree;

            for (int i = 0; i < N; ++i) {
                for (int j = 0; j < tailleEntree; ++j) sommePixels += pixelsEtape1[i][j];
            }
            double moyenneGlobale = sommePixels / totalElements;

            double sommeCarresDiff = 0;
            for (int i = 0; i < N; ++i) {
                for (int j = 0; j < tailleEntree; ++j) {
                    double diff = pixelsEtape1[i][j] - moyenneGlobale;
                    sommeCarresDiff += diff * diff;
                }
            }
            double ecartTypeGlobal = Math.sqrt(sommeCarresDiff / totalElements);
            System.out.printf("    Stats globales -> Moyenne : %.2f, Ecart-Type : %.2f%n", moyenneGlobale, ecartTypeGlobal);

            normaliseur = new NeuroneNormalis(moyenneGlobale, ecartTypeGlobal);
            
            // Appliquer la normalisation Z-Score
            for (int i = 0; i < N; ++i) {
                double[] srcDouble = new double[tailleEntree];
                for (int j = 0; j < tailleEntree; ++j) srcDouble[j] = pixelsEtape1[i][j];
                double[] resDouble = normaliseur.normaliser(srcDouble);
                for (int j = 0; j < tailleEntree; ++j) entreesTrain[i][j] = (float) resDouble[j];
            }
        } else {
            // Si pas de normalisation, on divise simplement par 255
            for (int i = 0; i < N; ++i) {
                for (int j = 0; j < tailleEntree; ++j) {
                    entreesTrain[i][j] = pixelsEtape1[i][j] / 255.0f;
                }
            }
        }

        long dt = System.currentTimeMillis() - t0;
        System.out.printf("    %d images pretraitees en %.1f s (%d chats, %d non-chats)%n", N, dt/1000.0, nbChat, N-nbChat);

        // Creation du neurone
        System.out.println("[2/4] Creation du neurone sigmoide");
        Neurone.fixeCoefApprentissage(ETA);
        iNeurone n = new NeuroneSigmoide(tailleEntree);

        // Apprentissage avec sigmoide
        System.out.printf("[3/4] Apprentissage (eta=%.4f, MSElimite=%.3f)...%n", ETA, MSE_LIMITE);
        long tA = System.currentTimeMillis();
        n.apprentissage(entreesTrain, ciblesTrain, MSE_LIMITE);
        long dtA = System.currentTimeMillis() - tA;
        System.out.printf("    Apprentissage termine en %.1f s%n", dtA/1000.0);

        // Evaluation sur le jeu de test
        System.out.println("[4/4] Evaluation sur le jeu de test...");
        List<String> cheminsTest = Image.listeFichiers(DIR_TEST);
        int vp = 0, vn = 0, fp = 0, fn = 0; 

        for (String chemin : cheminsTest) {
            int vraiLbl = labelChat(chemin);
            Image im = new Image(chemin, vraiLbl, NIVEAUX_DE_GRIS);
            
            // Utilisation de la methode unifiee pour le test
            float[] e = pretraiter(im.donnees());

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
        System.out.printf("Base de donnees : %d images%n", total);
        System.out.println("Matrice de confusion :");
        System.out.println("                  predit=chat  predit=autre");
        System.out.printf("  reel=chat       %6d        %6d%n", vp, fn);
        System.out.printf("  reel=autre      %6d        %6d%n", fp, vn);
        System.out.printf("Accuracy  : %.2f %%%n", accuracy);
        System.out.printf("Baseline  : %.2f %%%n", baseline);
        System.out.println("===========================================");

        UserInterface.start(cheminsTest, n);
    }
}