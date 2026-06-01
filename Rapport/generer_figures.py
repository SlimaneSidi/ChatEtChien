"""
Reproduit en Python les neurones Heaviside, Sigmoide et ReLU
implementes en Java (Support/neurone/*.java) afin d'exporter
les courbes MSE en fonction des iterations pour ET et OU.

Les formules d'apprentissage sont rigoureusement identiques
a celles du code Neurone.java fourni :
    synapses[j] += entree[j] * eta * delta
    biais       += eta * delta
ou delta = consigne - sortie, et sortie = activation(somme).
"""

import math
import random
import matplotlib.pyplot as plt

random.seed(42)

ETA = 0.0001
MSE_LIMITE = 0.001
MAX_ITER = 2000000  # garde-fou (le plateau ReLU/OU ne s'arrete jamais)

def heaviside(s): return 1.0 if s >= 0 else 0.0
def sigmoide(s):  return 1.0 / (1.0 + math.exp(-s))
def relu(s):      return max(0.0, s)

ACTIVATIONS = {
    "Heaviside": heaviside,
    "Sigmoide":  sigmoide,
    "ReLU":      relu,
}

ENTREES = [[0, 0], [0, 1], [1, 0], [1, 1]]
CIBLES_ET = [0, 0, 0, 1]
CIBLES_OU = [0, 1, 1, 1]

def apprend(activation, entrees, cibles, mse_limite=MSE_LIMITE, max_iter=MAX_ITER):
    """Reproduit exactement Neurone.apprentissage()"""
    w = [random.uniform(-1, 1) for _ in range(len(entrees[0]))]
    b = random.uniform(-1, 1)
    historique = []
    mse = float("inf")
    it = 0
    while mse > mse_limite and it < max_iter:
        mse = 0.0
        for i, e in enumerate(entrees):
            somme = b + sum(e[j] * w[j] for j in range(len(e)))
            sortie = activation(somme)
            delta = cibles[i] - sortie
            mse += delta * delta
            for j in range(len(e)):
                w[j] += e[j] * ETA * delta
            b += ETA * delta
        mse /= len(entrees)
        historique.append(mse)
        it += 1
    return historique, w, b

def trace(activation_name, gate_name, cibles, fichier):
    activation = ACTIVATIONS[activation_name]
    hist, w, b = apprend(activation, ENTREES, cibles)

    fig, ax = plt.subplots(figsize=(7, 4))
    ax.plot(hist, linewidth=0.8, color="#1f77b4")
    ax.set_xlabel("Iteration")
    ax.set_ylabel("MSE")
    ax.set_title(f"Convergence MSE - {activation_name} sur {gate_name}")
    ax.grid(True, alpha=0.3)
    ax.axhline(MSE_LIMITE, color="red", linestyle="--", linewidth=0.8,
               label=f"MSElimite = {MSE_LIMITE}")
    ax.legend(loc="upper right")

    # Annotations resultat
    n_iter = len(hist)
    mse_finale = hist[-1]
    texte = (f"Iterations : {n_iter}\n"
             f"MSE finale : {mse_finale:.4f}\n"
             f"w = ({w[0]:.3f}, {w[1]:.3f})\n"
             f"b = {b:.3f}")
    ax.text(0.98, 0.55, texte, transform=ax.transAxes,
            ha="right", va="top", family="monospace", fontsize=9,
            bbox=dict(boxstyle="round", facecolor="white", alpha=0.85))

    fig.tight_layout()
    fig.savefig(fichier, dpi=130)
    plt.close(fig)
    print(f"  -> {fichier}  ({n_iter} iter, MSE={mse_finale:.4f})")

if __name__ == "__main__":
    import os
    out = os.path.join(os.path.dirname(__file__), "figures")
    os.makedirs(out, exist_ok=True)

    configs = [
        ("Heaviside", "ET", CIBLES_ET),
        ("Heaviside", "OU", CIBLES_OU),
        ("Sigmoide",  "ET", CIBLES_ET),
        ("Sigmoide",  "OU", CIBLES_OU),
        ("ReLU",      "ET", CIBLES_ET),
        ("ReLU",      "OU", CIBLES_OU),
    ]
    for act, gate, cibles in configs:
        fichier = os.path.join(out, f"mse_{act.lower()}_{gate.lower()}.png")
        print(f"Generation {act} / {gate}")
        trace(act, gate, cibles, fichier)
    print("Termine.")
