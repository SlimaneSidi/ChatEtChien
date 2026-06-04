import java.util.*;

// ============================================================================
// HOG + (option) COULEUR, 3 neurones sigmoides (un par type), decision = argmax.
//
// Reprend la machinerie stabilisante de la V2 :
//   - standardisation z-score du vecteur concatene (HOG | couleur)
//   - init des poids a petite echelle (pas de saturation sigmoide)
//   - SGD stable : petit pas, shuffle par epoque, regularisation L2
//   - selection de la MEILLEURE epoque sur un set de validation (anti-oscillation)
//
// La couleur est derriere le flag COULEUR -> permet l'A/B (avec / sans).
// Le seuillage binaire de la V2 ne s'applique PAS ici : c'est de l'argmax multi-classe.
//
// javac neurone/*.java *.java
// java -cp ".;neurone" ChaineTraitImage           (ou "noui" pour sauter l'IHM)
// Si OutOfMemory : java -Xmx3g -cp ".;neurone" ChaineTraitImage
// ============================================================================
public class ChaineTraitImage
{
    static final String DIR_TRAIN = "../dataset_groupe_9/train";
    static final String DIR_TEST  = "../dataset_groupe_9/test";

    static final String[] TYPE = {"chat", "chien", "wild"};
    static final int      NB_TYPE = TYPE.length;

    // ----- Fusion couleur (mets false pour comparer HOG seul) -----
    static final boolean COULEUR = true;
    static final int   GRILLE_C = 2;     // grille spatiale 2x2 pour la couleur
    static final int   BINS_H   = 16;    // bins de teinte
    static final int   BINS_S   = 8;     // bins de saturation
    static final int   BINS_V   = 8;     // bins de valeur (luminosite)
    static final float S_MIN    = 0.15f; // saturation mini pour compter la teinte

    // ----- Apprentissage (machinerie V2) -----
    static final boolean MIROIR     = true;
    static final float   ETA        = 0.01f;
    static final int     EPOCHS     = 40;
    static final float   LAMBDA     = 1e-4f;
    static final float   VALID_FRAC = 0.15f;
    static final long    SEED       = System.currentTimeMillis(); // graine variable -> resultats differents a chaque lancement

    static int typeReel(String chemin) {
        if (chemin.contains("/cat/") || chemin.contains("\\cat\\")) return 0;
        if (chemin.contains("/dog/") || chemin.contains("\\dog\\")) return 1;
        return 2; // wild
    }

    // Type predit = indice du neurone de plus grande sortie (argmax).
    static int predictionType(iNeurone[] neurones, float[] entrees) {
        int meilleur = 0; float maxSortie = Float.NEGATIVE_INFINITY;
        for (int k = 0; k < neurones.length; ++k) {
            neurones[k].metAJour(entrees);
            float s = neurones[k].sortie();
            if (s > maxSortie) { maxSortie = s; meilleur = k; }
        }
        return meilleur;
    }

    // ---- Niveaux de gris depuis le RGB (avec miroir horizontal optionnel) ----
    static int[] grisDepuisRGB(int[] rgb, int W, int H, boolean miroir) {
        int[] g = new int[W * H];
        for (int y = 0; y < H; y++)
            for (int x = 0; x < W; x++) {
                int xs = miroir ? (W - 1 - x) : x;
                int s = y * W + xs;
                float gris = 0.2125f*rgb[3*s] + 0.7154f*rgb[3*s+1] + 0.0721f*rgb[3*s+2];
                g[y * W + x] = (int) Math.max(0, Math.min(255, gris));
            }
        return g;
    }

    // ---- Histogramme couleur HSV par cellule (teinte ponderee par saturation) ----
    static float[] histoCouleur(int[] rgb, int W, int H, boolean miroir) {
        int pc = BINS_H + BINS_S + BINS_V;
        float[] f = new float[GRILLE_C * GRILLE_C * pc];
        for (int y = 0; y < H; y++) {
            int gy = Math.min(GRILLE_C - 1, y * GRILLE_C / H);
            for (int x = 0; x < W; x++) {
                int gx = Math.min(GRILLE_C - 1, x * GRILLE_C / W);
                int xs = miroir ? (W - 1 - x) : x;
                int idx = y * W + xs;
                float r = rgb[3*idx]/255f, g = rgb[3*idx+1]/255f, b = rgb[3*idx+2]/255f;
                float M = Math.max(r, Math.max(g, b));
                float m = Math.min(r, Math.min(g, b));
                float C = M - m;
                float S = (M == 0f) ? 0f : C / M;
                float V = M;
                float Hh = 0f;
                if (C > 0f) {
                    if      (M == r) Hh = ((g - b) / C) % 6f;
                    else if (M == g) Hh = (b - r) / C + 2f;
                    else             Hh = (r - g) / C + 4f;
                    Hh *= 60f; if (Hh < 0) Hh += 360f;
                }
                int base = (gy * GRILLE_C + gx) * pc;
                if (S >= S_MIN) {
                    int bh = Math.min(BINS_H - 1, (int)(Hh / 360f * BINS_H));
                    f[base + bh] += S;
                }
                int bs = Math.min(BINS_S - 1, (int)(S * BINS_S));
                f[base + BINS_H + bs] += 1f;
                int bv = Math.min(BINS_V - 1, (int)(V * BINS_V));
                f[base + BINS_H + BINS_S + bv] += 1f;
            }
        }
        for (int c = 0; c < GRILLE_C * GRILLE_C; c++) {
            int base = c * pc;
            normaliseBloc(f, base, BINS_H);
            normaliseBloc(f, base + BINS_H, BINS_S);
            normaliseBloc(f, base + BINS_H + BINS_S, BINS_V);
        }
        return f;
    }
    static void normaliseBloc(float[] f, int base, int n) {
        float s = 0; for (int i=0;i<n;i++) s+=f[base+i];
        if (s>0) for (int i=0;i<n;i++) f[base+i]/=s;
    }

    // ---- Caracteristiques = HOG (+ couleur si COULEUR). RGB en entree. ----
    static float[] caracteristiques(int[] rgb, int W, int H, boolean miroir) {
        int[] gris = grisDepuisRGB(rgb, W, H, miroir);
        float[] hog = Hog.calcule(gris, W, H);
        if (!COULEUR) return hog;
        float[] col = histoCouleur(rgb, W, H, miroir);
        float[] out = new float[hog.length + col.length];
        System.arraycopy(hog, 0, out, 0, hog.length);
        System.arraycopy(col, 0, out, hog.length, col.length);
        return out;
    }
    // Surcharge utilisee par l'IHM (image chargee en COULEUR -> donnees() = RGB).
    static float[] caracteristiques(Image im) {
        return caracteristiques(im.donnees(), im.largeur(), im.hauteur(), false);
    }

    public static void main(String[] args)
    {
        System.out.println("===== HOG" + (COULEUR ? " + COULEUR (HSV)" : " seul") + " =====");
        System.out.printf("ETA=%.3f epochs=%d L2=%.0e miroir=%b%n", ETA, EPOCHS, LAMBDA, MIROIR);
        System.out.println("SEED = " + SEED + "  (note-la pour reproduire ce run exact)");

        // ---- 1) Chargement train + features ----
        System.out.println("[1/5] Chargement train + extraction...");
        List<String> cheminsTrain = Image.listeFichiers(DIR_TRAIN);
        if (cheminsTrain == null || cheminsTrain.isEmpty()) {
            System.err.println("Aucun fichier dans " + DIR_TRAIN); return;
        }
        Collections.shuffle(cheminsTrain, new Random(SEED));

        List<float[]> X = new ArrayList<>();
        List<Integer> T = new ArrayList<>();    // type reel par vecteur
        int F = -1, done = 0;
        long t0 = System.currentTimeMillis();
        for (String chemin : cheminsTrain) {
            int type = typeReel(chemin);
            Image im = new Image(chemin, type, false); // COULEUR (RGB)
            if (im.donnees() == null) continue;
            float[] v = caracteristiques(im.donnees(), im.largeur(), im.hauteur(), false);
            if (F == -1) F = v.length;
            if (v.length != F) continue;
            X.add(v); T.add(type);
            if (MIROIR) {
                X.add(caracteristiques(im.donnees(), im.largeur(), im.hauteur(), true));
                T.add(type);
            }
            if (++done % 1000 == 0) System.out.printf("    %d images%n", done);
        }
        System.out.printf("    %d vecteurs (%d features) en %.1f s%n",
                X.size(), F, (System.currentTimeMillis()-t0)/1000.0);

        // ---- 2) Standardisation z-score ----
        System.out.println("[2/5] Standardisation...");
        float[] moyenne = new float[F], ecart = new float[F];
        for (float[] v : X) for (int j=0;j<F;j++) moyenne[j]+=v[j];
        for (int j=0;j<F;j++) moyenne[j]/=X.size();
        for (float[] v : X) for (int j=0;j<F;j++){ float d=v[j]-moyenne[j]; ecart[j]+=d*d; }
        for (int j=0;j<F;j++){ ecart[j]=(float)Math.sqrt(ecart[j]/X.size()); if(ecart[j]<1e-6f) ecart[j]=1f; }
        for (float[] v : X) for (int j=0;j<F;j++) v[j]=(v[j]-moyenne[j])/ecart[j];

        // ---- 3) Split train / validation ----
        int n = X.size();
        Integer[] perm = new Integer[n]; for (int i=0;i<n;i++) perm[i]=i;
        Collections.shuffle(Arrays.asList(perm), new Random(SEED+7));
        int nVal = (int)(n*VALID_FRAC);
        float[][] Xtr=new float[n-nVal][]; int[] Ttr=new int[n-nVal];
        float[][] Xva=new float[nVal][];   int[] Tva=new int[nVal];
        for (int i=0;i<n;i++){
            if (i<nVal){ Xva[i]=X.get(perm[i]); Tva[i]=T.get(perm[i]); }
            else { Xtr[i-nVal]=X.get(perm[i]); Ttr[i-nVal]=T.get(perm[i]); }
        }

        // ---- 4) Apprentissage : 3 neurones, SGD + L2 + meilleure epoque ----
        System.out.println("[3/5] Apprentissage (3 neurones, argmax)...");
        iNeurone[] neurones = new iNeurone[NB_TYPE];
        Neurone[]  vues     = new Neurone[NB_TYPE];
        float[][]  w        = new float[NB_TYPE][];
        Random ri = new Random(SEED+1);
        for (int k=0;k<NB_TYPE;k++){
            neurones[k] = new NeuroneSigmoide(F);
            vues[k] = (Neurone) neurones[k];
            w[k] = vues[k].synapses();
            for (int j=0;j<F;j++) w[k][j] = (float)(ri.nextGaussian()*0.01); // petite init
            vues[k].fixeBiais(0f);
        }

        Integer[] ordre = new Integer[Xtr.length];
        for (int i=0;i<ordre.length;i++) ordre[i]=i;
        Random rs = new Random(SEED+2);

        float[][] bestW = new float[NB_TYPE][F]; float[] bestB = new float[NB_TYPE];
        double bestAcc = -1; int bestEpo = 0;
        for (int epo=1; epo<=EPOCHS; epo++){
            Collections.shuffle(Arrays.asList(ordre), rs);
            for (int idx : ordre){
                float[] x = Xtr[idx]; int type = Ttr[idx];
                for (int k=0;k<NB_TYPE;k++){
                    neurones[k].metAJour(x);
                    float cible = (type==k)?1f:0f;
                    float delta = cible - neurones[k].sortie();
                    float[] wk = w[k];
                    for (int j=0;j<F;j++) wk[j] += ETA*(delta*x[j] - LAMBDA*wk[j]);
                    vues[k].fixeBiais(vues[k].biais() + ETA*delta);
                }
            }
            double accVal = accValid(neurones, Xva, Tva);
            if (accVal > bestAcc){
                bestAcc=accVal; bestEpo=epo;
                for (int k=0;k<NB_TYPE;k++){ System.arraycopy(w[k],0,bestW[k],0,F); bestB[k]=vues[k].biais(); }
            }
            if (epo%5==0 || epo==1)
                System.out.printf("    epoque %d/%d  accVal=%.2f%%  (best=%.2f%% @%d)%n",
                        epo, EPOCHS, 100.0*accVal, 100.0*bestAcc, bestEpo);
        }
        for (int k=0;k<NB_TYPE;k++){ System.arraycopy(bestW[k],0,w[k],0,F); vues[k].fixeBiais(bestB[k]); }
        System.out.printf("    -> poids restaures de l'epoque %d (accVal=%.2f%%)%n", bestEpo, 100.0*bestAcc);

        // ---- 5) Evaluation sur le test (matrice 3x3) ----
        System.out.println("[4/5] Evaluation sur le test...");
        List<String> cheminsTest = Image.listeFichiers(DIR_TEST);
        int[][] conf = new int[NB_TYPE][NB_TYPE];
        for (String chemin : cheminsTest){
            int reel = typeReel(chemin);
            Image im = new Image(chemin, reel, false);
            if (im.donnees()==null) continue;
            float[] f = caracteristiques(im.donnees(), im.largeur(), im.hauteur(), false);
            if (f.length != F) continue;
            for (int j=0;j<F;j++) f[j]=(f[j]-moyenne[j])/ecart[j];
            conf[reel][predictionType(neurones, f)]++;
        }
        int total=0, bonnes=0;
        for (int r=0;r<NB_TYPE;r++) for (int p=0;p<NB_TYPE;p++){ total+=conf[r][p]; if(r==p) bonnes+=conf[r][p]; }

        System.out.println("================ RESULTATS ================");
        System.out.printf("Base de test : %d images%n", total);
        System.out.println("lignes = reel, colonnes = predit :");
        System.out.printf("            %8s %8s %8s%n", TYPE[0], TYPE[1], TYPE[2]);
        for (int r=0;r<NB_TYPE;r++)
            System.out.printf("  %-8s %8d %8d %8d%n", TYPE[r], conf[r][0], conf[r][1], conf[r][2]);
        double sp=0, sr=0;
        for (int k=0;k<NB_TYPE;k++){
            int tp=conf[k][k], predits=0, reels=0;
            for (int x=0;x<NB_TYPE;x++){ predits+=conf[x][k]; reels+=conf[k][x]; }
            double prec=predits==0?0:100.0*tp/predits, rapp=reels==0?0:100.0*tp/reels;
            sp+=prec; sr+=rapp;
            System.out.printf("  %-5s  precision=%.2f %%  rappel=%.2f %%%n", TYPE[k], prec, rapp);
        }
        System.out.printf("Accuracy globale   : %.2f %%%n", 100.0*bonnes/total);
        System.out.printf("Precision moyenne  : %.2f %%%n", sp/NB_TYPE);
        System.out.printf("Rappel moyen       : %.2f %%%n", sr/NB_TYPE);
        System.out.println("===========================================");

        boolean sansIHM = args.length>0 && args[0].equals("noui");
        if (!sansIHM) UserInterface.start(cheminsTest, neurones, moyenne, ecart);
    }

    static double accValid(iNeurone[] ns, float[][] X, int[] T){
        int ok=0; for (int i=0;i<X.length;i++) if (predictionType(ns, X[i])==T[i]) ok++;
        return X.length==0?0:(double)ok/X.length;
    }
}