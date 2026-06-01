# Rapport


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


#### ReLu