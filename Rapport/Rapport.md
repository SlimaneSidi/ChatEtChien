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



# RGB

64.94 %. Qu'estce qui pourrait causer ça ? Que faudrait il ameliorer pour avoir
  une meilleure précision de choix ?

● C'est en fait un résultat normal — tu butes contre la limite d'expressivité d'un perceptron simple. Quelques constats avant les
  pistes.

  Observations sur tes 2 runs

  ┌────────────┬────────────┬───────┬───────────────┐
  │ MSE limite │ Itérations │ Temps │ Accuracy test │
  ├────────────┼────────────┼───────┼───────────────┤
  │ 0.10       │ 255        │ 15 s  │ 77.28 %       │
  ├────────────┼────────────┼───────┼───────────────┤
  │ 0.07       │ 9166       │ 561 s │ 75.66 % ↓     │
  └────────────┴────────────┴───────┴───────────────┘

  Plus tu pousses l'apprentissage, moins ton accuracy de test est bonne → c'est le signal classique du surapprentissage
  (overfitting). Le neurone mémorise des particularités du train qui ne se généralisent pas. Tu peux le mentionner dans le rapport,
   c'est un point scientifique très solide.

  Pourquoi on plafonne à ~77 %

  Un seul neurone = un seul hyperplan dans l'espace des pixels (4096 dimensions). Or "être un chat" n'est pas une fonction linéaire
   des valeurs de pixels :

  1. Invariance à la position : un chat à gauche et un chat à droite ont des vecteurs d'entrée complètement différents. Le neurone
  ne peut pas "savoir" que c'est le même objet déplacé.
  2. Invariance d'apparence : pose, race, fond, éclairage → tout change les valeurs des pixels alors que sémantiquement c'est
  toujours un chat.
  3. Grayscale jette la couleur : information utile pour distinguer chat / chien / wild.
  4. Pixel brut = mauvaise feature : la valeur du pixel (12, 30) n'a aucun sens conceptuel.

  Donc 77 % avec un neurone linéaire sur pixels bruts c'est en fait déjà honorable (12 points au-dessus de la baseline 65 %).

  Pistes d'amélioration (du plus simple au plus ambitieux)

  Toutes celles-ci sont dans le sujet (page 5 "Extensions") :

  1. Mieux normaliser — standardisation

  Au lieu de juste ÷255, soustrais la moyenne et divise par l'écart-type sur tout le dataset :
  pixel' = (pixel - moyenne_globale) / ecartType_global
  Ça centre les entrées autour de 0, ce qui aide énormément la descente de gradient à converger sur un meilleur optimum.

  2. Passer en RGB

  Tes neurones ont 4096 entrées en gris. En RGB tu en aurais 12 288 (3×4096). Plus de paramètres = plus de capacité à séparer, et
  tu gardes l'info couleur (très utile : les wild ont souvent des couleurs distinctes).

  3. Passer en TSL (HSV)

  Encore mieux que RGB pour la séparation animale : la teinte (H) est invariante à la luminosité, donc plus robuste aux variations
  d'éclairage.

  4. Égalisation d'histogramme

  Avant normalisation, redistribue les niveaux de gris pour saturer l'usage de la dynamique [0, 255]. Marche bien sur des photos de
   luminosité variable.

  5. Augmentation de données (miroir horizontal)

  Pour chaque image de train, ajoute aussi sa version miroir avec le même label. Tu doubles le jeu de train et tu apprends une
  légère invariance à la symétrie gauche/droite. Ça aide vraiment contre le surapprentissage que tu observes.

  6. FFT 2D

  Mentionnée explicitement dans le sujet ("technique surtout 1D"). Le module de la FFT 2D est invariant à la translation dans
  l'image. Donc un chat décalé donnerait le même spectre. Tu remplaces tes 4096 pixels par les 4096 modules FFT en entrée du
  neurone. À tester — peut donner un gain significatif.

  7. Early stopping (anti-overfitting)

  Garde de côté ~1000 images du train comme "validation". Mesure l'accuracy sur cette validation toutes les N itérations.
  Arrête-toi quand elle commence à baisser (même si la MSE train continue à descendre). C'est la méthode standard contre
  l'overfitting.

  Recommandation pour le temps qui reste

  Vu que tu rends demain matin, je suggérerais cet ordre :
  1. (rapide, gros effet) Standardisation au lieu de simple ÷255 → re-mesure
  2. (rapide, moyen effet) Augmentation miroir → re-mesure
  3. (moyen, gros effet) Passer en RGB → re-mesure
  4. (intéressant pour rapport) Tester sans mélange, sans normalisation → montrer la dégradation

  Chaque variante = une ligne dans un tableau comparatif qui fera très bonne impression dans le rapport :

  ┌───────────────────────────────┬───────────────┐
  │           Variante            │ Accuracy test │
  ├───────────────────────────────┼───────────────┤
  │ Baseline (toujours "autre")   │ 65 %          │
  ├───────────────────────────────┼───────────────┤
  │ Sigmoïde, gris, ÷255 (actuel) │ 77 %          │
  ├───────────────────────────────┼───────────────┤
  │ + standardisation             │ ?             │
  ├───────────────────────────────┼───────────────┤
  │ + miroir                      │ ?             │
  ├───────────────────────────────┼───────────────┤
  │ + RGB                         │ ?             │
  ├───────────────────────────────┼───────────────┤
  │ Sans normalisation            │ ?             │
  ├───────────────────────────────┼───────────────┤
  │ Sans mélange                  │ ?             │
  └───────────────────────────────┴───────────────┘


# FFT

## Principe

On remplace les pixels bruts par le **spectre de Fourier** de l'image. Le pipeline (classe `FFT.java`) procède ainsi :

1. Conversion de l'image en **niveaux de gris** (1 canal, 64×64).
2. **FFT 2D** via une transformée radix-2 Cooley-Tukey appliquée d'abord sur chaque ligne, puis sur chaque colonne (64 est une puissance de 2, donc aucun rembourrage n'est nécessaire). L'implémentation a été validée numériquement par comparaison directe avec `numpy.fft.fft2`.
3. Calcul de la **log-magnitude** $\log(1 + |F(u,v)|)$. Le passage au logarithme est indispensable : le coefficient continu (DC) est plusieurs ordres de grandeur au-dessus des hautes fréquences et écraserait sinon tout le reste du signal.
4. **Normalisation** dans $[0, 1]$ par le maximum, pour rester dans la zone sensible de la sigmoïde (même motivation que la section 2).

On obtient ainsi 4096 entrées (comme en niveaux de gris), mais qui décrivent le **contenu fréquentiel** de l'image plutôt que ses valeurs spatiales.

## Motivation théorique

L'intérêt attendu de la FFT est l'**invariance à la translation** : le module de la transformée de Fourier ne dépend pas de la position de l'objet dans l'image. Un chat à gauche et le même chat à droite produisent en théorie le même spectre de magnitude. C'était la faiblesse n°1 du neurone sur pixels bruts (cf. section RGB), et la FFT visait précisément à la corriger.

## Résultats mesurés

| Représentation | MSE limite | Itérations | Accuracy | Précision | Rappel |
|----------------|-----------|-----------|----------|-----------|--------|
| FFT log-mag    | 0.15      | 80        | 79.10 %  | 76.92 %   | 56.61 % |
| FFT log-mag    | 0.11      | 2014      | 84.05 %  | 86.48 %   | 63.92 % |
| RGB (référence)| 0.01      | 1678      | 95.72 %  | 89.53 %   | 99.25 % |

## Analyse : pourquoi la FFT déçoit ici

La FFT est **nettement en dessous du RGB** (~84 % contre ~96 %). Trois raisons principales :

1. **La phase est jetée.** On ne conserve que le *module* du spectre. Or, pour une image, c'est la **phase** qui porte l'essentiel de la structure spatiale (contours, position des formes) — un résultat classique du traitement du signal. En ne gardant que la magnitude, on obtient bien l'invariance à la translation, mais on perd l'information qui permet de reconnaître *la forme* d'un chat. On échange une bonne propriété contre une perte d'information plus coûteuse.

2. **Le rappel s'effondre (≈ 57–64 %).** C'est la signature la plus parlante : le neurone rate plus d'un tiers des chats. La précision reste correcte (il se trompe peu quand il dit « chat »), mais il est devenu très conservateur. Le spectre de magnitude décrit surtout des **textures/fréquences globales** communes à beaucoup d'animaux, donc le signal discriminant chat / non-chat est faible.

3. **Le passage en gris supprime la couleur.** La FFT est calculée sur l'image en niveaux de gris, alors que la couleur était justement un atout majeur du RGB (les *wild* ont des teintes distinctes). On cumule donc deux handicaps : perte de la couleur **et** perte de la phase.

## Conclusion

La FFT log-magnitude apporte bien l'invariance à la translation visée, mais au prix de la phase et de la couleur, deux informations qui se révèlent plus utiles pour ce problème. Pour un perceptron simple, **les pixels RGB bruts restent une meilleure représentation** que le spectre de magnitude. La FFT n'est pas un échec conceptuel — elle illustre concrètement le compromis *invariance ↔ pouvoir discriminant* — mais elle ne convient pas à ce classifieur. Des pistes pour la réhabiliter : conserver la phase, calculer le spectre par canal RGB, ou ne garder que les basses fréquences (couronne centrale) comme descripteur compact.