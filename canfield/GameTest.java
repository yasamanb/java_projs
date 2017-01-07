package canfield;

import static org.junit.Assert.*;
import org.junit.Test;

/** Tests of the Game class.
 *  @author Yasaman Bahri
 */

public class GameTest {

    /** Example. */
    @Test
    public void testInitialScore() {
        Game g = new Game();
        g.deal();
        assertEquals(5, g.getScore());
    }


    /** A unit test for undo method. */
    @Test
    public void testUndo() {
    	Game _mygame = new Game();
    	_mygame.seed(110001);
    	_mygame.deal();
    	_mygame.reserveToFoundation();
    	_mygame.reserveToTableau(4);
    	_mygame.undo();
    	assertEquals(_mygame.topReserve().toString(), "8C");
    	assertEquals(_mygame.topWaste(), null);
    	assertEquals(_mygame.topTableau(1).toString(), "AS");
    	assertEquals(_mygame.topTableau(2).toString(), "10D");
    	assertEquals(_mygame.topTableau(3).toString(), "5H");
    	assertEquals(_mygame.topTableau(4).toString(), "9D");
    	assertEquals(_mygame.topFoundation(1).toString(), "JC");
    	assertEquals(_mygame.topFoundation(2).toString(), "JH");
    	assertEquals(_mygame.topFoundation(3), null);
    	assertEquals(_mygame.topFoundation(4), null);
    }
}
