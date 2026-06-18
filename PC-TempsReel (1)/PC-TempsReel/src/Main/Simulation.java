package Main;

import java.io.FileWriter; // Pour écrire le rapport dans un fichier texte
import java.io.IOException; // Exception levée si l'écriture échoue
import java.time.LocalDateTime; // Pour récupérer l'heure courante
import java.time.format.DateTimeFormatter; // Pour formater cette heure (ex : 14:30:05)
import java.util.ArrayList; // Listes dynamiques de threads et d'objets
import java.util.Collections; // Pour synchroniser la liste du journal
import java.util.List; // Interface liste générique

/**
 * Orchestre la simulation producteur-consommateur :
 * création des threads, suivi de leur état, génération du rapport.
 *
 * Cette classe est le "chef d'orchestre" : elle ne sait pas COMMENT déposer
 * ou retirer un article (c'est le rôle de TamponPartage), mais elle sait
 * COMBIEN de threads existent et QUAND la simulation doit s'arrêter.
 *
 * Tout est statique car l'application ne gère qu'UNE seule simulation à la
 * fois : un état global unique suffit, et cela permet à Producteur et
 * Consommateur d'appeler Simulation.xxx() sans détenir de référence.
 */
public class Simulation {

    // Drapeau indiquant si la simulation tourne.
    // volatile : lu par TOUS les threads (boucles while(estEnCours())) et
    // modifié par d'autres ; volatile garantit qu'ils voient la valeur à jour.
    private static volatile boolean enCours = false;

    // Le tampon partagé courant, utilisé par tous les threads.
    private static TamponPartage tampon;

    // On garde les objets métier (pour lire leurs statistiques dans le rapport)
    // ET les Thread correspondants (pour les démarrer / interrompre) séparément.
    private static ArrayList<Producteur> listeProducteurs = new ArrayList<>();
    private static ArrayList<Consommateur> listeConsommateurs = new ArrayList<>();
    private static ArrayList<Thread> listeThreads = new ArrayList<>();

    // Compteurs servant à détecter la fin de la simulation.
    private static int nbProducteurs = 0; // nombre total de producteurs prévus
    private static int producteursTermines = 0; // combien ont fini de produire
    private static int nbConsommateurs = 0; // nombre total de consommateurs prévus
    private static int consommateursTermines = 0; // combien se sont arrêtés

    // Fonction exécutée automatiquement quand la simulation se termine d'elle-même
    // (patron Observateur : découple la logique de l'interface graphique).
    private static Runnable callbackFinAutomatique;

    // Journal d'activité en temps réel : accumule les événements horodatés
    // (synchronized, wait, notify, sleep) pour les inclure dans le rapport.
    private static final List<String> journalActivite = Collections.synchronizedList(new ArrayList<>());

    // -----------------------------------------------------------------------
    /**
     * Lance une nouvelle simulation avec les paramètres saisis dans l'interface.
     */
    public static void demarrer(int nbProd, int nbCons,
            int taille, int articles) {

        // --- ETAPE 1 : terminer proprement une éventuelle simulation précédente ---
        // (cas où l'on relance sans avoir réinitialisé)
        if (!listeThreads.isEmpty()) {
            enCours = false; // signaler l'arrêt aux boucles

            // 1re boucle : demander l'arrêt à TOUS les threads d'un coup.
            // interrupt() réveille un thread bloqué dans acquire() ou sleep().
            for (Thread t : listeThreads) {
                t.interrupt();
            }
            // 2e boucle : attendre que chaque thread se termine (max 1 s chacun).
            // On sépare les deux boucles pour ne pas attendre la mort du premier
            // thread avant de demander l'arrêt aux autres (sinon arrêt très lent).
            for (Thread t : listeThreads) {
                try {
                    t.join(1000); // attendre au plus 1000 ms
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // restaurer le drapeau (bonne pratique)
                }
            }
        }

        // --- ETAPE 2 : réinitialiser tout l'état ---
        tampon = new TamponPartage(taille); // un tampon neuf de la taille demandée
        listeProducteurs.clear();
        listeConsommateurs.clear();
        listeThreads.clear();
        nbProducteurs = nbProd;
        nbConsommateurs = nbCons;
        producteursTermines = 0;
        consommateursTermines = 0;
        journalActivite.clear(); // vider le journal précédent

        // --- ETAPE 3 : activer la simulation AVANT de démarrer les threads ---
        // Sinon un thread rapide pourrait tester estEnCours(), le trouver à false
        // et s'arrêter immédiatement sans rien faire.
        enCours = true;

        // --- ETAPE 4 : créer tous les threads d'abord (sans les démarrer) ---
        // Les identifiants commencent à 1 -> P1, P2, C1, C2 (plus lisible).
        for (int i = 1; i <= nbProd; i++) {
            Producteur p = new Producteur(tampon, i, articles);
            listeProducteurs.add(p);
            listeThreads.add(new Thread(p)); // créé mais PAS encore démarré
        }
        for (int i = 1; i <= nbCons; i++) {
            Consommateur c = new Consommateur(tampon, i);
            listeConsommateurs.add(c);
            listeThreads.add(new Thread(c));
        }

        // ... puis les démarrer TOUS ensemble pour un départ quasi simultané
        // (équitable).
        for (Thread t : listeThreads) {
            t.start();
        }
    }

    // -----------------------------------------------------------------------
    /** Arrête la simulation (bouton "Arrêter"). */
    public static void arreter() {
        enCours = false; // les boucles while(estEnCours()) vont sortir
        for (Thread t : listeThreads) {
            t.interrupt(); // réveiller ceux qui dorment / sont bloqués
        }
    }

    // -----------------------------------------------------------------------
    /**
     * Arrête la simulation ET remet tout l'état à zéro (bouton "Réinitialiser").
     */
    public static void reinitialiser() {
        enCours = false;
        for (Thread t : listeThreads) {
            t.interrupt();
        }
        tampon = null; // on jette le tampon
        listeProducteurs.clear();
        listeConsommateurs.clear();
        listeThreads.clear();
        producteursTermines = 0;
        consommateursTermines = 0;
        journalActivite.clear(); // vider le journal
    }

    // -----------------------------------------------------------------------
    /**
     * Indique si la simulation tourne.
     * PAS synchronized : simple lecture d'un boolean volatile (déjà atomique
     * et déjà visible entre threads).
     */
    public static boolean estEnCours() {
        return enCours;
    }

    /**
     * Appelée par un producteur qui a fini sa production.
     * synchronized car plusieurs producteurs peuvent l'appeler en même temps
     * et l'incrément ++ n'est PAS atomique (lire, +1, réécrire).
     */
    public static synchronized void producteurTermine() {
        producteursTermines++;
    }

    /**
     * Vrai si tous les producteurs ont terminé.
     * Utilisée par les consommateurs pour décider de leur condition d'arrêt.
     */
    public static synchronized boolean tousProducteursTermines() {
        return producteursTermines >= nbProducteurs;
    }

    /**
     * Appelée par un consommateur qui s'arrête (toujours, via son bloc finally).
     * synchronized pour la même raison que producteurTermine().
     * Quand le DERNIER consommateur s'arrête, on termine la simulation et on
     * déclenche le callback (affichage du rapport + boutons).
     */
    public static synchronized void consommateurTermine() {
        consommateursTermines++;
        if (consommateursTermines >= nbConsommateurs) {
            enCours = false;
            if (callbackFinAutomatique != null) {
                callbackFinAutomatique.run(); // exécute le code fourni par l'interface
            }
        }
    }

    /**
     * Enregistre le code à exécuter en fin de simulation automatique.
     * L'interface graphique l'appelle à sa création (patron Observateur) :
     * Simulation reste ainsi indépendante de l'affichage.
     */
    public static void setCallbackFinAutomatique(Runnable callback) {
        callbackFinAutomatique = callback;
    }

    // -----------------------------------------------------------------------
    /**
     * Construit le rapport final : une DESCRIPTION des actions réalisées,
     * puis le bilan global, le détail par thread et l'état final.
     */
    public static String genererRapport() {
        if (tampon == null)
            return "Aucune simulation lancée."; // évite un plantage

        String date = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy 'à' HH:mm:ss"));

        // Nombre d'articles que chaque producteur devait produire (paramètre).
        int articlesParProd = listeProducteurs.isEmpty()
                ? 0
                : listeProducteurs.get(0).getArticlesAProduire();

        // StringBuilder (et non +) car les String sont immuables : concaténer
        // avec + recréerait un objet à chaque étape, ce qui est inefficace.
        StringBuilder r = new StringBuilder();

        // --- JOURNAL D'ACTIVITÉ EN TEMPS RÉEL ---
        r.append("JOURNAL D'ACTIVITÉ EN TEMPS RÉEL\n");
        r.append("------------------------------------------------\n");
        synchronized (journalActivite) {
            if (journalActivite.isEmpty()) {
                r.append("(aucun événement enregistré)\n");
            } else {
                for (String evt : journalActivite) {
                    r.append(evt).append("\n");
                }
            }
        }
        r.append("\n");

        // --- BILAN GLOBAL ---
        r.append("BILAN GLOBAL\n");
        r.append("------------------------------------------------\n");
        r.append("Articles produits au total      : ")
                .append(tampon.getTotalDepose()).append("\n");
        r.append("Articles consommés au total     : ")
                .append(tampon.getTotalRetire()).append("\n");
        r.append("Articles restant dans le tampon : ")
                .append(tampon.getCount()).append("\n\n");

        // --- DÉTAIL PAR PRODUCTEUR (phrasé descriptif) ---
        r.append("DÉTAIL PAR PRODUCTEUR\n");
        r.append("------------------------------------------------\n");
        for (Producteur p : listeProducteurs) {
            r.append("Le producteur ").append(p.getIdentifiant())
                    .append(" a produit ").append(p.getTotalProduit())
                    .append(" article(s).\n");
        }
        r.append("\n");

        // --- DÉTAIL PAR CONSOMMATEUR (phrasé descriptif) ---
        r.append("DÉTAIL PAR CONSOMMATEUR\n");
        r.append("------------------------------------------------\n");
        for (Consommateur c : listeConsommateurs) {
            r.append("Le consommateur ").append(c.getIdentifiant())
                    .append(" a consommé ").append(c.getTotalConsomme())
                    .append(" article(s).\n");
        }
        r.append("\n");

        // --- ÉTAT FINAL (interprétation du résultat) ---
        r.append("ÉTAT FINAL\n");
        r.append("------------------------------------------------\n");
        if (tampon.getCount() == 0
                && tampon.getTotalDepose() == tampon.getTotalRetire()) {
            r.append("Tous les articles produits ont été consommés :\n");
            r.append("la simulation s'est terminée normalement.\n");
        } else {
            r.append("Il reste ").append(tampon.getCount())
                    .append(" article(s) non consommé(s) dans le tampon :\n");
            r.append("la simulation a probablement été arrêtée manuellement.\n");
        }
        r.append("================================================\n");
        return r.toString();
    }

    // -----------------------------------------------------------------------
    /**
     * Écrit les journaux et le rapport dans un fichier texte horodaté.
     * Déclare throws IOException : la gestion de l'erreur est laissée à
     * l'appelant (l'interface affiche une boîte de dialogue).
     */
    public static String enregistrerRapport(String journalProductions,
            String journalConsommations,
            String rapport) throws IOException {
        String heure = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("HH-mm-ss"));
        String nomFichier = "rapport_" + heure + ".txt"; // l'heure évite d'écraser un ancien rapport

        // try-with-resources : le FileWriter est fermé AUTOMATIQUEMENT à la fin,
        // même en cas d'exception (plus sûr qu'un close() manuel).
        try (FileWriter fw = new FileWriter(nomFichier)) {
            fw.write("=== JOURNAL DES PRODUCTIONS ===\n");
            fw.write(journalProductions);
            fw.write("\n=== JOURNAL DES CONSOMMATIONS ===\n");
            fw.write(journalConsommations);
            fw.write("\n=== RAPPORT FINAL ===\n");
            fw.write(rapport);
        }
        return nomFichier; // renvoie le nom pour l'afficher à l'utilisateur
    }

    // -----------------------------------------------------------------------
    /**
     * Ajoute un événement horodaté au journal d'activité en temps réel.
     * Thread-safe grâce à la liste synchronisée.
     */
    public static void ajouterEvenementJournal(String message) {
        String heure = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
        journalActivite.add("[" + heure + "] " + message);
    }

    // -----------------------------------------------------------------------
    /** Donne accès au tampon courant (utilisé par l'interface pour l'affichage). */
    public static TamponPartage getTampon() {
        return tampon;
    }
}
