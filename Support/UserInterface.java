import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Random;

public class UserInterface extends JFrame {
    private JLabel labelImage;
    private JLabel labelVerdict;
    private JButton boutonSuivant;

    private final List<String> listeChemins;
    private final iNeurone[] neurones;
    private final float[] moyenne;
    private final float[] ecart;
    private final Random rand = new Random();

    public UserInterface(List<String> chemins, iNeurone[] neurones, float[] moyenne, float[] ecart) {
        this.listeChemins = chemins;
        this.neurones = neurones;
        this.moyenne = moyenne;
        this.ecart = ecart;

        setTitle("Détecteur Chat / Chien / Wild - ISEN Groupe 9");
        setSize(450, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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

        String chemin = listeChemins.get(rand.nextInt(listeChemins.size()));

        ImageIcon icone = new ImageIcon(chemin);
        java.awt.Image imgRedim = icone.getImage().getScaledInstance(350, 350, java.awt.Image.SCALE_SMOOTH);
        labelImage.setIcon(new ImageIcon(imgRedim));

        int typeReel = ChaineTraitImage.typeReel(chemin);
        String reelTexte = ChaineTraitImage.TYPE[typeReel];

        // Meme pipeline qu'a l'apprentissage : image COULEUR -> features -> standardisation
        Image im = new Image(chemin, typeReel, false);
        if (im.donnees() == null) { labelVerdict.setText("Image illisible"); return; }
        float[] f = ChaineTraitImage.caracteristiques(im);
        for (int j = 0; j < f.length; j++) f[j] = (f[j] - moyenne[j]) / ecart[j];

        int predit = ChaineTraitImage.predictionType(neurones, f);
        neurones[predit].metAJour(f);
        float score = neurones[predit].sortie();
        String preditTexte = ChaineTraitImage.TYPE[predit];

        boolean correct = (predit == typeReel);
        labelVerdict.setText(preditTexte.toUpperCase()
            + " (score " + String.format("%.2f", score) + " | reel: " + reelTexte + ")");
        labelVerdict.setForeground(correct ? new Color(34, 139, 34) : Color.RED);
    }

    public static void start(List<String> cheminsTest, iNeurone[] neurones, float[] moyenne, float[] ecart) {
        SwingUtilities.invokeLater(() -> {
            UserInterface app = new UserInterface(cheminsTest, neurones, moyenne, ecart);
            app.setVisible(true);
        });
    }
}