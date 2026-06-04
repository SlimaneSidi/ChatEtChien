import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;

public class UserInterface extends JFrame {

    // ── Drop zone ────────────────────────────────────────────────────
    private JPanel dropZone;
    private JLabel dropHint;
    private JLabel previewLabel;
    private CardLayout dropCardLayout;
    private JPanel dropContentPanel;

    // ── Infos image ──────────────────────────────────────────────────
    private JPanel imageInfoBox;
    private JLabel imageInfoName;
    private JLabel imageInfoSize;
    private JLabel imageInfoWeight;

    // ── Boutons ──────────────────────────────────────────────────────
    private JButton analyzeBtn;
    private JButton clearBtn;

    // ── Résultat ─────────────────────────────────────────────────────
    private JPanel resultCard;
    private JPanel resultPlaceholder;
    private JLabel resultEmoji;
    private JLabel resultLabel;
    private JLabel lowConfLabel;
    private JProgressBar confidenceBar;
    private JLabel confidencePercent;
    private JProgressBar catBar, dogBar, wildBar;
    private JLabel catPercent, dogPercent, wildPercent;

    // ── Stats ────────────────────────────────────────────────────────
    private JLabel statTotal, statChats, statChiens, statWild;

    // ── Logs ─────────────────────────────────────────────────────────
    private JTextArea logArea;

    // ── Données ──────────────────────────────────────────────────────
    private File selectedFile;
    private iNeurone[] neurones;
    private List<String> cheminsImages;
    private int sessionTotal = 0, sessionChats = 0, sessionChiens = 0, sessionWild = 0;

    private static final double SEUIL_CONFIANCE = 0.55;
    private static final Color CLR_CHAT  = new Color(74, 114, 184);
    private static final Color CLR_CHIEN = new Color(26, 58, 140);
    private static final Color CLR_WILD  = new Color(124, 92, 46);
    private static final Color CLR_UNKNOWN = new Color(107, 114, 128);

    // ═══════════════════════════════════════════════════════════════════
    //  CONSTRUCTEUR
    // ═══════════════════════════════════════════════════════════════════

    public UserInterface(iNeurone[] neurones) {
    this(neurones, null);
}

public UserInterface(iNeurone[] neurones, List<String> cheminsImages) {
    this.neurones = neurones;
    this.cheminsImages = cheminsImages;

    setTitle("Chat Et Chien — ISEN Groupe 9");
    setSize(1250, 780);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLocationRelativeTo(null);
    setResizable(false);
    getContentPane().setBackground(new Color(245, 247, 250));

    add(createHeader(), BorderLayout.NORTH);

    JPanel center = new JPanel(new BorderLayout(10, 0));
    center.setBackground(new Color(245, 247, 250));
    center.setBorder(BorderFactory.createEmptyBorder(12, 14, 14, 14));
    center.add(createLeftPanel(), BorderLayout.CENTER);

    JPanel rightWrapper = new JPanel(new BorderLayout());
    rightWrapper.setBackground(new Color(245, 247, 250));
    rightWrapper.setPreferredSize(new Dimension(400, 0));
    rightWrapper.add(createRightPanel(), BorderLayout.NORTH);
    center.add(rightWrapper, BorderLayout.EAST);

    add(center, BorderLayout.CENTER);

    resetUI();
    addLog("✅ Application prête — glissez une image ou cliquez");
}

    // ═══════════════════════════════════════════════════════════════════
    //  HEADER
    // ═══════════════════════════════════════════════════════════════════

    private JPanel createHeader() {
    JPanel h = new JPanel(new BorderLayout());
    h.setBackground(Color.WHITE);
    h.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, new Color(220, 225, 230)),
            BorderFactory.createEmptyBorder(14, 18, 14, 18)));

    JLabel logo = new JLabel("Chat Et Chien");
    logo.setFont(new Font("SansSerif", Font.BOLD, 19));
    logo.setForeground(new Color(33, 150, 243));
    h.add(logo, BorderLayout.WEST);

    JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
    rightPanel.setBackground(Color.WHITE);

    JLabel sub = new JLabel("ISEN Groupe 9");
    sub.setFont(new Font("SansSerif", Font.PLAIN, 12));
    sub.setForeground(new Color(120, 120, 120));
    rightPanel.add(sub);

    JButton miniJeuBtn = new JButton("🎮 Mini-Jeu");
    miniJeuBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
    miniJeuBtn.setForeground(Color.WHITE);
    miniJeuBtn.setBackground(new Color(155, 89, 182));
    miniJeuBtn.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
    miniJeuBtn.setFocusPainted(false);
    miniJeuBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    miniJeuBtn.addMouseListener(new MouseAdapter() {
        public void mouseEntered(MouseEvent e) { miniJeuBtn.setBackground(new Color(125, 60, 152)); }
        public void mouseExited(MouseEvent e)  { miniJeuBtn.setBackground(new Color(155, 89, 182)); }
    });
    miniJeuBtn.addActionListener(e -> ouvrirMiniJeu());
    rightPanel.add(miniJeuBtn);

    JButton captchaBtn = new JButton("🔒 CAPTCHA");
    captchaBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
    captchaBtn.setForeground(Color.WHITE);
    captchaBtn.setBackground(new Color(255, 152, 0));
    captchaBtn.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
    captchaBtn.setFocusPainted(false);
    captchaBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    captchaBtn.addMouseListener(new MouseAdapter() {
        public void mouseEntered(MouseEvent e) { captchaBtn.setBackground(new Color(225, 130, 0)); }
        public void mouseExited(MouseEvent e)  { captchaBtn.setBackground(new Color(255, 152, 0)); }
    });
    captchaBtn.addActionListener(e -> ouvrirCaptcha());
    rightPanel.add(captchaBtn);

    h.add(rightPanel, BorderLayout.EAST);

    return h;
}

    // ═══════════════════════════════════════════════════════════════════
    //  PANNEAU GAUCHE — DROP ZONE
    // ═══════════════════════════════════════════════════════════════════

    private JPanel createLeftPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setBackground(new Color(245, 247, 250));

        JPanel titles = new JPanel();
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.setBackground(new Color(245, 247, 250));
        JLabel t1 = new JLabel("📂  Zone d'analyse");
        t1.setFont(new Font("SansSerif", Font.BOLD, 16));
        t1.setForeground(new Color(33, 33, 33));
        titles.add(t1);
        p.add(titles, BorderLayout.NORTH);

        // ── Drop zone graphique ──────────────────────────────────────
dropZone = new JPanel(new BorderLayout()) {
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(230, 237, 250));
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
        g2.setColor(new Color(180, 200, 230));
        g2.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                0, new float[]{8, 4}, 0));
        g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 16, 16);
    }
};
dropZone.setOpaque(false);
dropZone.setPreferredSize(new Dimension(0, 420));
dropZone.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

dropCardLayout = new CardLayout();
dropContentPanel = new JPanel(dropCardLayout);
dropContentPanel.setOpaque(false);

JPanel hintPanel = new JPanel(new GridBagLayout());
hintPanel.setOpaque(false);

dropHint = new JLabel("<html><center>"
        + "<div style='font-size:42px;'>📁</div>"
        + "<div style='font-size:15px; color:#555;'>Glisser-deposer une image</div>"
        + "<div style='font-size:12px; color:#999;'>ou cliquer pour ajouter une image</div>"
        + "<div style='margin-top:6px;'>"
        + "<span style='background:#e8eef6; padding:2px 8px; font-size:11px;'>JPG</span> "
        + "<span style='background:#e8eef6; padding:2px 8px; font-size:11px;'>PNG</span> "
        + "<span style='background:#e8eef6; padding:2px 8px; font-size:11px;'>BMP</span>"
        + "</div></center></html>");
dropHint.setHorizontalAlignment(SwingConstants.CENTER);
hintPanel.add(dropHint);

JPanel previewPanel = new JPanel(new GridBagLayout());
previewPanel.setOpaque(false);

previewLabel = new JLabel("", SwingConstants.CENTER);
previewPanel.add(previewLabel);

dropContentPanel.add(hintPanel, "HINT");
dropContentPanel.add(previewPanel, "PREVIEW");

dropZone.add(dropContentPanel, BorderLayout.CENTER);

dropCardLayout.show(dropContentPanel, "HINT");

        // Drag & drop
        dropZone.setTransferHandler(new TransferHandler() {
            @Override
            public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
            }

            @Override
            public boolean importData(TransferSupport support) {
                if (!canImport(support)) return false;
                try {
                    @SuppressWarnings("unchecked")
                    List<File> files = (List<File>) support.getTransferable()
                            .getTransferData(DataFlavor.javaFileListFlavor);
                    if (!files.isEmpty()) {
                        File f = files.get(0);
                        if (isImageFile(f)) {
                            loadImage(f);
                            return true;
                        }
                        addLog("✗ Format non supporté : " + f.getName());
                    }
                } catch (Exception ex) {
                    addLog("✗ Erreur transfert : " + ex.getMessage());
                }
                return false;
            }
        });

        dropZone.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                JFileChooser fc = new JFileChooser();
                fc.setDialogTitle("Choisir une image");
                fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                        "Images", "jpg", "jpeg", "png", "bmp"));
                if (fc.showOpenDialog(UserInterface.this) == JFileChooser.APPROVE_OPTION) {
                    loadImage(fc.getSelectedFile());
                }
            }
        });

        p.add(dropZone, BorderLayout.CENTER);

        // ── Infos image ──────────────────────────────────────────────
        imageInfoBox = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 4));
        imageInfoBox.setBackground(new Color(245, 247, 250));
        imageInfoName = chipLabel("");
        imageInfoSize = chipLabel("");
        imageInfoWeight = chipLabel("");
        imageInfoBox.add(imageInfoName);
        imageInfoBox.add(imageInfoSize);
        imageInfoBox.add(imageInfoWeight);
        imageInfoBox.setVisible(false);

        // ── Boutons d'action ─────────────────────────────────────────
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        actions.setBackground(new Color(245, 247, 250));

        analyzeBtn = actionBtn("🔍  Analyser", new Color(33, 150, 243));
        clearBtn   = actionBtn("🗑  Effacer", new Color(244, 67, 54));

        analyzeBtn.addActionListener(e -> doAnalyze());
        clearBtn.addActionListener(e -> doClear());

        actions.add(analyzeBtn);
        actions.add(clearBtn);

        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.setBackground(new Color(245, 247, 250));
        bottom.add(imageInfoBox);
        bottom.add(Box.createVerticalStrut(8));
        bottom.add(actions);
        p.add(bottom, BorderLayout.SOUTH);

        return p;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  PANNEAU DROIT — RÉSULTAT + STATS + LOGS
    // ═══════════════════════════════════════════════════════════════════

    private JPanel createRightPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(new Color(245, 247, 250));

        p.add(buildResultCard());
        p.add(Box.createVerticalStrut(14));
        p.add(buildStatsCard());
        p.add(Box.createVerticalStrut(14));
        p.add(buildLogsCard());

        return p;
    }

    private JPanel buildResultCard() {
        JPanel card = cardBase();
        card.setMaximumSize(new Dimension(400, 420));
        card.add(sectionTitle("🎯  Résultat"));
        card.add(Box.createVerticalStrut(8));

        resultPlaceholder = new JPanel();
        resultPlaceholder.setLayout(new BoxLayout(resultPlaceholder, BoxLayout.Y_AXIS));
        resultPlaceholder.setBackground(Color.WHITE);
        JLabel ph = new JLabel("🔮");
        ph.setFont(new Font("SansSerif", Font.PLAIN, 40));
        ph.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel ph2 = new JLabel("En attente d'une image...");
        ph2.setFont(new Font("SansSerif", Font.PLAIN, 13));
        ph2.setForeground(new Color(160, 160, 160));
        ph2.setAlignmentX(Component.CENTER_ALIGNMENT);
        resultPlaceholder.add(Box.createVerticalStrut(20));
        resultPlaceholder.add(ph);
        resultPlaceholder.add(ph2);
        card.add(resultPlaceholder);

        resultCard = new JPanel();
        resultCard.setLayout(new BoxLayout(resultCard, BoxLayout.Y_AXIS));
        resultCard.setBackground(Color.WHITE);
        resultCard.setVisible(false);

        resultEmoji = new JLabel("🐱");
        resultEmoji.setFont(new Font("SansSerif", Font.PLAIN, 44));
        resultEmoji.setAlignmentX(Component.CENTER_ALIGNMENT);
        resultCard.add(resultEmoji);

        resultLabel = new JLabel("CHAT");
        resultLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        resultLabel.setForeground(CLR_CHAT);
        resultLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        resultCard.add(resultLabel);

        lowConfLabel = new JLabel("⚠️ Confiance insuffisante");
        lowConfLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lowConfLabel.setForeground(new Color(200, 150, 0));
        lowConfLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        lowConfLabel.setVisible(false);
        resultCard.add(lowConfLabel);
        resultCard.add(Box.createVerticalStrut(12));

        JPanel cHdr = new JPanel(new BorderLayout());
        cHdr.setBackground(Color.WHITE);
        JLabel cL = new JLabel("Confiance");
        cL.setFont(new Font("SansSerif", Font.PLAIN, 11));
        cL.setForeground(new Color(120, 120, 120));
        confidencePercent = new JLabel("0%");
        confidencePercent.setFont(new Font("SansSerif", Font.BOLD, 11));
        cHdr.add(cL, BorderLayout.WEST);
        cHdr.add(confidencePercent, BorderLayout.EAST);
        resultCard.add(cHdr);
        confidenceBar = makeBar(new Color(76, 175, 80));
        resultCard.add(confidenceBar);
        resultCard.add(Box.createVerticalStrut(12));

        JPanel grid = new JPanel(new GridLayout(3, 1, 0, 8));
        grid.setBackground(Color.WHITE);
        grid.setMaximumSize(new Dimension(380, 100));

        JPanel cP = statBarRow("🐱 Chat", new Color(74, 114, 184));
        catBar = (JProgressBar) cP.getComponent(1);
        catPercent = (JLabel) cP.getComponent(2);
        grid.add(cP);

        JPanel dP = statBarRow("🐶 Chien", new Color(26, 58, 140));
        dogBar = (JProgressBar) dP.getComponent(1);
        dogPercent = (JLabel) dP.getComponent(2);
        grid.add(dP);

        JPanel wP = statBarRow("🦁 Wild", new Color(124, 92, 46));
        wildBar = (JProgressBar) wP.getComponent(1);
        wildPercent = (JLabel) wP.getComponent(2);
        grid.add(wP);

        resultCard.add(grid);
        card.add(resultCard);
        return card;
    }

    private JPanel buildStatsCard() {
        JPanel card = cardBase();
        card.setMaximumSize(new Dimension(400, 140));
        card.add(sectionTitle("📊  Session"));
        card.add(Box.createVerticalStrut(6));

        JPanel g = new JPanel(new GridLayout(2, 2, 8, 8));
        g.setBackground(Color.WHITE);

        statTotal  = statCard(g, "0", "Analyses");
        statChats  = statCard(g, "0", "🐱 Chats");
        statChiens = statCard(g, "0", "🐶 Chiens");
        statWild   = statCard(g, "0", "🦁 Wild");

        card.add(g);
        return card;
    }

    private JPanel buildLogsCard() {
        JPanel card = cardBase();
        card.setLayout(new BorderLayout(0, 6));
        card.setMaximumSize(new Dimension(400, 160));
        card.add(sectionTitle("📋  Logs"), BorderLayout.NORTH);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 10));
        logArea.setBackground(new Color(250, 250, 250));
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);

        JScrollPane sp = new JScrollPane(logArea);
        sp.setBorder(new LineBorder(new Color(230, 230, 230)));
        card.add(sp, BorderLayout.CENTER);

        return card;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  CHARGEMENT IMAGE
    // ═══════════════════════════════════════════════════════════════════

    private void loadImage(File file) {
        selectedFile = file;

        java.awt.Image raw = new ImageIcon(file.getAbsolutePath()).getImage();
        java.awt.Image scaled = raw.getScaledInstance(380, 360, java.awt.Image.SCALE_SMOOTH);
        previewLabel.setIcon(new ImageIcon(scaled));
        dropCardLayout.show(dropContentPanel, "PREVIEW");

        imageInfoName.setText(file.getName());
        int w = raw.getWidth(null), h = raw.getHeight(null);
        imageInfoSize.setText(w + " × " + h + " px");
        imageInfoWeight.setText(String.format("%.1f Ko", file.length() / 1024.0));
        imageInfoBox.setVisible(true);

        analyzeBtn.setEnabled(true);
        clearBtn.setEnabled(true);

        addLog("📂 Image chargée : " + file.getName());
    }

    // ═══════════════════════════════════════════════════════════════════
    //  ANALYSE
    // ═══════════════════════════════════════════════════════════════════

    private void doAnalyze() {
        if (selectedFile == null) return;
        if (neurones == null) {
            addLog("⚠️ Neurones non disponibles");
            return;
        }

        analyzeBtn.setEnabled(false);
        clearBtn.setEnabled(false);
        addLog("🔍 Analyse en cours...");

        new Thread(() -> {
            try {
                String path = selectedFile.getAbsolutePath();
                int typeReel = ChaineTraitImage.typeReel(path);

                Image imgObj = new Image(path, typeReel, ChaineTraitImage.NIVEAUX_DE_GRIS);
                float[] entrees = ChaineTraitImage.caracteristiques(imgObj);

                int predit = ChaineTraitImage.predictionType(neurones, entrees);

                float[] scores = new float[3];
                for (int i = 0; i < 3; i++) {
                    neurones[i].metAJour(entrees);
                    scores[i] = neurones[i].sortie();
                }

                float scoreMain = scores[predit];
                float maxS = Math.max(scores[0], Math.max(scores[1], scores[2]));
                double confiance = (maxS > 0) ? scoreMain / maxS : 0;
                boolean lowConf = confiance < SEUIL_CONFIANCE;

                SwingUtilities.invokeLater(() -> {
                    displayResult(predit, confiance, scores, lowConf);
                    analyzeBtn.setEnabled(true);
                    clearBtn.setEnabled(true);
                });

            } catch (Exception ex) {
                addLog("✗ Erreur IA : " + ex.getMessage());
                SwingUtilities.invokeLater(() -> {
                    analyzeBtn.setEnabled(true);
                    clearBtn.setEnabled(true);
                });
            }
        }).start();
    }

    private void displayResult(int type, double confiance, float[] scores, boolean lowConf) {
        String[] emojis = {"🐱", "🐶", "🦁"};
        String[] labels = {"Chat", "Chien", "Wild"};
        Color[]  colors = {CLR_CHAT, CLR_CHIEN, CLR_WILD};

        if (lowConf) {
            resultEmoji.setText("❓");
            resultLabel.setText("INDÉTERMINÉ");
            resultLabel.setForeground(CLR_UNKNOWN);
        } else {
            resultEmoji.setText(emojis[type]);
            resultLabel.setText(labels[type].toUpperCase());
            resultLabel.setForeground(colors[type]);
        }
        lowConfLabel.setVisible(lowConf);

        float maxS = Math.max(scores[0], Math.max(scores[1], scores[2]));
        float nC = (maxS > 0) ? scores[0] / maxS : 0;
        float nD = (maxS > 0) ? scores[1] / maxS : 0;
        float nW = (maxS > 0) ? scores[2] / maxS : 0;

        animateBar(confidenceBar, confidencePercent, (float) confiance);
        animateBar(catBar,  catPercent,  nC);
        animateBar(dogBar,  dogPercent,  nD);
        animateBar(wildBar, wildPercent, nW);

        resultPlaceholder.setVisible(false);
        resultCard.setVisible(true);

        sessionTotal++;
        if (!lowConf) {
            if (type == 0) sessionChats++;
            else if (type == 1) sessionChiens++;
            else sessionWild++;
        }
        statTotal.setText(String.valueOf(sessionTotal));
        statChats.setText(String.valueOf(sessionChats));
        statChiens.setText(String.valueOf(sessionChiens));
        statWild.setText(String.valueOf(sessionWild));

        addLog("━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        if (lowConf) addLog("⚠️  INDÉTERMINÉ — confiance trop faible");
        else addLog("✅ " + emojis[type] + " " + labels[type].toUpperCase());
        addLog(String.format("   🐱 Chat  : %.1f%%", nC * 100));
        addLog(String.format("   🐶 Chien : %.1f%%", nD * 100));
        addLog(String.format("   🦁 Wild  : %.1f%%", nW * 100));
        addLog(String.format("   Confiance : %.1f%%", confiance * 100));
    }

    // ═══════════════════════════════════════════════════════════════════
    //  EFFACER
    // ═══════════════════════════════════════════════════════════════════

    private void doClear() {
        selectedFile = null;
        previewLabel.setIcon(null);
        dropCardLayout.show(dropContentPanel, "HINT");
        imageInfoBox.setVisible(false);

        resultPlaceholder.setVisible(true);
        resultCard.setVisible(false);

        analyzeBtn.setEnabled(false);
        clearBtn.setEnabled(false);

        resetBar(confidenceBar, confidencePercent);
        resetBar(catBar, catPercent);
        resetBar(dogBar, dogPercent);
        resetBar(wildBar, wildPercent);

        addLog("🗑 Effacé — Prêt");
    }

    // ═══════════════════════════════════════════════════════════════════
    //  UTILITAIRES
    // ═══════════════════════════════════════════════════════════════════

    private void addLog(String msg) {
        String time = java.time.LocalTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
        String line = "[" + time + "] " + msg + "\n";
        if (logArea != null) SwingUtilities.invokeLater(() -> logArea.append(line));
    }

    private void resetUI() {
        analyzeBtn.setEnabled(false);
        clearBtn.setEnabled(false);
        resultCard.setVisible(false);
        resultPlaceholder.setVisible(true);
        imageInfoBox.setVisible(false);
    }

    private boolean isImageFile(File f) {
        String n = f.getName().toLowerCase();
        return n.endsWith(".jpg") || n.endsWith(".jpeg") || n.endsWith(".png") || n.endsWith(".bmp");
    }

    private static void animateBar(JProgressBar bar, JLabel pct, float target) {
        javax.swing.Timer t = new javax.swing.Timer(15, null);
        final float[] cur = {bar.getValue()};
        t.addActionListener(e -> {
            cur[0] += (target * 100 - cur[0]) * 0.12f;
            bar.setValue((int) cur[0]);
            pct.setText(String.format("%.1f%%", cur[0]));
            if (Math.abs(cur[0] - target * 100) < 1) {
                bar.setValue((int) (target * 100));
                pct.setText(String.format("%.1f%%", target * 100));
                ((javax.swing.Timer) e.getSource()).stop();
            }
        });
        t.start();
    }

    private static void resetBar(JProgressBar bar, JLabel pct) {
        bar.setValue(0);
        pct.setText("0%");
    }

    // ── Helpers graphiques ────────────────────────────────────────────

    private JPanel cardBase() {
        JPanel c = new JPanel();
        c.setBackground(Color.WHITE);
        c.setBorder(new CompoundBorder(
                new RoundedBorder(10, new Color(220, 225, 230)),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)));
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        return c;
    }

    private JLabel sectionTitle(String txt) {
        JLabel l = new JLabel(txt);
        l.setFont(new Font("SansSerif", Font.BOLD, 13));
        l.setForeground(new Color(33, 33, 33));
        return l;
    }

    private JLabel chipLabel(String txt) {
        JLabel l = new JLabel(txt);
        l.setFont(new Font("SansSerif", Font.PLAIN, 11));
        l.setForeground(new Color(80, 80, 80));
        l.setOpaque(true);
        l.setBackground(new Color(232, 238, 246));
        l.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
        return l;
    }

    private JButton actionBtn(String txt, Color bg) {
        JButton b = new JButton(txt);
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setForeground(Color.WHITE);
        b.setBackground(bg);
        b.setBorder(null);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(175, 44));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(bg.darker()); }
            public void mouseExited(MouseEvent e)  { b.setBackground(bg); }
        });
        return b;
    }

    private JProgressBar makeBar(Color fg) {
        JProgressBar b = new JProgressBar(0, 100);
        b.setValue(0);
        b.setStringPainted(false);
        b.setForeground(fg);
        b.setBackground(new Color(230, 230, 230));
        b.setPreferredSize(new Dimension(350, 11));
        b.setMaximumSize(new Dimension(350, 11));
        return b;
    }

    private JPanel statBarRow(String lbl, Color clr) {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setBackground(Color.WHITE);
        JLabel l = new JLabel(lbl);
        l.setFont(new Font("SansSerif", Font.PLAIN, 11));
        l.setPreferredSize(new Dimension(85, 20));
        JProgressBar bar = makeBar(clr);
        bar.setPreferredSize(new Dimension(200, 9));
        bar.setMaximumSize(new Dimension(200, 9));
        JLabel pct = new JLabel("0%");
        pct.setFont(new Font("SansSerif", Font.BOLD, 10));
        pct.setPreferredSize(new Dimension(40, 20));
        p.add(l, BorderLayout.WEST);
        p.add(bar, BorderLayout.CENTER);
        p.add(pct, BorderLayout.EAST);
        return p;
    }

    private JLabel statCard(JPanel grid, String val, String lbl) {
        JPanel c = new JPanel();
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        c.setBackground(new Color(245, 247, 250));
        c.setBorder(new RoundedBorder(8, new Color(220, 225, 230)));

        JLabel v = new JLabel(val);
        v.setFont(new Font("SansSerif", Font.BOLD, 17));
        v.setForeground(new Color(33, 150, 243));
        v.setAlignmentX(Component.CENTER_ALIGNMENT);
        c.add(v);

        JLabel n = new JLabel(lbl);
        n.setFont(new Font("SansSerif", Font.PLAIN, 10));
        n.setForeground(new Color(120, 120, 120));
        n.setAlignmentX(Component.CENTER_ALIGNMENT);
        c.add(n);

        grid.add(c);
        return v;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  RoundedBorder
    // ═══════════════════════════════════════════════════════════════════

    static class RoundedBorder extends AbstractBorder {
        private final int radius;
        private final Color color;

        RoundedBorder(int radius, Color color) {
            this.radius = radius;
            this.color = color;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1));
            g2.drawRoundRect(x, y, w - 1, h - 1, radius, radius);
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(radius / 2, radius / 2, radius / 2, radius / 2);
        }

        @Override
        public boolean isBorderOpaque() { return false; }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  POINT D'ENTRÉE (compatibilité avec ChaineTraitImage)
    // ═══════════════════════════════════════════════════════════════════

    private void ouvrirMiniJeu() {
    if (neurones == null) {
        JOptionPane.showMessageDialog(this,
                "⚠️ Les neurones ne sont pas chargés.",
                "Erreur", JOptionPane.WARNING_MESSAGE);
        return;
    }

    List<String> chemins = cheminsImages;

    if (chemins == null || chemins.isEmpty()) {
        chemins = new ArrayList<>();
        File testDir = new File("src/main/dataset_groupe_9/test");
        if (!testDir.exists()) testDir = new File("dataset_groupe_9/test");
        if (!testDir.exists()) testDir = new File("test");

        if (testDir.exists()) {
            collectAllImages(testDir, chemins);
        }
    }

    if (chemins.isEmpty()) {
        JOptionPane.showMessageDialog(this,
                "⚠️ Aucune image trouvée pour le mini-jeu.\n" +
                "Vérifiez que le dossier de test existe.",
                "Erreur", JOptionPane.WARNING_MESSAGE);
        addLog("⚠️ Aucune image trouvée pour le mini-jeu.");
        return;
    }

    addLog("Ouverture du Mini-Jeu (" + chemins.size() + " images)");
    MiniJeu.start(chemins, neurones);
}

private static void collectAllImages(File dir, List<String> out) {
    File[] files = dir.listFiles();
    if (files == null) return;
    for (File f : files) {
        if (f.isDirectory()) collectAllImages(f, out);
        else {
            String n = f.getName().toLowerCase();
            if (n.endsWith(".jpg") || n.endsWith(".jpeg") || n.endsWith(".png") || n.endsWith(".bmp")) {
                out.add(f.getAbsolutePath());
            }
        }
    }
}

private void ouvrirCaptcha() {
    if (neurones == null) {
        JOptionPane.showMessageDialog(this,
                "Les neurones ne sont pas charges.",
                "Erreur", JOptionPane.WARNING_MESSAGE);
        return;
    }

    List<String> chemins = cheminsImages;

    if (chemins == null || chemins.isEmpty()) {
        chemins = new ArrayList<>();

        String[] cheminsPossibles = {
            "src/main/dataset_groupe_9/test",
            "dataset_groupe_9/test",
            "test",
            new File(".").getAbsolutePath() + "/test"
        };

        for (String path : cheminsPossibles) {
            File testDir = new File(path);
            if (testDir.exists()) {
                collectAllImages(testDir, chemins);
                break;
            }
        }
    }

    if (chemins == null || chemins.size() < 9) {
        JOptionPane.showMessageDialog(this,
                "Pas assez d'images pour le CAPTCHA (minimum 9).\n" +
                "Images trouvees : " + (chemins != null ? chemins.size() : 0),
                "Erreur", JOptionPane.WARNING_MESSAGE);
        addLog("Pas assez d'images pour le CAPTCHA.");
        return;
    }

    addLog("Ouverture du CAPTCHA (" + chemins.size() + " images)");
    CaptchaFrame.start(chemins, neurones);
}

    public static void start(List<String> cheminsTest, iNeurone[] neurones) {
    SwingUtilities.invokeLater(() -> new UserInterface(neurones, cheminsTest).setVisible(true));
}
}