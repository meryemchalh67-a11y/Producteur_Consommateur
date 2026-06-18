package Main;

/**
 * Thread consommateur : retire des articles du tampon en continu.
 * INSTRUMENTÉ : journalise l'appel à sleep() en temps réel.
 */
public class Consommateur implements Runnable {

    private static final int  DELAI_MAX_MS = 1500;
    private static final long TIMEOUT_MS   = 500;

    private final TamponPartage tamponPartage;
    private final int identifiant;

    private volatile int totalConsomme = 0;

    // -----------------------------------------------------------------------
    public Consommateur(TamponPartage tamponPartage, int identifiant) {
        if (tamponPartage == null) {
            throw new IllegalArgumentException("Le tampon ne peut pas être null.");
        }
        if (identifiant < 0) {
            throw new IllegalArgumentException("L'identifiant doit être positif.");
        }
        this.tamponPartage = tamponPartage;
        this.identifiant   = identifiant;
    }

    // -----------------------------------------------------------------------
    @Override
    public void run() {
        try {
            while (Simulation.estEnCours()) {
                int article = tamponPartage.consommerAvecTimeout(
                        identifiant, TIMEOUT_MS);

                if (article == -1) {
                    if (Simulation.tousProducteursTermines()
                            && tamponPartage.getCount() == 0) {
                        break;
                    }
                    continue;
                }

                totalConsomme++;

                // Simulation d'un temps de traitement : on journalise le sleep.
                int duree = (int) (Math.random() * DELAI_MAX_MS);
                InterfaceGraphique.ajouterEvenement(
                        "[C" + identifiant + "] sleep(" + duree
                        + " ms) : simule le temps de traitement");
                Thread.sleep(duree);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            InterfaceGraphique.ajouterEvenement(
                    "[C" + identifiant + "] s'arrête (" + totalConsomme + " articles consommés)");
            Simulation.consommateurTermine();
        }
    }

    // -----------------------------------------------------------------------
    public int getIdentifiant()   { return identifiant; }
    public int getTotalConsomme() { return totalConsomme; }
}
