package org.example.neurone;
public class NeuroneReLU extends Neurone
{
	// Fonction d'activation d'un neurone (peut facilement être modifiée par héritage)
	protected float activation(final float valeur) {return Math.max(0, valeur);} // ReLU

	// Constructeur
	public NeuroneReLU(final int nbEntrees) {super(nbEntrees);}
}
