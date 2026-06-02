public class NeuroneNormalis {
    
    private double moyenneGlobale;
    private double ecartTypeGlobal;

    // Constructeur qui prend les statistiques du dataset complet
    public NeuroneNormalis(double moyenneGlobale, double ecartTypeGlobal) {
        this.moyenneGlobale = moyenneGlobale;
        this.ecartTypeGlobal = ecartTypeGlobal;
    }

    /**
     * Applique la standardisation (Z-score normalization) sur un tableau de pixels.
     */
    public double[] normaliser(double[] pixelsBruts) {
        double[] pixelsNormalises = new double[pixelsBruts.length];
        
        for (int i = 0; i < pixelsBruts.length; i++) {
            // Application de la formule pour centrer autour de 0
            pixelsNormalises[i] = (pixelsBruts[i] - moyenneGlobale) / ecartTypeGlobal;
        }
        
        return pixelsNormalises;
    }
}