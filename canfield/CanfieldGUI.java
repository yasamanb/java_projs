package canfield;

import ucb.gui.TopLevel;
import ucb.gui.LayoutSpec;

import java.awt.event.MouseEvent;

/** A top-level GUI for Canfield solitaire.
 *  @author Yasaman Bahri
 */
class CanfieldGUI extends TopLevel {

    /** A new window with given TITLE and displaying GAME. */
    CanfieldGUI(String title, Game game) {
        super(title, true);
        _game = game;
        addMenuButton("Menu->Undo", "undoGUI");
        addMenuButton("Menu->Quit", "quit");
        _display = new GameDisplay(game);
        add(_display, new LayoutSpec("y", 2, "width", 2));
        _display.setMouseHandler("click", this, "mouseClicked");
        _display.setMouseHandler("release", this, "mouseReleased");
        _display.setMouseHandler("drag", this, "mouseDragged");
        _display.setMouseHandler("press", this, "mousePressed");
        display(true);
    }

    /** Respond to "Quit" button. */
    public void quit(String dummy) {
        System.exit(1);
    }


    /** Undo methods for the GUI, which includes repaint. */
    public void undoGUI(String dummy) {
        _game.undo();
        _display.repaint();
    }


     /** Action in response to mouse-clicking event EVENT. */
    public synchronized void mouseClicked(MouseEvent event) {
        String[] click_loc_name = _display.classifyPoint(event.getX(), event.getY());
        if (click_loc_name[0].equals("stock")) {
            _game.stockToWaste();
        }
        _display.repaint();
    }

    /** Action in response to mouse-released event EVENT. */
    public synchronized void mouseReleased(MouseEvent event) {
        String[] release_loc_name = _display.classifyPoint(event.getX(), event.getY());
        if (drag_motion) {
            if (pressed_loc_name[0].equals("reserve")) {
                if (release_loc_name[0].equals("found")) {
                    _game.reserveToFoundation();
                }
                if (release_loc_name[0].equals("tableau")) {
                    _game.reserveToTableau(Integer.parseInt(release_loc_name[1]) + 1);
                }
            } else if (pressed_loc_name[0].equals("waste")) {
                if (release_loc_name[0].equals("found")) {
                    _game.wasteToFoundation();
                } else if (release_loc_name[0].equals("tableau")) {
                    _game.wasteToTableau(Integer.parseInt(release_loc_name[1]) + 1);
                }
            } else if (pressed_loc_name[0].equals("found")) {
                if (release_loc_name[0].equals("tableau")) {
                    _game.foundationToTableau(Integer.parseInt(pressed_loc_name[1]) + 1, Integer.parseInt(release_loc_name[1]) + 1);
                }
            } else if (pressed_loc_name[0].equals("tableau")) {
                if (release_loc_name[0].equals("found")) {
                    _game.tableauToFoundation(Integer.parseInt(pressed_loc_name[1]) + 1);
                } else if (release_loc_name[0].equals("tableau")) {
                    _game.tableauToTableau(Integer.parseInt(pressed_loc_name[1]) + 1, Integer.parseInt(release_loc_name[1]) + 1);
                }
            }
        }

        _display.repaint();
        drag_motion = false;
    }

    /** Action in response to mouse-dragging event EVENT. */
    public synchronized void mouseDragged(MouseEvent event) {
        drag_motion = true; 
        _display.repaint();
    }




    /** My added method. */
    public synchronized void mousePressed(MouseEvent event) {
        pressed_loc_name = _display.classifyPoint(event.getX(), event.getY());
        _display.repaint();
    }

    /** The board widget. */
    private final GameDisplay _display;

    /** The game I am consulting. */
    private final Game _game;


    /**This tells us our current motion corresponds to drag. Is resest immediatley after a release is detected. */
    private boolean drag_motion;

    /** The string array classifying the location. First entry is a string name, second tells us the pile. */
    private String[] pressed_loc_name;

}
