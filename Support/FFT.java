// Transformee de Fourier rapide (FFT) pour les images.
// Implementation radix-2 Cooley-Tukey : les dimensions doivent etre
// des puissances de 2 (nos images font 64x64, c'est le cas).
public class FFT
{
    // FFT 1D en place sur les tableaux partie reelle (re) et imaginaire (im).
    // n = re.length doit etre une puissance de 2.
    // inverse = false : transformee directe ; true : transformee inverse (non normalisee).
    static void fft1D(double[] re, double[] im, boolean inverse)
    {
        final int n = re.length;

        // Permutation par renversement de bits (bit-reversal)
        for (int i = 1, j = 0; i < n; ++i) {
            int bit = n >> 1;
            for (; (j & bit) != 0; bit >>= 1)
                j ^= bit;
            j ^= bit;
            if (i < j) {
                double tr = re[i]; re[i] = re[j]; re[j] = tr;
                double ti = im[i]; im[i] = im[j]; im[j] = ti;
            }
        }

        // Papillons (butterflies) par tailles croissantes 2, 4, 8, ... n
        for (int len = 2; len <= n; len <<= 1) {
            final double ang = 2 * Math.PI / len * (inverse ? 1 : -1);
            final double wr = Math.cos(ang), wi = Math.sin(ang);
            for (int i = 0; i < n; i += len) {
                double cr = 1, ci = 0; // racine de l'unite courante
                for (int k = 0; k < len / 2; ++k) {
                    final int a = i + k, b = i + k + len / 2;
                    final double xr = re[b] * cr - im[b] * ci;
                    final double xi = re[b] * ci + im[b] * cr;
                    re[b] = re[a] - xr; im[b] = im[a] - xi;
                    re[a] += xr;        im[a] += xi;
                    final double ncr = cr * wr - ci * wi;
                    ci = cr * wi + ci * wr;
                    cr = ncr;
                }
            }
        }
    }

    // Spectre log-magnitude normalise dans [0, 1] d'une image en niveaux de gris.
    // entree : pixels mis a plat (longueur = largeur*hauteur) ;
    // largeur et hauteur doivent etre des puissances de 2.
    // sortie : vecteur de meme longueur, log(1+|F|) normalise par son maximum.
    static float[] spectreLogMagnitude(float[] gris, int largeur, int hauteur)
    {
        double[][] re = new double[hauteur][largeur];
        double[][] im = new double[hauteur][largeur];
        for (int y = 0; y < hauteur; ++y)
            for (int x = 0; x < largeur; ++x)
                re[y][x] = gris[y * largeur + x];

        // FFT 1D sur chaque ligne
        for (int y = 0; y < hauteur; ++y)
            fft1D(re[y], im[y], false);

        // FFT 1D sur chaque colonne
        double[] cr = new double[hauteur], ci = new double[hauteur];
        for (int x = 0; x < largeur; ++x) {
            for (int y = 0; y < hauteur; ++y) { cr[y] = re[y][x]; ci[y] = im[y][x]; }
            fft1D(cr, ci, false);
            for (int y = 0; y < hauteur; ++y) { re[y][x] = cr[y]; im[y][x] = ci[y]; }
        }

        // log-magnitude : log(1+|F|) pour ecraser l'enorme dynamique du spectre
        float[] out = new float[largeur * hauteur];
        float max = 0f;
        for (int y = 0; y < hauteur; ++y)
            for (int x = 0; x < largeur; ++x) {
                final float v = (float) Math.log1p(Math.hypot(re[y][x], im[y][x]));
                out[y * largeur + x] = v;
                if (v > max) max = v;
            }

        // Normalisation dans [0, 1]
        if (max > 0f)
            for (int i = 0; i < out.length; ++i)
                out[i] /= max;

        return out;
    }
}
