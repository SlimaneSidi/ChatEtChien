package org.example;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.example.neurone.iNeurone;

public class UserInterface extends JFrame {

    private JLabel labelImage;
    private JLabel labelVerdict;
    private JButton boutonSuivant;

    private List<String> listeChemins;
    private iNeurone[] neurones;
    private Random rand;

    public UserInterface(List<String> chemins, iNeurone[] neurones) {
        this.listeChemins = chemins;
        this.neurones = neurones;
        this.rand = new Random();

        setTitle("Détecteur Chat / Chien / Wild - ISEN Groupe 9");
        setSize(450, 550);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // ne ferme pas l'app JavaFX
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        labelVerdict = new JLabel("Analyse en cours...", SwingConstants.CENTER);
        labelVerdict.setFont(new Font("Arial", Font.BOLD, 22));
        labelVerdict.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(labelVerdict, BorderLayout.NORTH);

        labelImage = new JLabel("", SwingConstants.CENTER);
        labelImage.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        add(labelImage, BorderLayout.CENTER);

        boutonSuivant = new JButton("Image suivante");
        boutonSuivant.setFont(new Font("Arial", Font.PLAIN, 18));
        boutonSuivant.setPreferredSize(new Dimension(100, 50));
        boutonSuivant.addActionListener(e -> afficherNouvelleImage());
        add(boutonSuivant, BorderLayout.SOUTH);

        afficherNouvelleImage();
    }

    private void afficherNouvelleImage() {
        if (listeChemins == null || listeChemins.isEmpty()) return;

        // Choix aléatoire d'une image de test
        String cheminAleatoire = listeChemins.get(rand.nextInt(listeChemins.size()));

        // Affichage de l'image redimensionnée
        ImageIcon icone = new ImageIcon(cheminAleatoire);
        java.awt.Image imgRedimensionnee = icone.getImage()
                .getScaledInstance(350, 350, java.awt.Image.SCALE_SMOOTH);
        labelImage.setIcon(new ImageIcon(imgRedimensionnee));

        // Détermination de la vraie classe pour vérification visuelle
        int typeReel = ChaineTraitImage.typeReel(cheminAleatoire);
        String reelTexte = ChaineTraitImage.TYPE[typeReel];

        // Traitement : mêmes caractéristiques HOG qu'à l'apprentissage
        org.example.Image imageJava = new org.example.Image(
                cheminAleatoire, typeReel, ChaineTraitImage.NIVEAUX_DE_GRIS);
        float[] entrees = ChaineTraitImage.caracteristiques(imageJava);

        int predit = ChaineTraitImage.predictionType(neurones, entrees);
        neurones[predit].metAJour(entrees);
        float score = neurones[predit].sortie();
        String preditTexte = ChaineTraitImage.TYPE[predit];

        // Vert = correct, rouge = erreur
        boolean correct = (predit == typeReel);
        labelVerdict.setText(preditTexte.toUpperCase()
                + " (score " + String.format("%.2f", score)
                + " | réel: " + reelTexte + ")");
        labelVerdict.setForeground(correct ? new Color(34, 139, 34) : Color.RED);
    }

    public static void start(List<String> cheminsTest, iNeurone[] neurones) {
        SwingUtilities.invokeLater(() -> {
            UserInterface app = new UserInterface(cheminsTest, neurones);
            app.setVisible(true);
        });
    }
}