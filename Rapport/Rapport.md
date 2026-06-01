# Rapport Technique : Expérimentations sur les Réseaux de Neurones

---

## 1. Test des Neurones sur Fonctions Logiques

### 1.1 Apprentissage de la Porte Logique ET

#### A. Fonction d'activation : Heaviside

> **Problématique :** En effectuant des statistiques sur les valeurs des poids trouvées, ces valeurs sont-elles similaires ? Comment le justifier ?

**Analyse :** Les valeurs obtenues ne sont **pas similaires**. Le perceptron Heaviside cherche simplement un hyperplan séparateur valide, et non un hyperplan optimal. Comme il existe une infinité de solutions valides (toute droite passant entre le point $(1,1)$ et les trois autres points de la table de vérité), l'algorithme s'arrête sur la première configuration fonctionnelle rencontrée au hasard de l'initialisation aléatoire. 

#### B. Fonction d'activation : Sigmoïde

**Analyse :** Contrairement au neurone Heaviside, la sigmoïde converge vers une **solution unique**, totalement indépendante de l'initialisation aléatoire. 
Avec une fonction sigmoïde, le gradient ($\delta$) ne s'annule jamais brutalement. L'apprentissage continue d'affiner les poids vers une solution "centrée" et optimale. 

*Comparaison des résultats (Contraste Heaviside / Sigmoïde) :*
* **Heaviside (Essai 1) :** Poids dispersés ($0.00017,\ 0.78,\ -0.78$) après 5914 itérations.
* **Heaviside (Essai 2) :** Poids dispersés ($0.00024,\ 0.00010,\ -0.0003$) après quelques itérations.
* **Sigmoïde (Sur 15 essais) :** Solution constante à la 4ème décimale près ($w_1 = 6.4835,\ w_2 = 6.4835,\ b = -9.9001$).

**Explication mathématique :** Heaviside cherche n'importe quel hyperplan qui sépare les données. La sigmoïde minimise une erreur quadratique moyenne (MSE) continue et convexe sur des données séparables linéairement. La continuité et la dérivabilité de la sigmoïde transforment un problème à solutions multiples en un problème d'optimisation convexe possédant un minimum unique. Quelle que soit l'initialisation, la descente de gradient finit toujours dans le même puits.

#### C. Fonction d'activation : ReLU

Le comportement du neurone ReLU se distingue par plusieurs caractéristiques :
1. **Vitesse de convergence :** ReLU converge environ 15 fois plus vite que la Sigmoïde. Pour une somme pondérée $s > 0$, la dérivée de ReLU vaut $1$. Le gradient passe à "pleine puissance", ce qui accélère drastiquement l'apprentissage.
2. **Compacité de la solution :** Les poids obtenus sont environ 7 fois plus petits que pour la sigmoïde, et le biais 11 fois plus petit. La sigmoïde nécessite de grands poids pour "saturer" sa sortie près de 0 ou 1, tandis que ReLU est linéaire dans sa zone positive et cible directement les valeurs voulues.
3. **Absence de borne supérieure :** La sortie de ReLU n'est pas bornée à 1. Si ce comportement fonctionne par chance sur la porte ET, il nécessite une vigilance particulière sur des données plus complexes (comme les images), où les sorties peuvent dépasser $1.0$.
4. **Le piège du "Dying ReLU" :** Si l'initialisation aléatoire génère un biais très négatif empêchant la somme pondérée de devenir positive pour les entrées d'entraînement, la sortie reste figée à $0$. Le gradient s'annule définitivement et le neurone "meurt", produisant des résultats catastrophiques.

---

### 1.2 Apprentissage de la Porte Logique OU

#### A. Fonction d'activation : Heaviside
Le réseau parvient à apprendre, mais propose une solution dégénérée presque équivalente à $\text{sortie} = x_1$. La dépendance à $x_2$ ne tient qu'au signe d'un nombre minuscule. L'ajout de bruit sur les entrées ferait immédiatement s'effondrer les prédictions de ce modèle.

#### B. Fonction d'activation : ReLU
**Échec (Phénomène du "Dying ReLU") :** L'apprentissage stagne définitivement à une erreur (MSE) de $0.0625$. Le biais devenant trop négatif, la somme pondérée passe sous zéro : la fonction ReLU renvoie alors strictement $0$, sa dérivée s'annule, tout comme le gradient. Le neurone cesse d'apprendre.

#### C. Fonction d'activation : Sigmoïde
Le modèle démontre une optimisation parfaite :
1. **Convergence symétrique :** Contrairement au comportement erratique de Heaviside, la sigmoïde trouve une solution où les deux synapses sont équilibrées ($w_1 = 6.64,\ w_2 = 6.63$). Cette symétrie est la traduction mathématique exacte de la logique OU (les entrées A et B ont strictement le même poids décisionnel).
2. **Seuil adaptatif (Biais) :** Le biais se fixe à $-2.84$ (contre $-9.90$ pour la porte ET). Conséquence : une seule entrée active ($1 \times 6.64$) suffit amplement à surmonter ce seuil négatif ($6.64 - 2.84 = +3.80$) pour que la sortie approche $1.0$.
3. **Robustesse au bruit :** Grâce à cet équilibre entre les poids et le seuil, la progressivité de la courbe en "S" absorbe les signaux d'entrée imparfaits ou bruités, sans faire basculer brutalement la prédiction comme le ferait Heaviside.

---

## 2. Traitement des Images et Normalisation

Avant de transmettre le jeu de données des images au réseau de neurones, une étape de conditionnement des signaux est indispensable.

1. **Problématique des données brutes (Le Signal) :** Les images fournies au réseau de neurones sont constituées de pixels dont l'amplitude brute varie de 0 à 255. Ces valeurs possèdent une dynamique trop élevée pour notre algorithme. L'objectif de cette étape est de normaliser les amplitudes des pixels avant de les envoyer au neurone.
   
2. **Le piège de la saturation (Vanishing Gradient) :** L'injection de valeurs brutes non normalisées (ex: 200, 150, 255) dans un neurone utilisant une fonction d'activation sigmoïde provoque l'explosion de la somme pondérée interne. Pour des valeurs extrêmes ($x > 5$ ou $x < -5$), la courbe de la sigmoïde s'aplatit totalement. La sortie sature à $1.0$, la dérivée devient quasi nulle, et le réseau se fige. Les poids synaptiques ne sont plus mis à jour.
   
3. **Solution mise en œuvre :** La normalisation appliquée consiste à diviser la valeur de chaque pixel par $255.0$. Cette opération ramène l'intégralité des signaux d'entrée dans un intervalle strictement compris entre $0.0$ et $1.0$. Cela garantit que la somme pondérée reste concentrée dans la zone linéaire (sensible) de la fonction d'activation, assurant ainsi une convergence stable de l'algorithme.