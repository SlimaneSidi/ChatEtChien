# Rapport

## Test des neurones

### Porte Logique ET

#### Heaviside

"faire des statistiques sur les valeurs des poids trouvées : ces valeurs sont-elles similaires ? Pouvez-vous le justifier ?"

  Réponse à mettre dans le rapport : non, elles ne sont pas similaires, parce que le perceptron Heaviside cherche
  n'importe quel hyperplane séparateur, pas un hyperplane optimal. Il y a une infinité de solutions valides (toute
  droite passant entre (1,1) et les 3 autres points) et l'algo prend la première rencontrée à partir de l'init
  aléatoire. Ce que tu peux montrer graphiquement (papier ou matplotlib) en traçant les droites obtenues sur les 20-30
  runs.

Ce qui change avec Sigmoïde

  Avec une sigmoïde, delta ne s'annule jamais complètement → l'apprentissage continue à raffiner les poids vers une
  solution plus "centrée". Tu devrais y observer des poids plus stables d'un run à l'autre. Bon contraste pour le
  rapport.

 Contraste total avec Heaviside. Souviens-toi :
  - Heaviside run 1 → (0.00017, 0.78, −0.78) après 5914 itérations
  - Heaviside run 2 → (0.00024, 0.00010, −0.0003) après ~quelques itérations
  - Sigmoïde : toujours (6.4835, 6.4835, −9.9001) à 4 décimales près


#### Sigmoide

  → La sigmoïde converge vers une solution unique, indépendante de l'initialisation aléatoire. C'est LE point clé à
  mettre en exergue.

  Pourquoi

  Heaviside cherche n'importe quel hyperplane qui sépare → infinité de solutions, première trouvée gagne.

  Sigmoïde minimise une MSE continue et convexe (sur des données séparables linéairement) → un seul minimum. Quelle que
  soit l'init, la descente de gradient finit dans le même puits. L'init ne fait que changer le chemin pris, pas la
  destination.

Sur 15 essais indépendants, le neurone sigmoïde converge vers une solution unique (w₁ = w₂ = 6.48, b = −9.90), à la
  ▎  4ᵉ décimale près. À l'inverse, le neurone Heaviside produit des solutions dispersées (poids variant de 10⁻⁴ à 10⁰),
  ▎  reflétant l'infinité d'hyperplanes séparateurs valides. La continuité et la dérivabilité de la sigmoïde
  ▎ transforment un problème à solutions multiples en un problème d'optimisation convexe à minimum unique.


#### ReLU

  1. ReLU converge ~15× plus vite que Sigmoïde. Pourquoi ? Parce que pour s > 0, la dérivée de ReLU vaut 1 (pas
  σ(s)·(1−σ(s)) qui s'écrase près de 0 ou 1). Le gradient passe "à pleine puissance", l'apprentissage est plus rapide.

  2. Solution beaucoup plus "compacte" : poids ~7× plus petits que sigmoïde, biais 11× plus petit. Pourtant elle réalise
   la même tâche. La sigmoïde a besoin de gros poids pour "saturer" sa sortie près de 0 et 1 ; ReLU n'a pas ce problème
  puisqu'elle est linéaire dans la zone positive — elle peut "viser" directement les valeurs cibles 0 et 1.

  3. La sortie de ReLU n'est pas bornée à 1. Sur ET ça arrive par chance, mais sur les images il faudra y faire
  attention — on pourra avoir des sorties > 1.

  4. Piège connu : "dying ReLU". Si l'init aléatoire donne un biais très négatif et des poids tels que la somme reste
  négative pour toutes les entrées d'entraînement, la sortie est toujours 0, donc delta est constant, mais le gradient
  entree·eta·delta ne suffit pas toujours à sortir de cette zone. Le neurone est "mort". Refais 20 runs et regarde si tu
   as parfois des résultats catastrophiques — bonne expérience pour le rapport.


### Porte Logique OU


#### Heaviside

Ça marche, mais c'est une solution dégénérée : presque équivalente à "sortie = x₁". La dépendance à x₂ ne tient qu'au
  signe d'un nombre minuscule. Si on rajoutait du bruit aux entrées, ce neurone se casserait la figure sur (0,1) — alors
   que la solution sigmoïde (w₁=w₂=6.64) résisterait beaucoup mieux. C'est précisément ce que va montrer le test au
  bruit (étape suivante du PDF).


#### ReLU

Marche pas (bloque en 0.625)


#### Sigmoide
1. Convergence symétrique parfaite : Contrairement au comportement erratique de Heaviside, la sigmoïde trouve une solution où les deux synapses sont équilibrées (w₁ = 6.64, w₂ = 6.63). Cette symétrie est la traduction mathématique exacte de la logique OU (les entrées A et B ont strictement le même poids décisionnel).
  
  2. Analyse du Biais (Contraste ET / OU) : Le biais calculé est de -2.84, ce qui est une différence majeure par rapport au biais de -9.90 obtenu pour la porte ET. L'explication est purement logique : pour une porte OU, il suffit qu'une seule entrée soit active (soit 1 * 6.64 = 6.64) pour surpasser le biais (6.64 - 2.84 = +3.80). La somme interne devient alors suffisamment positive pour pousser la sortie de la sigmoïde proche de 1 (0.97). Pour la porte ET, le biais très profond nécessitait absolument l'addition des deux entrées pour passer dans le positif.
  
  3. Comportement face aux signaux imparfaits : Grâce à cet équilibre entre les poids et le seuil (biais), la sigmoïde se montre extrêmement robuste face au bruit. Une entrée bruitée ne déclenche pas de changement d'état brutal, contrairement à l'effet "tout-ou-rien" de Heaviside.


## Normalisation

 

