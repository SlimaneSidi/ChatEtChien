// HOG : Histogramme de Gradients Orientes
/* Transforme une image en niveaux de gris en un vecteur de caracteristiques
// decrivant les contours (directions de gradient), bien plus separable
// lineairement que les pixels bruts -> ideal pour un neurone mono-couche.
//
// Pipeline : gradients -> histogrammes d'orientation par cellule
//            -> normalisation par bloc (L2) -> concatenation.
//
// Pour une image 64x64 : 8x8 cellules, blocs 2x2, 9 orientations
//   => 7x7 blocs x (2x2x9) = 49 x 36 = 1764 entrees (contre 12288 en RGB brut). */

public class Hog
{
    static final int   TAILLE_CELLULE   = 8;     // cote d'une cellule, en pixels
    static final int   CELLULES_BLOC    = 2;     // cote d'un bloc, en cellules
    static final int   NB_ORIENTATIONS  = 9;     // bins d'orientation sur [0, 180)
    static final float EPSILON          = 1e-6f; // evite la division par zero

    // Nombre d'entrees produites pour une image largeur x hauteur (sans la calculer)
    public static int taille(int largeur, int hauteur) {
        int cellulesX = largeur / TAILLE_CELLULE;
        int cellulesY = hauteur / TAILLE_CELLULE;
        int blocsX = cellulesX - CELLULES_BLOC + 1;
        int blocsY = cellulesY - CELLULES_BLOC + 1;
        if (blocsX <= 0 || blocsY <= 0) return 0;
        return blocsX * blocsY * CELLULES_BLOC * CELLULES_BLOC * NB_ORIENTATIONS;
    }

    // Calcule le descripteur HOG d'une image en niveaux de gris (valeurs 0..255,
    // applatie ligne par ligne, taille = largeur*hauteur).
    public static float[] calcule(int[] gris, int largeur, int hauteur)
    {
        // --- 1. Gradients en chaque pixel (magnitude + orientation non signee) ---
        float[] mag = new float[largeur * hauteur];
        float[] ori = new float[largeur * hauteur]; // en degres, ramene a [0, 180)
        for (int y = 0; y < hauteur; ++y) {
            for (int x = 0; x < largeur; ++x) {
                final int xg = Math.max(0, x - 1), xd = Math.min(largeur - 1, x + 1);
                final int yh = Math.max(0, y - 1), yb = Math.min(hauteur - 1, y + 1);
                final float gx = gris[y * largeur + xd] - gris[y * largeur + xg];
                final float gy = gris[yb * largeur + x] - gris[yh * largeur + x];
                final int idx = y * largeur + x;
                mag[idx] = (float) Math.sqrt(gx * gx + gy * gy);
                float angle = (float) Math.toDegrees(Math.atan2(gy, gx)); // [-180, 180]
                if (angle < 0) angle += 180f;        // gradient non signe : 0 == 180
                if (angle >= 180f) angle -= 180f;
                ori[idx] = angle;
            }
        }

        // --- 2. Histogramme d'orientations par cellule (interpolation entre bins) ---
        final int cellulesX = largeur / TAILLE_CELLULE;
        final int cellulesY = hauteur / TAILLE_CELLULE;
        final float largeurBin = 180f / NB_ORIENTATIONS;
        float[][] histos = new float[cellulesX * cellulesY][NB_ORIENTATIONS];
        for (int cy = 0; cy < cellulesY; ++cy) {
            for (int cx = 0; cx < cellulesX; ++cx) {
                final float[] h = histos[cy * cellulesX + cx];
                for (int dy = 0; dy < TAILLE_CELLULE; ++dy) {
                    for (int dx = 0; dx < TAILLE_CELLULE; ++dx) {
                        final int idx = (cy * TAILLE_CELLULE + dy) * largeur
                                      + (cx * TAILLE_CELLULE + dx);
                        // repartition lineaire de la magnitude sur les 2 bins voisins
                        final float posBin = ori[idx] / largeurBin - 0.5f;
                        final int bas = (int) Math.floor(posBin);
                        final float frac = posBin - bas;
                        final int b0 = ((bas % NB_ORIENTATIONS) + NB_ORIENTATIONS) % NB_ORIENTATIONS;
                        final int b1 = (b0 + 1) % NB_ORIENTATIONS;
                        h[b0] += mag[idx] * (1f - frac);
                        h[b1] += mag[idx] * frac;
                    }
                }
            }
        }

        // --- 3. Normalisation par bloc (L2) et concatenation du descripteur ---
        final int blocsX = cellulesX - CELLULES_BLOC + 1;
        final int blocsY = cellulesY - CELLULES_BLOC + 1;
        final int tailleBloc = CELLULES_BLOC * CELLULES_BLOC * NB_ORIENTATIONS;
        float[] descripteur = new float[Math.max(0, blocsX * blocsY) * tailleBloc];
        int indice = 0;
        for (int by = 0; by < blocsY; ++by) {
            for (int bx = 0; bx < blocsX; ++bx) {
                final float[] bloc = new float[tailleBloc];
                float somme = 0f;
                int k = 0;
                for (int iy = 0; iy < CELLULES_BLOC; ++iy)
                    for (int ix = 0; ix < CELLULES_BLOC; ++ix) {
                        final float[] h = histos[(by + iy) * cellulesX + (bx + ix)];
                        for (int o = 0; o < NB_ORIENTATIONS; ++o) {
                            bloc[k] = h[o];
                            somme += h[o] * h[o];
                            ++k;
                        }
                    }
                final float norme = (float) Math.sqrt(somme + EPSILON * EPSILON);
                for (int j = 0; j < tailleBloc; ++j)
                    descripteur[indice++] = bloc[j] / norme;
            }
        }
        return descripteur;
    }
}
