public class NeuroneHistogrm {

    /**
     * Applique l'égalisation d'histogramme sur une image en niveaux de gris.
     * Les pixels en entrée doivent être dans l'intervalle [0, 255].
     */
    public int[] egaliser(int[] pixelsGris) {
        int totalPixels = pixelsGris.length;
        
        // 1. Calcul de l'histogramme des fréquences
        int[] histogramme = new int[256];
        for (int pixel : pixelsGris) {
            histogramme[pixel]++;
        }

        // 2. Calcul de l'histogramme cumulé (CDF - Cumulative Distribution Function)
        int[] cdf = new int[256];
        cdf[0] = histogramme[0];
        for (int i = 1; i < 256; i++) {
            cdf[i] = cdf[i - 1] + histogramme[i];
        }

        // 3. Trouver la plus petite valeur non nulle du CDF
        int minCdf = 0;
        for (int i = 0; i < 256; i++) {
            if (cdf[i] > 0) {
                minCdf = cdf[i];
                break;
            }
        }

        // 4. Égalisation des pixels
        int[] pixelsEgalises = new int[totalPixels];
        for (int i = 0; i < totalPixels; i++) {
            // Formule générale de l'égalisation d'histogramme
            double calcul = (double)(cdf[pixelsGris[i]] - minCdf) / (totalPixels - minCdf);
            pixelsEgalises[i] = (int) Math.round(calcul * 255);
        }

        return pixelsEgalises;
    }
}