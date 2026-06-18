package Main;

/**
 * Thread producteur : génère un nombre fixe d'articles et les dépose
 * dans le tampon. INSTRUMENTÉ : journalise l'appel à sleep() en temps réel.
 */
public class Producteur implements Runnable {

    private static final int DELAI_MAX_MS = 500;

    private final TamponPartage tamponPartage;
    private final int identifiant;
    private final int articlesAProduire;

    private volatile int totalProduit = 0;

    // -----------------------------------------------------------------------
    public Producteur(TamponPartage tamponPartage, int identifiant,
                      int articlesAProduire) {
        if (tamponPartage == null) {
            throw new IllegalArgumentException("Le tampon ne peut pas être null.");
        }
        if (identifiant < 0) {
            throw new IllegalArgumentException("L'identifiant doit être positif.");
        }
        if (articlesAProduire <= 0) {
            throw new IllegalArgumentException(
                    "Le nombre d'articles à produire doit être strictement positif.");
        }
        this.tamponPartage     = tamponPartage;
        this.identifiant       = identifiant;
        this.articlesAProduire = articlesAProduire;
    }

    // -----------------------------------------------------------------------
    @Override
    public void run() {
        try {
            for (int i = 0;
                 i < articlesAProduire && Simulation.estEnCours();
                 i++) {

                int article = identifiant * 100 + i;
                tamponPartage.produire(article, identifiant);
                totalProduit++;

                // Simulation d'un temps de production : on journalise le sleep.
                int duree = (int) (Math.random() * DELAI_MAX_MS);
                InterfaceGraphique.ajouterEvenement(
                        "[P" + identifiant + "] sleep(" + duree
                        + " ms) : simule le temps de production");
                Thread.sleep(duree);
            }
            if (totalProduit >= articlesAProduire) {
                InterfaceGraphique.ajouterEvenement(
                        "[P" + identifiant + "] a terminé sa production ("
                        + totalProduit + " articles)");
                Simulation.producteurTermine();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // -----------------------------------------------------------------------
    public int getIdentifiant()       { return identifiant; }
    public int getArticlesAProduire() { return articlesAProduire; }
    public int getTotalProduit()      { return totalProduit; }
}
