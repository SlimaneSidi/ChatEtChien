# ChatEtChien — Réseau de Neurones 

---

<p align="center">
  <img src="https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white" alt="Java">
  <img src="https://img.shields.io/badge/ISEN-M%C3%A9diterran%C3%A9e-red?style=for-the-badge" alt="ISEN">
  <img src="https://img.shields.io/badge/THS-Traitement%20du%20Signal-8A2BE2?style=for-the-badge" alt="THS">
  <img src="https://img.shields.io/badge/Version-1.0.0-blue?style=for-the-badge" alt="Version">
</p>

## Présentation Générale

**ChatEtChien** est un projet transversal combinant **Intelligence Artificielle, Java et Traitement du Signal (THS)**, réalisé dans le cadre du cycle ingénieur à l'**ISEN Méditerranée**.

L'objectif principal de ce projet est de concevoir, implémenter et entraîner un **réseau de neurones artificiels (perceptron) entièrement. Le modèle est développé pour classifier et différencier de manière autonome des images de chats, de chiens et de wild.

---

## Équipe Projet — Groupe 9

Une collaboration menée de front par :
* **Icham OULALI**
* **Robin NULLANS**
* **Guillaume BEDMAR**
* **Benjamin L'HOMME**
* **Maelys BERNARD**

---

## Fonctionnalités & Concepts Clés

Le projet s'articule autour de quatre grands axes techniques et scientifiques :

### 1. Traitement du Signal & Prétraitement d'Images
* **Lecture des flux d'images** brutes au format matriciel.
* **Conversion en niveaux de gris** pour s'affranchir de la complexité des canaux RVB superflus et se focaliser sur l'intensité lumineuse.
* **Mise à plat (*Flattening*)** : Transformation d'une image bidimensionnelle en un vecteur de caractéristiques unidimensionnel (1D) exploitable par la couche d'entrée.
* **Normalisation des amplitudes** : Ajustement des valeurs des pixels (0-255) dans un intervalle optimisé afin d'éviter la saturation des fonctions d'activation et de stabiliser le calcul du gradient.

### 2. Architecture Neuronale & Programmation Orientée Objet
* Modélisation rigoureuse en Java s'appuyant sur les meilleurs paradigmes de la POO.
* Utilisation d'interfaces strictes (`iNeurone`) et de classes abstraites (`Neurone`) garantissant la modularité, la lisibilité et la maintenance du code.
* Conception d'un modèle extensible permettant de faire évoluer facilement l'architecture algorithmique (par exemple, l'ajout de nouvelles fonctions ou structures).

### 3. Fonctions d'Activation & Mathématiques
Étude comparative de la convergence et des performances à travers plusieurs fonctions mathématiques d'activation :
- **Heaviside (Seuil)** : Approche logique et binaire classique.
- **Sigmoïde** : Modélisation continue, non linéaire et différentiable, idéale pour l'analyse des probabilités de classe.
- **ReLU (Rectified Linear Unit)** : Optimisation de l'apprentissage profond en évitant le phénomène de disparition du gradient.

### 4. Algorithme d'Apprentissage & Analyse Scientifique
* Implémentation complète de la **descente de gradient** rétroactive.
* Calcul de l'**Erreur Quadratique Moyenne (MSE)** à chaque époque pour monitorer précisément la courbe d'apprentissage.
* Ajustement dynamique et itératif des **poids synaptiques** et du **biais** du réseau.
* **Étude de robustesse** : Évaluation approfondie du comportement et de la tolérance de l'algorithme face à l'introduction de bruit aléatoire (bruit blanc ou gaussien) dans les signaux d'entrée.

---

## Structure du Dépôt

L'arborescence du projet est structurée de manière claire pour isoler le code source, les données d'entraînement et les livrables d'analyse :

```text
├── Support/                       # Code source principal de l'application
│   ├── neurone/                  # Package contenant le cœur de l'architecture neuronale
│   │   ├── Neurone.java          # Classe abstraite de base du neurone
│   │   ├── iNeurone.java         # Interface définissant le comportement d'un neurone
│   │   └── NeuroneReLU.java      # Implémentation spécifique avec activation ReLU
│   └── ChaineTraitImage.java     # Point d'entrée principal / Routine d'entraînement et de test
├── Rapport/                       # Documentations et analyses des performances
│   └── ...                       # Comptes-rendus des expérimentations et graphiques de convergence
├── dataset_groupe_9/              # Échantillon de données (Train & Test)
│   └── ...                       # Images extraites et adaptées du dataset Kaggle Animal Faces
└── Projet IA_Java_Signal 2026-06-01.pdf # Cahier des charges et directives officielles


## Structure du Dépôt
* `Support/` : Contient les classes de base du perceptron (`Neurone.java`, `iNeurone.java`, etc.).
* `Rapport/` : Dossier contenant nos expérimentations, notre analyse de la convergence des modèles et nos conclusions scientifiques.
* `dataset_groupe_9/` : Sous-ensemble d'images d'entraînement et de test issu du dataset Kaggle Animal Faces.
* `Projet IA_Java_Signal 2026-06-01.pdf` : Cahier des charges et directives du projet.

## Exécution
Pour compiler et exécuter le projet depuis le dossier Support:
```bash
# Compilation des fichiers Java
javac neurone/*.java *.java      

# Lancement de la routine d'entraînement et de test
java -cp .:neurone ChaineTraitImage
