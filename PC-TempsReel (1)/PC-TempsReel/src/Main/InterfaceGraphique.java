package Main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Interface graphique Swing.
 * Ajout : un "Journal d'activité (temps réel)" qui affiche, horodatées à la
 * milliseconde, les actions de synchronisation (synchronized, wait, notify)
 * et les sleep, au moment exact où elles se produisent.
 */
public class InterfaceGraphique {

    private static JLabel tailleTamponLabel;
    private static JLabel totalProduitsLabel;
    private static JLabel totalConsommesLabel;
    private static JTextArea articlesProduitsTextArea;
    private static JTextArea articlesConsommesTextArea;
    private static JTextArea journalActiviteTextArea;   // NOUVEAU : journal temps réel
    private static JTextArea rapportTextArea;
    private static PanneauTampon panneauTampon;

    private static JButton demarrerButton;
    private static JButton arreterButton;
    private static JButton reinitialiserButton;
    private static JButton enregistrerButton;

    private static JFrame frame;

    // Format avec millisecondes pour bien voir le "temps réel".
    private static final DateTimeFormatter FORMAT_MS =
            DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    // -----------------------------------------------------------------------
    public InterfaceGraphique() {
        frame = new JFrame("Producteur-Consommateur");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(950, 950);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Champs de saisie
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Nombre de producteurs:"), gbc);
        final JTextField producteursInput = new JTextField("2");
        gbc.gridx = 1; panel.add(producteursInput, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Nombre de consommateurs:"), gbc);
        final JTextField consommateursInput = new JTextField("2");
        gbc.gridx = 1; panel.add(consommateursInput, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Taille du tampon:"), gbc);
        final JTextField tailleTamponInput = new JTextField("5");
        gbc.gridx = 1; panel.add(tailleTamponInput, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Nombre d'articles par producteur:"), gbc);
        final JTextField articlesAProduireInput = new JTextField("10");
        gbc.gridx = 1; panel.add(articlesAProduireInput, gbc);

        // Statistiques
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Remplissage du tampon (actuel/max):"), gbc);
        tailleTamponLabel = new JLabel("0 / 0");
        tailleTamponLabel.setForeground(Color.BLUE);
        gbc.gridx = 1; panel.add(tailleTamponLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 5;
        panel.add(new JLabel("Total articles produits:"), gbc);
        totalProduitsLabel = new JLabel("0");
        totalProduitsLabel.setForeground(new Color(0, 150, 0));
        gbc.gridx = 1; panel.add(totalProduitsLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 6;
        panel.add(new JLabel("Total articles consommés:"), gbc);
        totalConsommesLabel = new JLabel("0");
        totalConsommesLabel.setForeground(Color.RED);
        gbc.gridx = 1; panel.add(totalConsommesLabel, gbc);

        // Journaux produits / consommations
        gbc.gridx = 0; gbc.gridy = 7;
        panel.add(new JLabel("Journal des produits:"), gbc);
        articlesProduitsTextArea = new JTextArea(6, 30);
        articlesProduitsTextArea.setEditable(false);
        articlesProduitsTextArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        gbc.gridx = 1;
        panel.add(new JScrollPane(articlesProduitsTextArea), gbc);

        gbc.gridx = 0; gbc.gridy = 8;
        panel.add(new JLabel("Journal des consommations:"), gbc);
        articlesConsommesTextArea = new JTextArea(6, 30);
        articlesConsommesTextArea.setEditable(false);
        articlesConsommesTextArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        gbc.gridx = 1;
        panel.add(new JScrollPane(articlesConsommesTextArea), gbc);

        // NOUVEAU : Journal d'activité (temps réel) : synchronized / wait / notify / sleep
        gbc.gridx = 0; gbc.gridy = 9;
        panel.add(new JLabel("Journal d'activité (temps réel) :"), gbc);
        journalActiviteTextArea = new JTextArea(12, 30);
        journalActiviteTextArea.setEditable(false);
        journalActiviteTextArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        gbc.gridx = 1;
        panel.add(new JScrollPane(journalActiviteTextArea), gbc);

        // Rapport
        gbc.gridx = 0; gbc.gridy = 10;
        panel.add(new JLabel("Rapport final:"), gbc);
        rapportTextArea = new JTextArea(6, 30);
        rapportTextArea.setEditable(false);
        rapportTextArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        gbc.gridx = 1;
        panel.add(new JScrollPane(rapportTextArea), gbc);

        // Visualisation
        gbc.gridx = 0; gbc.gridy = 11;
        gbc.gridwidth = 2;
        panel.add(new JLabel("Visualisation du tampon :"), gbc);

        panneauTampon = new PanneauTampon();
        gbc.gridx = 0; gbc.gridy = 12;
        gbc.gridwidth = 2;
        panel.add(panneauTampon, gbc);
        gbc.gridwidth = 1;

        // Boutons
        JPanel panneauBoutons = new JPanel(new FlowLayout());
        demarrerButton      = new JButton("Démarrer");
        arreterButton       = new JButton("Arrêter");
        reinitialiserButton = new JButton("Réinitialiser");
        enregistrerButton   = new JButton("Enregistrer rapport");

        demarrerButton.setBackground(new Color(0, 150, 0));
        demarrerButton.setForeground(Color.WHITE);
        arreterButton.setBackground(Color.RED);
        arreterButton.setForeground(Color.WHITE);
        enregistrerButton.setBackground(Color.BLUE);
        enregistrerButton.setForeground(Color.WHITE);

        arreterButton.setEnabled(false);
        reinitialiserButton.setEnabled(false);
        enregistrerButton.setEnabled(false);

        panneauBoutons.add(demarrerButton);
        panneauBoutons.add(arreterButton);
        panneauBoutons.add(reinitialiserButton);
        panneauBoutons.add(enregistrerButton);

        gbc.gridx = 0; gbc.gridy = 13;
        gbc.gridwidth = 2;
        panel.add(panneauBoutons, gbc);

        // Callback de fin automatique
        Simulation.setCallbackFinAutomatique(new Runnable() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        afficherRapport();
                        demarrerButton.setEnabled(false);
                        arreterButton.setEnabled(false);
                        reinitialiserButton.setEnabled(true);
                        enregistrerButton.setEnabled(true);
                    }
                });
            }
        });

        // Action Démarrer
        demarrerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int nbProd   = Integer.parseInt(producteursInput.getText());
                    int nbCons   = Integer.parseInt(consommateursInput.getText());
                    int taille   = Integer.parseInt(tailleTamponInput.getText());
                    int articles = Integer.parseInt(articlesAProduireInput.getText());

                    if (nbProd <= 0 || nbCons <= 0
                            || taille <= 0 || articles <= 0) {
                        JOptionPane.showMessageDialog(frame,
                                "Toutes les valeurs doivent être positives !");
                        return;
                    }

                    Simulation.demarrer(nbProd, nbCons, taille, articles);

                    demarrerButton.setEnabled(false);
                    arreterButton.setEnabled(true);
                    reinitialiserButton.setEnabled(false);
                    enregistrerButton.setEnabled(false);

                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame,
                            "Veuillez entrer des nombres entiers valides !");
                }
            }
        });

        // Action Arrêter
        arreterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Simulation.arreter();
                demarrerButton.setEnabled(false);
                arreterButton.setEnabled(false);
                reinitialiserButton.setEnabled(true);
                enregistrerButton.setEnabled(true);
                afficherRapport();
            }
        });

        // Action Réinitialiser
        reinitialiserButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Simulation.reinitialiser();
                articlesProduitsTextArea.setText("");
                articlesConsommesTextArea.setText("");
                journalActiviteTextArea.setText("");      // on vide aussi le journal d'activité
                rapportTextArea.setText("");
                tailleTamponLabel.setText("0 / 0");
                totalProduitsLabel.setText("0");
                totalConsommesLabel.setText("0");
                panneauTampon.rafraichir(null);
                demarrerButton.setEnabled(true);
                arreterButton.setEnabled(false);
                reinitialiserButton.setEnabled(false);
                enregistrerButton.setEnabled(false);
            }
        });

        // Action Enregistrer
        enregistrerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String nomFichier = Simulation.enregistrerRapport(
                            articlesProduitsTextArea.getText(),
                            articlesConsommesTextArea.getText(),
                            rapportTextArea.getText());
                    JOptionPane.showMessageDialog(frame,
                            "Rapport enregistré : " + nomFichier);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(frame,
                            "Erreur lors de l'enregistrement : " + ex.getMessage());
                }
            }
        });

        frame.getContentPane().add(new JScrollPane(panel));
        frame.setVisible(true);
    }

    // -----------------------------------------------------------------------
    private static void afficherRapport() {
        rapportTextArea.setText(Simulation.genererRapport());
    }

    // -----------------------------------------------------------------------
    public static void mettreAJourTailleTampon() {
        final TamponPartage tampon = Simulation.getTampon();
        if (tampon == null) return;

        final TamponPartage.EtatTampon etat = tampon.getEtatTampon();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                tailleTamponLabel.setText(etat.count + " / " + etat.tailleMax);
                totalProduitsLabel.setText(String.valueOf(tampon.getTotalDepose()));
                totalConsommesLabel.setText(String.valueOf(tampon.getTotalRetire()));
                panneauTampon.rafraichir(etat);
            }
        });
    }

    // -----------------------------------------------------------------------
    public static void ajouterArticleProduit(final String article, int idProducteur) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                articlesProduitsTextArea.append(article + "\n");
            }
        });
    }

    // -----------------------------------------------------------------------
    public static void ajouterArticleConsomme(final String article, int idConsommateur) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                articlesConsommesTextArea.append(article + "\n");
            }
        });
    }

    // -----------------------------------------------------------------------
    /**
     * Ajoute une ligne au JOURNAL D'ACTIVITÉ en temps réel, horodatée à la
     * milliseconde. Appelée par TamponPartage, Producteur et Consommateur
     * pour tracer les actions synchronized / wait / notify / sleep.
     */
    public static void ajouterEvenement(final String message) {
        // Stocker l'événement dans Simulation pour le rapport final.
        Simulation.ajouterEvenementJournal(message);

        final String heure = LocalDateTime.now().format(FORMAT_MS);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (journalActiviteTextArea == null) return;
                journalActiviteTextArea.append("[" + heure + "] " + message + "\n");
                // défilement automatique vers la dernière ligne
                journalActiviteTextArea.setCaretPosition(
                        journalActiviteTextArea.getDocument().getLength());
            }
        });
    }
}
