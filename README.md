# 🐱🐶 ChatEtChien — Réseau de Neurones From Scratch en Java

---

<p align="center">
  <img src="https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white" alt="Java">
  <img src="https://img.shields.io/badge/ISEN-M%C3%A9diterran%C3%A9e-red?style=for-the-badge" alt="ISEN">
  <img src="https://img.shields.io/badge/THS-Traitement%20du%20Signal-8A2BE2?style=for-the-badge" alt="THS">
  <img src="https://img.shields.io/badge/Version-1.0.0-blue?style=for-the-badge" alt="Version">
</p>

## 📋 Présentation Générale

**ChatEtChien** est un projet transversal combinant **Intelligence Artificielle, Programmation Orientée Objet (Java) et Traitement du Signal (THS)**, réalisé dans le cadre du cycle ingénieur à l'**ISEN Méditerranée**.

L'objectif principal de ce projet est de concevoir, implémenter et entraîner un **réseau de neurones artificiels (perceptron) entièrement *from scratch***, c'est-à-dire sans recours à des bibliothèques de Machine Learning externes (telles que TensorFlow, PyTorch ou Weka). 

Ce dépôt comprend notre implémentation qui permet de classifier et différencier de manière autonome des images de **chats**, de **chiens**, mais également — dans le cadre de nos extensions de recherche — d'**animaux sauvages (wild)**.

---

## 👥 Équipe Projet — Groupe 9

Une collaboration menée de front par :
* **Icham OULALI**
* **Robin NULLANS**
* **Guillaume BEDMAR**
* **Benjamin L'HOMME**
* **Maelys BERNARD**

---

## 🛠️ Fonctionnalités & Concepts Clés

Le projet s'articule autour de quatre grands axes techniques et scientifiques :

### 1. Traitement du Signal & Prétraitement d'Images
* **Lecture des flux d'images** brutes au format matriciel via la classe `Image.java`.
* **Conversion en niveaux de gris** pour s'affranchir de la complexité des canaux RVB superflus et se focaliser sur l'intensité lumineuse.
* **Mise à plat (*Flattening*)** : Transformation d'une image bidimensionnelle en un vecteur de caractéristiques unidimensionnel (1D) exploitable par la couche d'entrée.
* **Normalisation des amplitudes** : Ajustement des valeurs des pixels dans un intervalle optimisé afin d'éviter la saturation des fonctions d'activation et de stabiliser le calcul du gradient.

### 2. Architecture Neuronale & Programmation Orientée Objet
* Modélisation rigoureuse en Java s'appuyant sur les meilleurs paradigmes de la POO.
* Utilisation d'interfaces strictes (`iNeurone`) et de classes abstraites (`Neurone`) garantissant la modularité, la lisibilité et la maintenance du code.
* Conception d'un modèle extensible permettant de faire évoluer facilement l'architecture algorithmique.

### 3. Fonctions d'Activation & Mathématiques
Étude comparative de la convergence et des performances à travers plusieurs fonctions mathématiques d'activation implémentées :
* 🛑 **Heaviside (Seuil)** (`NeuroneHeavyside.java`) : Approche logique et binaire classique.
* 📈 **Sigmoïde** (`NeuroneSigmoide.java`) : Modélisation continue, non linéaire et différentiable, idéale pour l'analyse des probabilités de classe.
* ⚡ **ReLU (Rectified Linear Unit)** (`NeuroneReLU.java`) : Optimisation de l'apprentissage profond en évitant le phénomène de disparition du gradient.

### 4. Algorithme d'Apprentissage & Analyse Scientifique
* Implémentation complète de la **descente de gradient** rétroactive via un apprentissage supervisé.
* Calcul de l'**Erreur Quadratique Moyenne (MSE)** à chaque époque pour monitorer précisément la courbe d'apprentissage.
* **Étude de robustesse** : Évaluation approfondie du comportement et de la tolérance de l'algorithme.
* Génération automatisée de graphiques de résultats via un script Python (`generer_figures.py`).

### 🌟 Bonus & Extensions Implémentées
* **Interface Graphique Utilisateur (GUI) :** Développement d'une interface interactive en Java Swing (`UserInterface.java`) permettant la classification visuelle en temps réel des images.
* **Extraction de caractéristiques (HOG) :** Utilisation d'algorithmes de détection (`HOG.java`) pour optimiser l'identification des formes.
* **Classification multi-classes :** Extension de l'apprentissage pour inclure une troisième catégorie : les animaux sauvages (`wild`).

---

## 📂 Structure du Dépôt

L'arborescence du projet est structurée de manière claire pour isoler le code source, les données d'entraînement et les livrables d'analyse :

```text
├── Support/                       # Code source principal de l'application (Java)
│   ├── neurone/                  # Package contenant le cœur de l'architecture neuronale
│   │   ├── Neurone.java          # Classe abstraite de base du neurone
│   │   ├── iNeurone.java         # Interface définissant le comportement d'un neurone
│   │   ├── NeuroneHeavyside.java # Implémentation de la fonction d'activation de Heaviside
│   │   ├── NeuroneReLU.java      # Implémentation de la fonction d'activation ReLU
│   │   └── NeuroneSigmoide.java  # Implémentation de la fonction d'activation Sigmoïde
│   ├── ChaineTraitImage.java     # Script d'apprentissage et de test sur les images
│   ├── UserInterface.java        # Interface Graphique (GUI) en Java Swing
│   ├── Image.java                # Traitement et mise à plat des images
│   └── HOG.java                  # Algorithme d'extraction des caractéristiques (Histogram of Oriented Gradients)
├── dataset_groupe_9/              # Échantillons de données
│   ├── train/                    # Images d'entraînement (chat, chien, wild)
│   └── test/                     # Images de test
├── modeles/                       # Fichiers de sauvegarde des poids synaptiques
│   ├── modele_chat.txt
│   ├── modele_chien.txt
│   └── modele_wild.txt
├── Rapport/                       # Documentations et analyses des performances
│   ├── figures/                  # Graphiques et exports visuels
│   ├── generer_figures.py        # Script Python d'automatisation des graphiques
│   ├── Rapport.md                # Rédaction des expérimentations scientifiques
│   └── Results.md                # Analyse détaillée des performances
└── Projet IA_Java_Signal 2026-06-01.pdf # Cahier des charges et directives officielles du projet
```

---

## 🚀 Exécution et Compilation

Pour compiler et exécuter le projet, placez-vous d'abord dans le dossier `Support` contenant les sources :

```bash
cd Support
```

**1. Compilation des fichiers Java :**
Compilez l'ensemble des packages et des classes Java nécessaires à la chaîne de traitement (incluant l'interface graphique).
```bash
javac neurone/*.java *.java
```

**2. Lancement de la routine d'entraînement ou de l'interface :**

Si vous souhaitez lancer l'interface graphique de détection :
```bash
java -cp .:neurone UserInterface
```

Si vous souhaitez exécuter la chaîne d'apprentissage classique :
```bash
java -cp .:neurone ChaineTraitImage
```
*(Note pour les utilisateurs Windows : remplacez le deux-points par un point-virgule pour le classpath : `java -cp .;neurone ChaineTraitImage`)*

---

## 📊 Méthodologie & Analyses

Le projet s'accompagne d'une démarche scientifique rigoureuse documentée dans le dossier `Rapport/`, étudiant notamment :
1. **L'impact du taux d'apprentissage (*Learning Rate*)** sur la stabilité et la vitesse de convergence du modèle.
2. **L'analyse comparative des fonctions d'activation** (Heaviside vs Sigmoïde vs ReLU) pour identifier le compromis optimal entre coût de calcul et précision.
3. **L'efficacité des algorithmes HOG** pour la différenciation complexe entre animaux domestiques (chiens/chats) et sauvages.- **ReLU (Rectified Linear Unit)** : Optimisation de l'apprentissage profond en évitant le phénomène de disparition du gradient.

### 4. Algorithme d'Apprentissage & Analyse Scientifique
* Implémentation complète de la **descente de gradient** rétroactive.
* Calcul de l'**Erreur Quadratique Moyenne (MSE)** à chaque époque pour monitorer précisément la courbe d'apprentissage.
* Ajustement dynamique et itératif des **poids synaptiques** et du **biais** du réseau.
* **Étude de robustesse** : Évaluation approfondie du comportement et de la tolérance de l'algorithme face à l'introduction de bruit aléatoire (bruit blanc ou gaussien) dans les signaux d'entrée.
