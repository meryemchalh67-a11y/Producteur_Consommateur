package Main;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Panneau Swing qui dessine la représentation visuelle du tampon :
 * une rangée de cases qui se colorent selon le remplissage,
 * avec les flèches 'in' et 'out' illustrant le caractère circulaire.
 */
public class PanneauTampon extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final int LARGEUR_CASE = 60;
    private static final int HAUTEUR_CASE = 50;
    private static final int ESPACEMENT   = 5;
    private static final int MARGE_GAUCHE = 20;
    private static final int MARGE_HAUT   = 30;

    private static final Color COULEUR_PLEINE = new Color(76, 175, 80);
    private static final Color COULEUR_VIDE   = new Color(238, 238, 238);
    private static final Color COULEUR_BORD   = Color.DARK_GRAY;
    private static final Color COULEUR_IN     = new Color(220, 50, 50);
    private static final Color COULEUR_OUT    = new Color(50, 100, 220);

    private TamponPartage.EtatTampon etat = null;

    // -----------------------------------------------------------------------
    public PanneauTampon() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(500, 130));
    }

    // -----------------------------------------------------------------------
    public void rafraichir(TamponPartage.EtatTampon nouvelEtat) {
        this.etat = nouvelEtat;
        repaint();
    }

    // -----------------------------------------------------------------------
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (etat == null || etat.tailleMax == 0) {
            g.setColor(Color.GRAY);
            g.setFont(new Font("SansSerif", Font.ITALIC, 12));
            g.drawString("(en attente d'une simulation)",
                         MARGE_GAUCHE, getHeight() / 2);
            return;
        }

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Déterminer les cases occupées
        boolean[] caseOccupee = new boolean[etat.tailleMax];
        for (int k = 0; k < etat.count; k++) {
            int idx = (etat.out + k) % etat.tailleMax;
            caseOccupee[idx] = true;
        }

        // Dessiner les cases
        for (int i = 0; i < etat.tailleMax; i++) {
            int x = MARGE_GAUCHE + i * (LARGEUR_CASE + ESPACEMENT);
            int y = MARGE_HAUT;

            g2.setColor(Color.GRAY);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            g2.drawString("[" + i + "]", x + 4, y - 4);

            g2.setColor(caseOccupee[i] ? COULEUR_PLEINE : COULEUR_VIDE);
            g2.fillRect(x, y, LARGEUR_CASE, HAUTEUR_CASE);

            g2.setColor(COULEUR_BORD);
            g2.drawRect(x, y, LARGEUR_CASE, HAUTEUR_CASE);

            if (caseOccupee[i]) {
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", Font.BOLD, 16));
                String txt = String.valueOf(etat.contenu[i]);
                int largeurTxt = g2.getFontMetrics().stringWidth(txt);
                g2.drawString(txt,
                              x + (LARGEUR_CASE - largeurTxt) / 2,
                              y + HAUTEUR_CASE / 2 + 6);
            }
        }

        // Flèches in et out
        int yFleche = MARGE_HAUT + HAUTEUR_CASE + 4;
        dessinerFleche(g2, etat.in,  COULEUR_IN,  "in",  yFleche);
        dessinerFleche(g2, etat.out, COULEUR_OUT, "out", yFleche + 18);
    }

    // -----------------------------------------------------------------------
    private void dessinerFleche(Graphics2D g2, int idx, Color couleur,
                                String label, int y) {
        int xCentre = MARGE_GAUCHE
                + idx * (LARGEUR_CASE + ESPACEMENT)
                + LARGEUR_CASE / 2;
        g2.setColor(couleur);
        g2.setFont(new Font("SansSerif", Font.BOLD, 11));
        String texte = "↑ " + label;
        int largeur = g2.getFontMetrics().stringWidth(texte);
        g2.drawString(texte, xCentre - largeur / 2, y + 10);
    }
}