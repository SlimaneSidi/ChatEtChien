import java.awt.*;
import java.util.List;
import java.util.Random;
import javax.swing.*;

public class MiniJeu extends JFrame {
    private JLabel labelImage;
    private JLabel labelVerdict;
    private JButton boutonSuivant;

    private List<String> listeChemins;
    private iNeurone[] neurones;
    private Random rand;

    public MiniJeu(List<String> chemins, iNeurone[] neurones) {
        this.listeChemins = chemins;
        this.neurones = neurones;
        this.rand = new Random();

        setTitle("Mini-Jeu — Détecteur Chat / Chien / Wild");
        setSize(450, 550);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        labelVerdict = new JLabel("Analyse en cours...", SwingConstants.CENTER);
        labelVerdict.setFont(new Font("Arial", Font.BOLD, 22));
        labelVerdict.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(labelVerdict, BorderLayout.NORTH);

        labelImage = new JLabel("", SwingConstants.CENTER);
        labelImage.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        add(labelImage, BorderLayout.CENTER);

        boutonSuivant = new JButton(" Image suivante");
        boutonSuivant.setFont(new Font("Arial", Font.PLAIN, 18));
        boutonSuivant.setPreferredSize(new Dimension(100, 50));
        boutonSuivant.addActionListener(e -> afficherNouvelleImage());
        add(boutonSuivant, BorderLayout.SOUTH);

        afficherNouvelleImage();
    }

    private void afficherNouvelleImage() {
        if (listeChemins == null || listeChemins.isEmpty()) return;

        String cheminAleatoire = listeChemins.get(rand.nextInt(listeChemins.size()));

        ImageIcon icone = new ImageIcon(cheminAleatoire);
        java.awt.Image imgRedimensionnee = icone.getImage().getScaledInstance(350, 350, java.awt.Image.SCALE_SMOOTH);
        labelImage.setIcon(new ImageIcon(imgRedimensionnee));

        int typeReel = ChaineTraitImage.typeReel(cheminAleatoire);
        String reelTexte = ChaineTraitImage.TYPE[typeReel];

        Image imageJava = new Image(cheminAleatoire, typeReel, ChaineTraitImage.NIVEAUX_DE_GRIS);
        float[] entrees = ChaineTraitImage.caracteristiques(imageJava);

        int predit = ChaineTraitImage.predictionType(neurones, entrees);
        neurones[predit].metAJour(entrees);
        float score = neurones[predit].sortie();
        String preditTexte = ChaineTraitImage.TYPE[predit];

        boolean correct = (predit == typeReel);
        labelVerdict.setText(preditTexte.toUpperCase()
            + " (score " + String.format("%.2f", score) + " | reel: " + reelTexte + ")");
        labelVerdict.setForeground(correct ? new Color(34, 139, 34) : Color.RED);
    }

    public static void start(List<String> cheminsTest, iNeurone[] neurones) {
        SwingUtilities.invokeLater(() -> {
            MiniJeu app = new MiniJeu(cheminsTest, neurones);
            app.setVisible(true);
        });
    }
}