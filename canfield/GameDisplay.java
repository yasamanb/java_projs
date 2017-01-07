package canfield;

import ucb.gui.Pad;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;

import javax.imageio.ImageIO;

import java.io.InputStream;
import java.io.IOException;

/** A widget that displays a Pinball playfield.
 *  @author P. N. Hilfinger
 */
class GameDisplay extends Pad {

    /** Color of display field. */
    private static final Color BACKGROUND_COLOR = Color.white;

    /* Coordinates and lengths in pixels unless otherwise stated. */

    /** Preferred dimensions of the playing surface. */
    private static final int BOARD_WIDTH = 700, BOARD_HEIGHT = 1000;

    /** Displayed dimensions of a card image. */
    private static final int CARD_HEIGHT = 125, CARD_WIDTH = 90;

    /** Board location of foundation piles. */
    private static int[] foundX = {300, 400, 500, 600}, foundY = {100, 100, 100, 100};

    /** Location of reserve pile. */
    private static int reserveX = 50, reserveY = 300;

    /** Location of stock pile. */
    private static int stockX = 50, stockY = 450;

    /** Location of waste pile. */
    private static int wasteX = 150, wasteY = 450;

    /** Location of tableau piles. */
    private static int[] tableauX = {300, 400, 500, 600}, tableauY = {300, 300, 300, 300};

    /** A graphical representation of GAME. */
    public GameDisplay(Game game) {
        _game = game;
        setPreferredSize(BOARD_WIDTH, BOARD_HEIGHT);
    }

    /** Return an Image read from the resource named NAME. */
    private Image getImage(String name) {
        InputStream in =
            getClass().getResourceAsStream("/canfield/resources/" + name);
        try {
            return ImageIO.read(in);
        } catch (IOException excp) {
            return null;
        }
    }

    /** Return an Image of CARD. */
    private Image getCardImage(Card card) {
        return getImage("playing-cards/" + card + ".png");
    }

    /** Return an Image of the back of a card. */
    private Image getBackImage() {
        return getImage("playing-cards/blue-back.png");
    }

    /** Draw CARD at X, Y on G. */
    private void paintCard(Graphics2D g, Card card, int x, int y) {
        if (card != null) {
            g.drawImage(getCardImage(card), x, y,
                        CARD_WIDTH, CARD_HEIGHT, null);
        }
    }

    /** Draw card back at X, Y on G. */
    private void paintBack(Graphics2D g, int x, int y) {
        g.drawImage(getBackImage(), x, y, CARD_WIDTH, CARD_HEIGHT, null);
    }

    @Override
    public synchronized void paintComponent(Graphics2D g) {
        g.setColor(BACKGROUND_COLOR);
        Color LINE_COLOR = Color.black;
        Rectangle b = g.getClipBounds();
        g.fillRect(0, 0, b.width, b.height);
        g.setColor(LINE_COLOR);
        paintBlanks(g);
        paintReserve(g);
        paintFound(g);
        paintStock(g);
        paintWaste(g);
        paintTableau(g);
    }

    /** Method paints blank rectangles on board. */
    private void paintBlanks(Graphics2D g) {
        for (int i = 0; i < foundX.length; i++) {
            g.drawRoundRect(foundX[i], foundY[i], CARD_WIDTH, CARD_HEIGHT, 10, 10);
            g.drawRoundRect(tableauX[i], tableauY[i], CARD_WIDTH, CARD_HEIGHT, 10, 10);
        }
        g.drawRoundRect(stockX, stockY, CARD_WIDTH, CARD_HEIGHT, 10, 10);
        g.drawRoundRect(wasteX, wasteY, CARD_WIDTH, CARD_HEIGHT, 10, 10);
        g.drawRoundRect(reserveX, reserveY, CARD_WIDTH, CARD_HEIGHT, 10, 10);
    }



    /** Paints reserve. */
    private void paintReserve(Graphics2D g) {
        paintCard(g, _game.topReserve(), reserveX,reserveY);
    }

    /** Paints foundation. */
    private void paintFound(Graphics2D g) {
        for (int i = 0; i < foundX.length; i++) {
            paintCard(g, _game.topFoundation(i + 1), foundX[i], foundY[i]);
        }
    }

    /** Paints stock. */
    private void paintStock(Graphics2D g) {
        if (!_game.stockEmpty()) {
            paintBack(g, stockX, stockY);
        }
    }

    /** Paints waste. */ 
    private void paintWaste(Graphics2D g) {
        paintCard(g, _game.topWaste(), wasteX, wasteY);
    }

    /** Paints tableau. */
    private void paintTableau(Graphics2D g) {
        for (int i = 0; i < tableauX.length; i++) {
            for (int j = 0; j <_game.tableauSize(i + 1); j++) {
                paintCard(g, _game.getTableau(i + 1, _game.tableauSize(i + 1) - j - 1), tableauX[i], (tableauY[i] + shift_y * j));
            }
        }
    }


    /** Classifies the location of the mouse event to a string. */
    public String[] classifyPoint(int X, int Y) {
        if ((stockX <= X) && (X <= stockX + CARD_WIDTH) && (stockY <= Y) && (Y <= stockY + CARD_HEIGHT)) {
            return new String[] {"stock"};
        } else if ((reserveX <= X) && (X <= reserveX + CARD_WIDTH) && (reserveY <= Y) && (Y <= reserveY + CARD_HEIGHT)) {
            return new String[] {"reserve"};
        } else if ((wasteX <= X) && (X <= wasteX + CARD_WIDTH) && (wasteY <= Y) && (Y <= wasteY + CARD_HEIGHT)) {
            return new String[] {"waste"};
        } else { 
            for (int i = 0; i < foundX.length; i++) {
                if ((foundX[i] <= X) && (X <= foundX[i] + CARD_WIDTH) && (foundY[i] <= Y) && (Y <= foundY[i] + CARD_HEIGHT)) {
                    return new String[] {"found", Integer.toString(i)};
                }
            }
            for (int i = 0; i < tableauX.length; i++) {
                int yshift = CARD_HEIGHT + (_game.tableauSize(i + 1) - 1) * shift_y;
                if ((tableauX[i] <= X) && (X <= tableauX[i] + CARD_WIDTH) && (tableauY[i] <= Y) && (Y <= tableauY[i] + yshift)) {
                    return new String[] {"tableau", Integer.toString(i)};
                }
            }
            return new String[] {"outside"};
        }
    }





    /** Game I am displaying. */
    private final Game _game;

    /** An integer to shift the y-displacement of the tableau cards by. */
    private final int shift_y = 40;


}
