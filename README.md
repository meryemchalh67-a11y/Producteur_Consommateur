# Producteur-Consommateur

Simulation du problème Producteur-Consommateur en Java avec interface graphique.

## Prérequis

- Java JDK 8 ou supérieur

## Lancer le projet

```bash
make run
```

Ou sans Make :

```bash
mkdir -p bin
javac -d bin src/Main/*.java
java -cp bin Main.App
```

## Fonctionnement

- Les **producteurs** déposent des articles dans un tampon partagé
- Les **consommateurs** les retirent
- L'interface affiche l'état du tampon et les événements en temps réel
- Un rapport `.txt` est généré automatiquement à la fin

## Structure

```
src/Main/
├── App.java                # Point d'entrée
├── TamponPartage.java      # Tampon circulaire partagé
├── Producteur.java         # Thread producteur
├── Consommateur.java       # Thread consommateur
├── Simulation.java         # Gestion des threads
├── InterfaceGraphique.java # Interface graphique
└── PanneauTampon.java      # Visualisation du tampon
```
