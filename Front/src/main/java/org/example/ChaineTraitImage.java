package org.example;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.example.neurone.Neurone;
import org.example.neurone.NeuroneSigmoide;
import org.example.neurone.iNeurone;

// 3 neurones sigmoides (1 par type)
// Chaine de traitement complete : lecture des images, extraction des
// caracteristiques HOG (Histogramme de Gradients Orientes), miroir horizontal,
// melange, apprentissage de 3 neurones.
// TYPE : 0 = chat, 1 = chien, 2 = wild
//
// javac neurone/*.java *.java
// java -cp ".;neurone" ChaineTraitImage

public class ChaineTraitImage {
    
    static final boolean NIVEAUX_DE_GRIS = true;
    static final String DIR_TRAIN = "../dataset_groupe_9/train";
    static final String DIR_TEST = "../dataset_groupe_9/test";
    static final float ETA = 0.001f;
    static final int NB_ITERATIONS = 500;
    static final long SEED = 676767L;
    static final String DIR_MODELE = "../modeles";
    static final boolean FORCER_ENTRAINEMENT = true;
    static final String[] TYPE = {"chat", "chien", "wild"};
    static final int NB_TYPE = TYPE.length;

    // Determine le type réel d'une image avec son dossier
    static int typeReel(String chemin) {
        if (chemin.contains("/cat/") || chemin.contains("\\cat\\")) return 0;
        if (chemin.contains("/dog/") || chemin.contains("\\dog\\")) return 1;
        return 2; // wild
    }

    // type predit = indice du neurone qui produit la plus grande sortie (argmax)
    static int predictionType(iNeurone[] neurones, float[] entrees) {
        int meilleur = 0;
        float maxSortie = Float.NEGATIVE_INFINITY;
        for (int k = 0; k < neurones.length; ++k) {
            neurones[k].metAJour(entrees);
            float s = neurones[k].sortie();
            if (s > maxSortie) { 
                maxSortie = s; 
                meilleur = k; 
            }
        }
        return meilleur;
    }

    // Chemin du fichier de sauvegarde du neurone d'un type
    static String fichierModele(int type) {
        return DIR_MODELE + "/modele_" + TYPE[type] + ".txt";
    }

    // si existent deja = true
    static boolean modelesExistent() {
        for (int k = 0; k < NB_TYPE; ++k)
            if (!new java.io.File(fichierModele(k)).exists()) return false;
        return true;
    }

    // Caracteristiques HOG d'une image (entrees du neurone). Point d'entree unique
    // partage avec l'UI pour garantir le meme pretraitement a l'apprentissage et au test.
    static float[] caracteristiques(Image im) {
        return Hog.calcule(im.donnees(), im.largeur(), im.hauteur());
    }

    // Miroir horizontal d'une image en niveaux de gris (colonne j -> largeur-1-j).
    // Sert d'augmentation de donnees : on recalcule ensuite le HOG sur l'image inversee.
    static int[] miroirGris(int[] src, int largeur, int hauteur) {
        int[] dst = new int[src.length];
        for (int i = 0; i < hauteur; ++i)
            for (int j = 0; j < largeur; ++j)
                dst[i * largeur + (largeur-1 - j)] = src[i * largeur + j];
        return dst;
    }

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║   CHAINE DE TRAITEMENT D'IMAGES - GROUPE 9         ║");
        System.out.println("╚══════════════════════════════════════════════════════╝\n");

        Neurone.fixeCoefApprentissage(ETA);
        iNeurone[] neurones = new iNeurone[NB_TYPE];
        final int tailleEntree;
        int totalIterations = 0;
        int nbImagesTrain = 0;

        boolean entrainer = FORCER_ENTRAINEMENT || !modelesExistent();

        // ═══════════════════════════════════════════════════════
        // PHASE 1 : ENTRAINEMENT OU CHARGEMENT
        // ═══════════════════════════════════════════════════════
        
        if (entrainer) {
            System.out.println("[1/4] 📂 Chargement de la base de données...");
            List<String> cheminsTrain = Image.listeFichiers(DIR_TRAIN);
            if (cheminsTrain == null || cheminsTrain.isEmpty()) {
                System.err.println("❌ Aucun fichier trouvé dans " + DIR_TRAIN);
                return;
            }

            Collections.shuffle(cheminsTrain, new Random(SEED));

            final int N = cheminsTrain.size();
            final int M = 2 * N;
            nbImagesTrain = M;
            float[][] entreesTrain = new float[M][];
            float[][] ciblesTrain = new float[NB_TYPE][M];
            int[] nbPartype = new int[NB_TYPE];
            
            long t0 = System.currentTimeMillis();
            for (int i = 0; i < N; ++i) {
                int type = typeReel(cheminsTrain.get(i));
                Image im = new Image(cheminsTrain.get(i), type, NIVEAUX_DE_GRIS);
                int[] gris = im.donnees();
                entreesTrain[i] = Hog.calcule(gris, im.largeur(), im.hauteur());
                
                int[] grisMiroir = miroirGris(gris, im.largeur(), im.hauteur());
                entreesTrain[N+i] = Hog.calcule(grisMiroir, im.largeur(), im.hauteur());
                
                for (int k = 0; k < NB_TYPE; ++k) {
                    float cible = (type == k) ? 1f : 0f;
                    ciblesTrain[k][i] = cible;
                    ciblesTrain[k][N+i] = cible;
                }
                nbPartype[type] += 2;
                
                if ((i+1) % 1000 == 0)
                    System.out.printf("    ⏳ %d / %d images chargées%n", i+1, N);
            }

            long dt = System.currentTimeMillis() - t0;
            System.out.printf("    ✓ %d images chargées (%d originales + %d miroirs) en %.1f s%n",
                    M, N, N, dt/1000.0);
            for (int k = 0; k < NB_TYPE; ++k)
                System.out.printf("      • %-5s : %d images%n", TYPE[k], nbPartype[k]);

            tailleEntree = entreesTrain[0].length;
            System.out.println("\n[2/4] 🧠 Création des 3 neurones sigmoïdes");
            System.out.printf("    → %d entrées (caractéristiques HOG)%n", tailleEntree);
            for (int k = 0; k < NB_TYPE; ++k)
                neurones[k] = new NeuroneSigmoide(tailleEntree);

            System.out.printf("\n[3/4] 🎓 Apprentissage (eta=%.4f, nb_iterations=%d)...%n",
                    ETA, NB_ITERATIONS);
            long tA = System.currentTimeMillis();
            for (int k = 0; k < NB_TYPE; ++k) {
                System.out.printf("   → Neurone %s%n", TYPE[k]);
                totalIterations += neurones[k].apprentissage(entreesTrain, ciblesTrain[k], NB_ITERATIONS);
            }
            long dtA = System.currentTimeMillis() - tA;
            System.out.printf("    ✓ Apprentissage terminé en %.1f s (%d iterations cumulées)%n",
                    dtA/1000.0, totalIterations);

            new java.io.File(DIR_MODELE).mkdirs();
            for (int k = 0; k < NB_TYPE; ++k)
                neurones[k].sauvegarde(fichierModele(k));
            
        } else {
            System.out.println("[1/4] 💾 Chargement des sauvegardes existantes");
            List<String> echantillon = Image.listeFichiers(DIR_TEST);
            if (echantillon == null || echantillon.isEmpty()) {
                System.err.println("❌ Aucun fichier trouvé dans " + DIR_TEST);
                return;
            }
            Image ref = new Image(echantillon.get(0), 0, NIVEAUX_DE_GRIS);
            tailleEntree = Hog.taille(ref.largeur(), ref.hauteur());
            System.out.printf("    → %d entrées attendues par neurone%n", tailleEntree);
            for (int k = 0; k < NB_TYPE; ++k) {
                neurones[k] = new NeuroneSigmoide(tailleEntree);
                neurones[k].chargement(fichierModele(k));
                System.out.printf("    ✓ Neurone %s chargé%n", TYPE[k]);
            }
        }

        // ═══════════════════════════════════════════════════════
        // PHASE 2 : EVALUATION
        // ═══════════════════════════════════════════════════════
        
        System.out.println("\n[4/4] 📊 Évaluation sur la base de test...");
        List<String> cheminsTest = Image.listeFichiers(DIR_TEST);
        int[][] conf = new int[NB_TYPE][NB_TYPE];
        
        int count = 0;
        for (String chemin : cheminsTest) {
            int reel = typeReel(chemin);
            Image im = new Image(chemin, reel, NIVEAUX_DE_GRIS);
            float[] e = caracteristiques(im);
            int predit = predictionType(neurones, e);
            conf[reel][predit]++;
            
            if ((++count) % 500 == 0)
                System.out.printf("    ⏳ %d images testées...%n", count);
        }

        int total = 0, bonnes = 0;
        for (int r = 0; r < NB_TYPE; ++r)
            for (int p = 0; p < NB_TYPE; ++p) {
                total += conf[r][p];
                if (r == p) bonnes += conf[r][p];
            }
        double moyenne = 100.0 * bonnes / total;

        // ═══════════════════════════════════════════════════════
        // AFFICHAGE DES RESULTATS
        // ═══════════════════════════════════════════════════════
        
        System.out.println("\n╔════════════════════════════════════════════════════╗");
        System.out.println("║                   RÉSULTATS                        ║");
        System.out.println("╚════════════════════════════════════════════════════╝");
        System.out.printf("Base de test : %d images%n%n", total);
        System.out.println("Matrice de confusion (lignes = réel, colonnes = prédit) :");
        System.out.printf("            %8s %8s %8s%n", TYPE[0], TYPE[1], TYPE[2]);
        for (int r = 0; r < NB_TYPE; ++r)
            System.out.printf("  %-8s %8d %8d %8d%n",
                    TYPE[r], conf[r][0], conf[r][1], conf[r][2]);

        double sommePrecision = 0, sommeRappel = 0;
        System.out.println("\nMétriques par type :");
        for (int k = 0; k < NB_TYPE; ++k) {
            int tp = conf[k][k];
            int predits = 0, reels = 0;
            for (int x = 0; x < NB_TYPE; ++x) {
                predits += conf[x][k];
                reels += conf[k][x];
            }
            double prec = predits == 0 ? 0 : 100.0 * tp / predits;
            double rapp = reels == 0 ? 0 : 100.0 * tp / reels;
            sommePrecision += prec;
            sommeRappel += rapp;
            System.out.printf("  %-5s  précision=%.2f %%  rappel=%.2f %%%n",
                    TYPE[k], prec, rapp);
        }
        
        double precision = sommePrecision / NB_TYPE;
        double rappel = sommeRappel / NB_TYPE;
        
        System.out.println("\n" + "─".repeat(52));
        System.out.printf("Moyenne générale    : %.2f %%%n", moyenne);
        System.out.printf("Précision générale  : %.2f %%%n", precision);
        System.out.printf("Rappel général      : %.2f %%%n", rappel);
        System.out.println("═".repeat(52) + "\n");

        // Sauvegarde des résultats
        String type = "HOG Miroir";
        String neuroneInfo = "NeuroneSigmoide x" + NB_TYPE;
        enregistreResultats(type, neuroneInfo, nbImagesTrain, totalIterations, ETA,
                moyenne, precision, rappel);

        // ═══════════════════════════════════════════════════════
        // PHASE 3 : LANCEMENT DE L'INTERFACE JAVAFX
        // ═══════════════════════════════════════════════════════
        
        System.out.println("🚀 Lancement de l'interface graphique...\n");
        javafx.application.Application.launch(MainApp.class, args);
    }

    static void enregistreResultats(String type, String neurone, int nbImagesTrain,
                                    int iterations, float eta,
                                    double moyenne, double precision, double rappel) {
        final String chemin = "../Rapport/Results.md";
        final String entete =
                "# Résultats des exécutions\n\n" +
                "| id | date | type | neurone | images_train | iterations | eta | moyenne (%) | precision (%) | rappel (%) |\n" +
                "|----|------|------|---------|--------------|------------|-----|-------------|---------------|------------|\n";
        try {
            java.io.File f = new java.io.File(chemin);
            f.getParentFile().mkdirs();
            int prochainId = 1;
            boolean entetePresente = false;
            
            if (f.exists() && f.length() > 0) {
                for (String ligne : java.nio.file.Files.readAllLines(f.toPath())) {
                    String t = ligne.trim();
                    if (t.startsWith("| id ")) {
                        entetePresente = true;
                    } else if (t.startsWith("|")) {
                        String[] cols = t.split("\\|");
                        if (cols.length > 1) {
                            try {
                                int id = Integer.parseInt(cols[1].trim());
                                if (id >= prochainId) prochainId = id + 1;
                            } catch (NumberFormatException ignore) {}
                        }
                    }
                }
            }
            
            try (java.io.FileWriter w = new java.io.FileWriter(f, true)) {
                if (!entetePresente) w.write(entete);
                String date = java.time.LocalDateTime.now()
                        .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss dd-MM"));
                w.write(String.format(java.util.Locale.US,
                        "| %d | %s | %s | %s | %d | %d | %.4f | %.2f | %.2f | %.2f |%n",
                        prochainId, date, type, neurone, nbImagesTrain, iterations,
                        eta, moyenne, precision, rappel));
            }
            System.out.printf("✓ Résultats enregistrés dans %s (id=%d)%n", chemin, prochainId);
        } catch (java.io.IOException e) {
            System.err.println("❌ Impossible d'écrire les résultats dans " + chemin);
            e.printStackTrace();
        }
    }
}