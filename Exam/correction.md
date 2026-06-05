# Correction — DS ISEN3, 5 juin 2025

**Projet I.A. / Java / THS**

> ⭐ = question à ne pas traiter si tiers-temps validé. Corrigée ci-dessous quand même.

---

## a) Contrainte sur la taille N d'un bloc pour la FFT

☑ **N doit être une puissance de 2.**

## b) ⭐ Pourquoi

La FFT fournie est une **Cooley-Tukey radix-2** : elle calcule la DFT par
**« diviser pour régner »**, en scindant à chaque étape le bloc en deux
sous-blocs (indices pairs / impairs), chacun de taille N/2. Pour que ce
découpage par 2 reste valide **jusqu'au niveau 1**, il faut que N soit
divisible par 2 à chaque niveau de récursion → **N = 2^k**. Si N n'est pas une
puissance de 2, un des sous-blocs aurait une taille impaire et la récursion
casse.

## c) Deux raisons d'appliquer efficacement une FFT sur un fichier son complet

1. **Coût algorithmique** : la FFT est en **O(N log N)** au lieu de O(N²) pour
   la DFT naïve. Même sur un fichier entier (des centaines de milliers
   d'échantillons), le calcul reste praticable.
2. **(lié à a/b)** La contrainte « puissance de 2 » n'est pas bloquante : un
   fichier de taille quelconque peut être **complété par des zéros
   (zero-padding)** jusqu'à la puissance de 2 supérieure, ou **découpé en
   blocs** de taille puissance de 2 (fenêtrage / STFT). Un signal sonore est
   déjà une suite finie d'échantillons réels, directement exploitable en
   entrée.

## d) Pourquoi des classes dérivées pour la fonction d'activation

Pour **mutualiser** tout le comportement commun d'un neurone (poids, biais,
`metAJour`, `apprentissage`) écrit **une seule fois** dans la classe mère
`Neurone`, et ne **redéfinir que ce qui change** — la méthode `activation` —
dans chaque classe fille (`NeuroneHeavyside`, `NeuroneSigmoide`,
`NeuroneReLU`). C'est le **polymorphisme** : le code de la mère appelle
`activation()` et la bonne version s'exécute selon le type réel de l'objet. On
évite la duplication et on peut ajouter une nouvelle activation sans toucher au
reste.

## e) Pourquoi `activation` est abstraite dans `Neurone`

Parce que la classe `Neurone` **ne peut pas fournir de version générique** de
l'activation : c'est précisément le seul élément indéterminé. La déclarer
`abstract` :

- **force** chaque classe fille concrète à la définir (contrat vérifié à la
  **compilation**) ;
- rend `Neurone` **non instanciable** — ce qui est correct, un « neurone sans
  fonction d'activation » n'a pas de sens ;
- tout en autorisant la mère à **l'appeler** dans `metAJour`/`apprentissage`
  (patron de méthode).

## f) Pourquoi une interface `iNeurone`

☑ **« car si on manipule un neurone au travers d'une interface, on pourra
utiliser une autre implémentation du neurone sans changer le code qui utilise
l'interface »**

Les deux autres sont fausses : on peut très bien utiliser des classes dérivées
sans interface, et une interface n'est **pas** obligatoire en Java.

## g) Le neurone a bon sur N-1 données, faux sur la dernière

☑ **« il se peut qu'il faille faire non pas une seule, mais plusieurs passes
avec toutes les N données pour converger »**

Justification : l'apprentissage est une **descente de gradient itérative**.
Corriger la dernière donnée **modifie les poids**, ce qui peut re-déranger des
cas déjà appris. Il faut donc **repasser toutes les N données plusieurs fois
(époques)** jusqu'à convergence globale.

- 1ère case fausse : n'apprendre que la dernière donnée → le neurone
  **« oublie »** les autres (oubli catastrophique).
- 2e case fausse : une seule passe supplémentaire ne garantit pas la
  correction.

---

## h) Explication de la réponse à e)

La fonction d'activation est ce qui **distingue** les types de neurones ; la
classe générique `Neurone` n'a aucune valeur par défaut sensée à lui donner. La
rendre **abstraite** établit un **contrat** : toute classe fille *doit*
l'implémenter, et `Neurone` ne peut pas être instanciée seule (ce qui serait
absurde). Cela permet aussi à `metAJour`/`apprentissage` d'**appeler
`activation()` de façon polymorphe** sans connaître l'activation concrète.
C'est donc à la fois une garantie de cohérence (à la compilation) et le
mécanisme qui rend le polymorphisme possible.

## i) Poids petits et variables d'un essai à l'autre

```
essai 1 : 0.038192287 ; 0.067795664 ; biais -0.038834155
essai 2 : 0.35553247  ; 0.26893687  ; biais -0.50386286
essai 3 : 0.15349302  ; 0.09287446  ; biais -0.16372138
```

☑ **Heaviside.**

Les poids sont **petits** (0.04 à 0.36) et **très variables** entre les trois
essais. C'est la signature d'une activation **en marche d'escalier** : la règle
d'apprentissage du perceptron **arrête de mettre à jour les poids dès que les 4
cas sont bien classés** (erreur = 0). Comme on part de poids aléatoires petits
et qu'on s'arrête tôt, les poids restent petits et **dépendent du tirage
initial** → forte variabilité. Il n'existe pas de solution unique, juste un
hyperplan séparateur quelconque.

## j) ⭐ Poids grands et quasi identiques à chaque essai

```
essai 1 : 6.487072 ; 6.48707  ; biais -9.869467
essai 2 : 6.487069 ; 6.487073 ; biais -9.869467
essai 3 : 6.487071 ; 6.487071 ; biais -9.869467
```

☑ **Sigmoïde.**

Les poids convergent vers une valeur **précise et reproductible** (≈ 6.487 ;
6.487 ; biais ≈ −9.869 aux 6 décimales près sur les 3 essais). C'est typique
d'une activation **continue et dérivable** : la descente de gradient minimise
la **MSE** vers un **optimum unique et bien défini**, d'où la reproductibilité.
Et comme la sigmoïde ne vaut jamais exactement 0 ou 1, l'optimiseur doit
**pousser les poids vers de grandes valeurs** pour saturer la sortie près de
0/1 (on vérifie : (1,1)→σ(3.1)≈0.96, les autres →σ(<0)≈0, soit bien la fonction
ET). ReLU est exclue dans les deux cas (sortie non bornée, peu adaptée à une
sortie 0/1).

## k) Les quatre étapes de la démarche scientifique

1. **Observation** : constat d'un phénomène, question posée.
2. **Hypothèse** : proposition d'une explication testable.
3. **Expérimentation** : prédiction puis expérience reproductible pour tester
   l'hypothèse.
4. **Conclusion / analyse** : interprétation des résultats — l'hypothèse est
   **confirmée ou réfutée** (sinon on reformule et on recommence).

## l) ⭐ Les quatre étapes de la démarche d'ingénierie

1. **Définition du besoin / problème** : cahier des charges, contraintes,
   objectifs.
2. **Conception** : recherche et choix de solutions possibles (avant-projet).
3. **Réalisation** : implémentation / fabrication d'un prototype.
4. **Test et validation** : évaluation par rapport au besoin, puis
   **itération / amélioration**.

> Différence clé avec la démarche scientifique : l'ingénierie vise à
> **résoudre un problème / produire un objet conforme à un besoin**, alors que
> la science vise à **comprendre / expliquer** un phénomène.
