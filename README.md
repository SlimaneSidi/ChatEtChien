# 🐱🐶 ChatEtChien - Projet IA / Java / THS

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![ISEN](https://img.shields.io/badge/ISEN-Méditerranée-red?style=for-the-badge)
![THS](https://img.shields.io/badge/THS-Traitement_du_Signal-8A2BE2?style=for-the-badge)

**Équipe Projet (Groupe 9) :**
* Icham OULALI
* Robin NULLANS
* Guillaume BEDMAR
* Benjamin L'HOMME
* Maelys BERNARD

## Description du Projet
Ce dépôt contient le code source et le rapport de notre projet transversal **IA / Java / THS** réalisé en cycle ingénieur. 

L'objectif principal est de concevoir et d'entraîner un réseau de neurones artificiels **de zéro**, sans utiliser de bibliothèques de Machine Learning externes, afin de différencier des images de chiens et de chats.

## Fonctionnalités et Apprentissages
* **Traitement du Signal (Images) :** Lecture, conversion en niveaux de gris, mise à plat (flattening) et normalisation des amplitudes des pixels pour préparer les données brutes.
* **Architecture neuronale (POO) :** Implémentation complète d'un perceptron en Java via des interfaces (`iNeurone`) et des classes abstraites.
* **Fonctions d'activation :** Étude comparative et implémentation de plusieurs fonctions mathématiques :
  * *Heaviside* * *Sigmoïde*
  * *ReLU (Rectified Linear Unit)*
* **Algorithme d'apprentissage :** Descente de gradient, calcul de l'erreur quadratique moyenne (MSE), ajustement des poids synaptiques et du biais.
* **Analyse scientifique :** Évaluation de la robustesse des modèles face à l'introduction de bruit aléatoire dans les signaux d'entrée.

## Structure du Dépôt
* `Support/` : Contient les classes de base du perceptron (`Neurone.java`, `iNeurone.java`, etc.).
* `Rapport/` : Dossier contenant nos expérimentations, notre analyse de la convergence des modèles et nos conclusions scientifiques.
* `dataset_groupe_9/` : Sous-ensemble d'images d'entraînement et de test issu du dataset Kaggle Animal Faces.
* `Projet IA_Java_Signal 2026-06-01.pdf` : Cahier des charges et directives du projet.

## Exécution
Pour compiler et exécuter le projet d'entraînement :
```bash
# Compilation des fichiers Java
javac *.java 

# Lancement de la routine d'entraînement et de test
java [NOM DU FICHIER]
