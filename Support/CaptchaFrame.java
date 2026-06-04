import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

public class CaptchaFrame extends JFrame {

    private final List<String> cheminsImages;
    private final iNeurone[] neurones;
    private final Random rand = new Random();

    private int typeCibleIndex;
    private int tentatives = 0;
    private static final int MAX_TENTATIVES = 3;
    private boolean resolu = false;

    private final List<CelluleCaptcha> cellules = new ArrayList<>();
    private JPanel grillePanel;
    private JLabel instructionLabel;
    private JLabel statusLabel;
    private JLabel tentativesLabel;
    private JLabel robotLabel;
    private JProgressBar robotBar;
    private JButton verifyBtn;
    private JButton refreshBtn;

    private static final Color CLR_BG = new Color(245, 247, 250);
    private static final Color CLR_PRIMARY = new Color(33, 150, 243);
    private static final Color CLR_SUCCESS = new Color(76, 175, 80);
    private static final Color CLR_DANGER = new Color(244, 67, 54);

    public CaptchaFrame(List<String> cheminsImages, iNeurone[] neurones) {
        this.cheminsImages = cheminsImages;
        this.neurones = neurones;

        setTitle("Verification CAPTCHA");
        setSize(560, 800);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        mainPanel.add(createHeader(), BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.WHITE);
        centerPanel.add(createInstructions(), BorderLayout.NORTH);
        centerPanel.add(createGridPanel(), BorderLayout.CENTER);
        centerPanel.add(createStatusPanel(), BorderLayout.SOUTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        mainPanel.add(createFooter(), BorderLayout.SOUTH);

        add(mainPanel);
        genererGrille();
    }

    // ═══════════════════════════════════════════════════════════════════
    //  HEADER
    // ═══════════════════════════════════════════════════════════════════

    private JPanel createHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setPreferredSize(new Dimension(520, 50));
        p.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 225, 230)));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBackground(Color.WHITE);
        JLabel titre = new JLabel("Verification de securite");
        titre.setFont(new Font("Arial", Font.BOLD, 14));
        JLabel sous = new JLabel("reCAPTCHA");
        sous.setFont(new Font("Arial", Font.PLAIN, 10));
        sous.setForeground(new Color(120, 120, 120));
        left.add(titre);
        left.add(sous);
        p.add(left, BorderLayout.WEST);

        return p;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  INSTRUCTIONS
    // ═══════════════════════════════════════════════════════════════════

    private JPanel createInstructions() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        JLabel title = new JLabel("Prouvez que vous n'etes pas un robot");
        title.setFont(new Font("Arial", Font.BOLD, 13));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(title);

        instructionLabel = new JLabel("Chargement...");
        instructionLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        instructionLabel.setForeground(new Color(80, 80, 80));
        instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(instructionLabel);

        return p;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  GRILLE
    // ═══════════════════════════════════════════════════════════════════

    private JPanel createGridPanel() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        grillePanel = new JPanel(new GridLayout(3, 3, 6, 6));
        grillePanel.setBackground(Color.WHITE);
        grillePanel.setPreferredSize(new Dimension(390, 390));

        wrapper.add(grillePanel);
        return wrapper;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  STATUS
    // ═══════════════════════════════════════════════════════════════════

    private JPanel createStatusPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 225, 230)));
        p.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 11));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(statusLabel);
        p.add(Box.createVerticalStrut(8));

        JPanel labelPanel = new JPanel(new BorderLayout());
        labelPanel.setBackground(Color.WHITE);
        robotLabel = new JLabel("Probabilite robot : 0%");
        robotLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        tentativesLabel = new JLabel("0 / " + MAX_TENTATIVES);
        tentativesLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        labelPanel.add(robotLabel, BorderLayout.WEST);
        labelPanel.add(tentativesLabel, BorderLayout.EAST);
        p.add(labelPanel);
        p.add(Box.createVerticalStrut(4));

        robotBar = new JProgressBar(0, 100);
        robotBar.setValue(0);
        robotBar.setForeground(CLR_SUCCESS);
        robotBar.setBackground(new Color(230, 230, 230));
        robotBar.setPreferredSize(new Dimension(400, 10));
        p.add(robotBar);

        return p;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  FOOTER (Boutons)
    // ═══════════════════════════════════════════════════════════════════

    private JPanel createFooter() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 225, 230)));
        p.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        p.setPreferredSize(new Dimension(520, 50));

        refreshBtn = createButton("Nouvelle grille", new Color(100, 109, 118), 130);
        refreshBtn.addActionListener(e -> genererGrille());
        p.add(refreshBtn);

        verifyBtn = createButton("Valider", new Color(76, 175, 80), 130);
        verifyBtn.addActionListener(e -> verifier());
        p.add(verifyBtn);

        return p;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  GÉNÉRATION DE LA GRILLE
    // ═══════════════════════════════════════════════════════════════════

    private void genererGrille() {
    grillePanel.removeAll();
    cellules.clear();
    statusLabel.setText(" ");
    statusLabel.setForeground(Color.BLACK);

    if (cheminsImages == null || cheminsImages.isEmpty()) {
        instructionLabel.setText("Aucune image disponible");
        grillePanel.revalidate();
        grillePanel.repaint();
        return;
    }

    Map<Integer, List<String>> parType = new HashMap<>();
    parType.put(0, new ArrayList<>());
    parType.put(1, new ArrayList<>());
    parType.put(2, new ArrayList<>());

    for (String chemin : cheminsImages) {
        int t = detectType(chemin);
        if (t >= 0 && t < 3) parType.get(t).add(chemin);
    }

    // Limiter à 1000 par type
    for (int i = 0; i < 3; i++) {
        if (parType.get(i).size() > 1000) {
            Collections.shuffle(parType.get(i), rand);
            parType.put(i, new ArrayList<>(parType.get(i).subList(0, 1000)));
        }
    }

    int typeCount = 0;
    int typeSimple = -1;
    for (int i = 0; i < 3; i++) {
        if (!parType.get(i).isEmpty()) { typeCount++; typeSimple = i; }
    }

    List<CellData> data = new ArrayList<>();

    if (typeCount == 1) {
        String[] labels = {"CHATS", "CHIENS", "WILD"};
        instructionLabel.setText("Selectionnez tous les " + labels[typeSimple]);
        typeCibleIndex = typeSimple;
        List<String> toutes = new ArrayList<>(parType.get(typeSimple));
        Collections.shuffle(toutes, rand);
        for (int i = 0; i < 9 && i < toutes.size(); i++) data.add(new CellData(toutes.get(i), typeSimple));
    } else {
        List<Integer> typesOk = new ArrayList<>();
        for (int i = 0; i < 3; i++) if (parType.get(i).size() >= 3) typesOk.add(i);
        if (typesOk.isEmpty()) {
            instructionLabel.setText("Pas assez d'images");
            grillePanel.revalidate(); grillePanel.repaint(); return;
        }
        typeCibleIndex = typesOk.get(rand.nextInt(typesOk.size()));
        String[] labels = {"CHATS", "CHIENS", "WILD"};
        instructionLabel.setText("Cliquez sur tous les " + labels[typeCibleIndex]);
        List<String> cibles = new ArrayList<>(parType.get(typeCibleIndex));
        List<String> autres = new ArrayList<>();
        for (int i = 0; i < 3; i++) if (i != typeCibleIndex) autres.addAll(parType.get(i));
        Collections.shuffle(cibles, rand); Collections.shuffle(autres, rand);
        int nbCibles = Math.min(3 + rand.nextInt(2), cibles.size());
        int nbAutres = Math.min(9 - nbCibles, autres.size());
        for (int i = 0; i < nbCibles; i++) data.add(new CellData(cibles.get(i), typeCibleIndex));
        for (int i = 0; i < nbAutres; i++) data.add(new CellData(autres.get(i), detectType(autres.get(i))));
        while (data.size() < 9) { String p = cheminsImages.get(rand.nextInt(cheminsImages.size())); data.add(new CellData(p, detectType(p))); }
    }

    Collections.shuffle(data, rand);
    for (CellData d : data) { CelluleCaptcha c = new CelluleCaptcha(d); cellules.add(c); grillePanel.add(c.panel); }
    grillePanel.revalidate(); grillePanel.repaint();
    verifyBtn.setText("Valider"); verifyBtn.setBackground(CLR_SUCCESS); verifyBtn.setEnabled(true);
    refreshBtn.setVisible(true); resolu = false;
}

    // ═══════════════════════════════════════════════════════════════════
    //  VÉRIFICATION
    // ═══════════════════════════════════════════════════════════════════

    private void verifier() {
    if (resolu) { dispose(); return; }
    tentatives++; tentativesLabel.setText(tentatives + " / " + MAX_TENTATIVES);
    boolean succes = true; int erreurs = 0;
    for (CelluleCaptcha c : cellules) {
        boolean estCible = (c.typeReel == typeCibleIndex);
        if (estCible && !c.selected) succes = false;
        if (!estCible && c.selected) { succes = false; erreurs++; }
    }
    if (succes) {
        resolu = true;
        statusLabel.setText("Humain confirme !");
        statusLabel.setForeground(CLR_SUCCESS);
        robotBar.setValue(0); robotBar.setForeground(CLR_SUCCESS);
        robotLabel.setText("Humain confirme a 100%");
        verifyBtn.setText("Quitter"); verifyBtn.setBackground(CLR_SUCCESS);
        refreshBtn.setVisible(false);
    } else if (tentatives >= MAX_TENTATIVES) {
        resolu = true;
        statusLabel.setText("ROBOT DETECTE");
        statusLabel.setForeground(CLR_DANGER);
        robotBar.setValue(100); robotBar.setForeground(CLR_DANGER);
        robotLabel.setText("Probabilite robot : 100%");
        verifyBtn.setText("Quitter"); verifyBtn.setBackground(CLR_DANGER);
        refreshBtn.setVisible(false);
    } else {
        statusLabel.setText(erreurs + " erreur(s). Reessayez !");
        statusLabel.setForeground(CLR_DANGER);
        int prob = (tentatives * 33);
        robotBar.setValue(prob);
        robotLabel.setText("Probabilite robot : " + prob + "%");
        javax.swing.Timer timer = new javax.swing.Timer(800, e -> genererGrille());
        timer.setRepeats(false); timer.start();
    }
}

    // ═══════════════════════════════════════════════════════════════════
    //  UTILITAIRES
    // ═══════════════════════════════════════════════════════════════════

    private int detectType(String chemin) {
        String lower = chemin.toLowerCase().replace('\\', '/');
        
        String[] parts = lower.split("/");
        for (int i = parts.length - 1; i >= Math.max(0, parts.length - 3); i--) {
            String part = parts[i].toLowerCase();
            if (part.contains("cat") || part.contains("chat")) return 0;
            if (part.contains("dog") || part.contains("chien")) return 1;
            if (part.contains("wild") || part.contains("sauvage")) return 2;
        }
        
        try {
            return ChaineTraitImage.typeReel(chemin);
        } catch (Exception e) {
            return -1;
        }
    }

    private JButton createButton(String txt, Color bg, int width) {
        JButton b = new JButton(txt);
        b.setFont(new Font("Arial", Font.BOLD, 11));
        b.setForeground(Color.WHITE);
        b.setBackground(bg);
        b.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(width, 35));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(bg.darker()); }
            public void mouseExited(MouseEvent e) { b.setBackground(bg); }
        });
        return b;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  CELLULE
    // ═══════════════════════════════════════════════════════════════════

    private static class CellData {
        final String chemin;
        final int typeReel;
        
        CellData(String chemin, int typeReel) {
            this.chemin = chemin;
            this.typeReel = typeReel;
        }
    }

    private class CelluleCaptcha {
        final JPanel panel;
        final int typeReel;
        boolean selected = false;

        CelluleCaptcha(CellData d) {
            this.typeReel = d.typeReel;
            
            panel = new JPanel(new BorderLayout());
            panel.setBackground(Color.WHITE);
            panel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 2));
            panel.setPreferredSize(new Dimension(120, 120));
            panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            JLabel img = new JLabel();
            img.setHorizontalAlignment(SwingConstants.CENTER);
            img.setVerticalAlignment(SwingConstants.CENTER);
            
            try {
                ImageIcon ic = new ImageIcon(d.chemin);
                if (ic.getIconWidth() > 0) {
                    java.awt.Image scaled = ic.getImage().getScaledInstance(115, 115, java.awt.Image.SCALE_SMOOTH);
                    img.setIcon(new ImageIcon(scaled));
                } else {
                    img.setText("?");
                    img.setFont(new Font("Arial", Font.PLAIN, 24));
                }
            } catch (Exception e) {
                img.setText("!");
                img.setFont(new Font("Arial", Font.PLAIN, 24));
            }
            
            panel.add(img, BorderLayout.CENTER);

            MouseAdapter clickHandler = new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    selected = !selected;
                    panel.setBorder(selected
                            ? BorderFactory.createLineBorder(CLR_PRIMARY, 4)
                            : BorderFactory.createLineBorder(new Color(200, 200, 200), 2));
                    panel.setBackground(selected ? new Color(227, 242, 253) : Color.WHITE);
                }
            };
            
            panel.addMouseListener(clickHandler);
            img.addMouseListener(clickHandler);
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  DÉMARRAGE
    // ═══════════════════════════════════════════════════════════════════

    public static void start(List<String> cheminsImages, iNeurone[] neurones) {
        SwingUtilities.invokeLater(() ->
                new CaptchaFrame(cheminsImages, neurones).setVisible(true));
    }
}