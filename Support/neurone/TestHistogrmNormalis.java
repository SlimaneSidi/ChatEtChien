public class TestHistogrmNormalis{

    public static void main(String[] args) {
        
        System.out.println("=== TEST DE LA NORMALISATION ===");
        // Imaginons que sur tout le dataset "Chats et Chiens", 
        // la moyenne des pixels est de 127.0 et l'écart-type est de 50.0
        NeuroneNormalis normaliseur = new NeuroneNormalis(127.0, 50.0);
        
        // On teste avec 3 pixels : un sombre, un moyen, un clair
        double[] pixelsTest = {77.0, 127.0, 177.0}; 
        
        // Calcul attendu : 
        // (77 - 127) / 50 = -1.0
        // (127 - 127) / 50 = 0.0
        // (177 - 127) / 50 = 1.0
        
        double[] resultatNorm = normaliseur.normaliser(pixelsTest);
        
        System.out.println("Résultats attendus : -1.0 | 0.0 | 1.0");
        System.out.print("Résultats obtenus  : ");
        for (double p : resultatNorm) {
            System.out.print(p + " | ");
        }
        System.out.println("\n");


        System.out.println("=== TEST DE L'ÉGALISATION D'HISTOGRAMME ===");
        NeuroneHistogrm egaliseur = new NeuroneHistogrm();
        
        // On simule une mini-image de 5 pixels très sombre et peu contrastée
        int[] imageSombre = {10, 12, 10, 15, 12}; 
        
        int[] resultatHist = egaliseur.egaliser(imageSombre);
        
        // L'égalisation devrait forcer la valeur la plus basse (10) vers 0 
        // et la valeur la plus haute (15) vers 255 pour étirer le contraste.
        System.out.print("Pixels d'origine   : ");
        for (int p : imageSombre) {
            System.out.print(p + " ");
        }
        
        System.out.println();
        System.out.print("Pixels égalisés    : ");
        for (int p : resultatHist) {
            System.out.print(p + " ");
        }
        System.out.println();
    }
}
