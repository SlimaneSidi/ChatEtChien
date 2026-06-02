public interface iNeurone
{
	// Calcule la valeur de sortie en fonction des entrées, des poids synaptiques,
	// du biais et de la fonction d'activation
	public void metAJour(final float[] entrees);
	
	// Accesseur pour la valeur de sortie/d'activation du neurone
	public float sortie();

	// Fonction d'apprentissage relative à la mse ; renvoie le nombre d'itérations effectuées
	public int apprentissage(final float[][] entrees, final float[] resultats, final float MSElimite);

	public void sauvegarde(String chemin); // optionel
	public void chargement(String chemin); // optionel
}
