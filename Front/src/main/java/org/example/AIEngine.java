package org.example;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import org.example.neurone.NeuroneSigmoide;
import org.example.neurone.iNeurone;


 //Pont entre le front JavaFX et le back neurones.
 //Les modèles sont cherchés dans les ressources JAR sous /models/.

public class AIEngine {

    // ──────────────────────────────────────────────────────────────
    //  Singleton
    // ──────────────────────────────────────────────────────────────
    private static AIEngine instance;

    public static AIEngine getInstance() {
        if (instance == null) instance = new AIEngine();
        return instance;
    }

    // ──────────────────────────────────────────────────────────────
    //  Constantes
    // ──────────────────────────────────────────────────────────────
    private static final int TARGET_W = 64;
    private static final int TARGET_H = 64;

    public static final String[] TYPE_LABELS = {"chat", "chien", "wild"};
    public static final String[] TYPE_EMOJIS = {"🐱", "🐶", "🦁"};

    // ──────────────────────────────────────────────────────────────
    //  3 neurones sigmoïdes (un par classe)
    //  On utilise directement NeuroneSigmoide du back.
    // ──────────────────────────────────────────────────────────────
    private final iNeurone[] neurones = new iNeurone[3];
    private boolean loaded = false;

    // ──────────────────────────────────────────────────────────────
    //  Chargement des modèles depuis les ressources
    // ──────────────────────────────────────────────────────────────
    private AIEngine() {
        int hogSize = Hog.taille(TARGET_W, TARGET_H); // 1764

        String[] modelPaths = {
                "/models/modele_chat.txt",
                "/models/modele_chien.txt",
                "/models/modele_wild.txt"
        };

        try {
            for (int k = 0; k < 3; k++) {
                neurones[k] = new NeuroneSigmoide(hogSize);

                InputStream is = getClass().getResourceAsStream(modelPaths[k]);
                if (is == null) {
                    System.err.println("[AIEngine] Modèle introuvable : " + modelPaths[k]);
                    return;
                }

                File tmp = File.createTempFile("modele_" + TYPE_LABELS[k], ".txt");
                tmp.deleteOnExit();
                try (OutputStream os = new FileOutputStream(tmp)) {
                    is.transferTo(os);
                }

                neurones[k].chargement(tmp.getAbsolutePath());
                System.out.println("[AIEngine] ✓ Neurone " + TYPE_LABELS[k]
                        + " chargé (" + hogSize + " poids)");
            }
            loaded = true;
            System.out.println("[AIEngine] ✅ Tous les neurones sont prêts.");
        } catch (Exception e) {
            System.err.println("[AIEngine] Erreur de chargement : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean isLoaded() { return loaded; }

    public iNeurone[] getNeurones() { return neurones; }

    // ──────────────────────────────────────────────────────────────
    //  Résultat d'une prédiction
    // ──────────────────────────────────────────────────────────────
    public static class PredictResult {
        public final int     type;    
        public final String  label;
        public final String  emoji;
        public final float   score;
        public final float[] scores;

        PredictResult(int type, float[] scores) {
            this.type   = type;
            this.label  = TYPE_LABELS[type];
            this.emoji  = TYPE_EMOJIS[type];
            this.score  = scores[type];
            this.scores = scores;
        }

        public float catScore()  { return scores[0]; }
        public float dogScore()  { return scores[1]; }
    }

    // ──────────────────────────────────────────────────────────────
    //  Prédiction depuis un File
    // ──────────────────────────────────────────────────────────────
    public PredictResult predict(File imageFile) throws IOException {
        if (!loaded) throw new IllegalStateException("Neurones non chargés");
        BufferedImage raw = ImageIO.read(imageFile);
        if (raw == null) throw new IOException("Impossible de lire : " + imageFile.getName());
        return predictFromBuffered(raw);
    }

    public PredictResult predict(BufferedImage img) {
        if (!loaded) throw new IllegalStateException("Neurones non chargés");
        return predictFromBuffered(img);
    }

    // ──────────────────────────────────────────────────────────────
    //  Pipeline interne : redimension → niveaux de gris → HOG → neurones
    // ──────────────────────────────────────────────────────────────
    private PredictResult predictFromBuffered(BufferedImage src) {

        BufferedImage resized = new BufferedImage(TARGET_W, TARGET_H, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(src, 0, 0, TARGET_W, TARGET_H, null);
        g.dispose();

        int[] gris = new int[TARGET_W * TARGET_H];
        for (int y = 0; y < TARGET_H; y++) {
            for (int x = 0; x < TARGET_W; x++) {
                int rgb = resized.getRGB(x, y);
                int r  = (rgb >> 16) & 0xFF;
                int gr = (rgb >>  8) & 0xFF;
                int b  =  rgb        & 0xFF;
                float gray = 0.2125f * r + 0.7154f * gr + 0.0721f * b;
                gris[y * TARGET_W + x] = (int) Math.min(255, Math.max(0, gray));
            }
        }

        float[] hog = Hog.calcule(gris, TARGET_W, TARGET_H);

        float[] scores = new float[3];
        for (int k = 0; k < 3; k++) {
            neurones[k].metAJour(hog);
            scores[k] = neurones[k].sortie();
        }

        int best = 0;
        for (int k = 1; k < 3; k++) if (scores[k] > scores[best]) best = k;

        return new PredictResult(best, scores);
    }
}