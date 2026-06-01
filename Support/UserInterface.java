import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.Random;

public class UserInterface extends JFrame {
    private JLabel labelImage;
    private JLabel labelVerdict;
    private JButton boutonSuivant;

    private List<String> listeChemins;
    private iNeurone neuroneEntraine;
    private Random rand;

    public UserInterface(List<String> chemins, iNeurone neurone) {
        this.listeChemins = chemins;
        this.neuroneEntraine = neurone;
        this.rand = new Random();

        setTitle("Détecteur de Chats - ISEN Groupe 9");
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

        // Choix aléatoire d'une image de test
        String cheminAleatoire = listeChemins.get(rand.nextInt(listeChemins.size()));

        // Affichage de l'image redimensionnée
        ImageIcon icone = new ImageIcon(cheminAleatoire);
        java.awt.Image imgRedimensionnee = icone.getImage().getScaledInstance(350, 350, java.awt.Image.SCALE_SMOOTH);
        labelImage.setIcon(new ImageIcon(imgRedimensionnee));

        // Détermination du vrai label pour vérification visuelle humaine
        int vraiLabel = ChaineTraitImage.labelChat(cheminAleatoire);
        String labelReelTexte = (vraiLabel == 1) ? "Chat" : "Autre";

        // Traitement du signal par le neurone
        Image imageJava = new Image(cheminAleatoire, vraiLabel, false);
        float[] entreesNormalisees = ChaineTraitImage.normalise(imageJava.donnees());

        neuroneEntraine.metAJour(entreesNormalisees);
        float score = neuroneEntraine.sortie();

        // Verdict final par seuillage
        if (score >= 0.5f) {
            labelVerdict.setText(" CHAT ! (Score: " + String.format("%.2f", score) + " | Réel: " + labelReelTexte + ")");
            labelVerdict.setForeground(new Color(34, 139, 34)); // Vert
        } else {
            labelVerdict.setText(" NON CHAT (Score: " + String.format("%.2f", score) + " | Réel: " + labelReelTexte + ")");
            labelVerdict.setForeground(Color.RED);
        }
    }

    public static void start(List<String> cheminsTest, iNeurone neurone) {
        SwingUtilities.invokeLater(() -> {
            UserInterface app = new UserInterface(cheminsTest, neurone);
            app.setVisible(true);
        });
    }
}