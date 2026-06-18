package Main;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Tampon circulaire partagé (version wait/notify) INSTRUMENTÉE :
 * chaque action de synchronisation (entrée en synchronized, wait, notify)
 * est journalisée en TEMPS RÉEL dans le journal d'activité de l'interface.
 */
public class TamponPartage {

    private final int[] tampon;
    private int in  = 0;
    private int out = 0;

    private volatile int count = 0;
    private volatile int totalDepose = 0;
    private volatile int totalRetire = 0;

    static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("HH:mm:ss");

    // -----------------------------------------------------------------------
    public TamponPartage(int taille) {
        if (taille <= 0) {
            throw new IllegalArgumentException(
                    "La taille du tampon doit être strictement positive.");
        }
        this.tampon = new int[taille];
    }

    // -----------------------------------------------------------------------
    /** Dépose un article. Journalise synchronized / wait / notify en direct. */
    public synchronized void produire(int article, int idProducteur)
            throws InterruptedException {

        // Le thread vient d'obtenir le moniteur de l'objet (synchronized).
        InterfaceGraphique.ajouterEvenement(
                "[P" + idProducteur + "] ENTRE en section critique (synchronized)");

        // Tant que le tampon est plein, on attend (en libérant le verrou).
        while (count == tampon.length) {
            InterfaceGraphique.ajouterEvenement(
                    "[P" + idProducteur + "] tampon PLEIN -> wait() : libère le verrou et dort");
            wait();
            InterfaceGraphique.ajouterEvenement(
                    "[P" + idProducteur + "] RÉVEILLÉ -> reprend le verrou et re-teste la condition");
        }

        // Section critique : dépôt.
        tampon[in] = article;
        in = (in + 1) % tampon.length;
        count++;
        totalDepose++;

        InterfaceGraphique.ajouterEvenement(
                "[P" + idProducteur + "] DÉPOSE l'article " + article
                + "  (tampon : " + count + "/" + tampon.length + ")");

        // Journal "productions" + mise à jour visuelle (comme avant).
        String heure = LocalDateTime.now().format(FORMAT);
        InterfaceGraphique.ajouterArticleProduit(
                "[" + heure + "] [P" + idProducteur + "] a produit : " + article,
                idProducteur);
        InterfaceGraphique.mettreAJourTailleTampon();

        // On réveille les threads en attente, puis on sort du synchronized.
        InterfaceGraphique.ajouterEvenement(
                "[P" + idProducteur + "] notifyAll() -> réveille les threads en attente, puis SORT");
        notifyAll();
    }

    // -----------------------------------------------------------------------
    /** Retire un article avec timeout. Journalise wait / notify en direct. */
    public synchronized int consommerAvecTimeout(int idConsommateur, long timeoutMs)
            throws InterruptedException {

        InterfaceGraphique.ajouterEvenement(
                "[C" + idConsommateur + "] ENTRE en section critique (synchronized)");

        long echeance = System.currentTimeMillis() + timeoutMs;
        while (count == 0) {
            long restant = echeance - System.currentTimeMillis();
            if (restant <= 0) {
                InterfaceGraphique.ajouterEvenement(
                        "[C" + idConsommateur + "] tampon VIDE, délai écoulé -> abandonne (retourne -1)");
                return -1;
            }
            InterfaceGraphique.ajouterEvenement(
                    "[C" + idConsommateur + "] tampon VIDE -> wait(" + restant
                    + " ms) : libère le verrou et dort");
            wait(restant);
            InterfaceGraphique.ajouterEvenement(
                    "[C" + idConsommateur + "] RÉVEILLÉ -> reprend le verrou et re-teste la condition");
        }

        int article = tampon[out];
        out = (out + 1) % tampon.length;
        count--;
        totalRetire++;

        InterfaceGraphique.ajouterEvenement(
                "[C" + idConsommateur + "] CONSOMME l'article " + article
                + "  (tampon : " + count + "/" + tampon.length + ")");

        String heure = LocalDateTime.now().format(FORMAT);
        InterfaceGraphique.ajouterArticleConsomme(
                "[" + heure + "] [C" + idConsommateur + "] a consommé : " + article,
                idConsommateur);
        InterfaceGraphique.mettreAJourTailleTampon();

        InterfaceGraphique.ajouterEvenement(
                "[C" + idConsommateur + "] notifyAll() -> réveille les threads en attente, puis SORT");
        notifyAll();
        return article;
    }

    // -----------------------------------------------------------------------
    public static class EtatTampon {
        public final int[] contenu;
        public final int in;
        public final int out;
        public final int count;
        public final int tailleMax;

        public EtatTampon(int[] contenu, int in, int out,
                          int count, int tailleMax) {
            this.contenu   = contenu;
            this.in        = in;
            this.out       = out;
            this.count     = count;
            this.tailleMax = tailleMax;
        }
    }

    public EtatTampon getEtatTampon() {
        return new EtatTampon(tampon.clone(), in, out, count, tampon.length);
    }

    // -----------------------------------------------------------------------
    public int getCount()       { return count; }
    public int getTotalDepose() { return totalDepose; }
    public int getTotalRetire() { return totalRetire; }
    public int getTailleMax()   { return tampon.length; }
}
