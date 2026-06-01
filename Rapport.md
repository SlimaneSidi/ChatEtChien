# Rapport

#### Heaviside

"faire des statistiques sur les valeurs des poids trouvées : ces valeurs sont-elles similaires ? Pouvez-vous le justifier ?"

  Réponse à mettre dans le rapport : non, elles ne sont pas similaires, parce que le perceptron Heaviside cherche
  n'importe quel hyperplane séparateur, pas un hyperplane optimal. Il y a une infinité de solutions valides (toute
  droite passant entre (1,1) et les 3 autres points) et l'algo prend la première rencontrée à partir de l'init
  aléatoire. Ce que tu peux montrer graphiquement (papier ou matplotlib) en traçant les droites obtenues sur les 20-30
  runs.