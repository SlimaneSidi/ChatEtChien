package org.example.neurone;
public class NeuroneSigmoide extends Neurone
{
	// Fonction d'activation d'un neurone (peut facilement être modifiée par héritage)
	protected float activation(final float valeur) {return 1f/(1f + (float)Math.exp(-valeur));} // classique formule
	
	// Constructeur
	public NeuroneSigmoide(final int nbEntrees) {super(nbEntrees);}
}
