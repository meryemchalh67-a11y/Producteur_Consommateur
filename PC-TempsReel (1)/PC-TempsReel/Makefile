# ============================================================
#  Makefile — Projet Producteur-Consommateur
#  Compilation, exécution et nettoyage du projet Java
# ============================================================

# --- Variables ---
JAVAC = javac
JAVA  = java

# Dossier des sources (chemin relatif)
SRC_DIR = src/Main

# Dossier des fichiers compilés (.class)
BIN_DIR = bin

# Liste des fichiers sources Java
SOURCES = $(SRC_DIR)/App.java \
          $(SRC_DIR)/TamponPartage.java \
          $(SRC_DIR)/Producteur.java \
          $(SRC_DIR)/Consommateur.java \
          $(SRC_DIR)/Simulation.java \
          $(SRC_DIR)/PanneauTampon.java \
          $(SRC_DIR)/InterfaceGraphique.java

# Classe principale avec son package
MAIN_CLASS = Main.App

# --- Règles ---

all: compile

compile:
	@mkdir -p $(BIN_DIR)
	$(JAVAC) -d $(BIN_DIR) $(SOURCES)
	@echo "==> Compilation reussie."

run: compile
	$(JAVA) -cp $(BIN_DIR) $(MAIN_CLASS)

clean:
	rm -rf $(BIN_DIR)
	@echo "==> Nettoyage effectue."

.PHONY: all compile run clean