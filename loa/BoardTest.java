package loa;

import org.junit.Test;
import static org.junit.Assert.*;

import static loa.Piece.*;
import static loa.Direction.*;
import java.util.Iterator;

/** Full unit testing of the Board Class.
 *  @author Yasaman Bahri
 */
public class BoardTest {

    static final int M = 8;

    /** This test checks that the incr method in internal MoveIterator
    class works. */
    @Test
    public void iteratorTest() {
        Piece[][] mypieces = new Piece[8][8];
        for (int c = 0; c < M; c++) {
            for (int r = 0; r < M; r++) {
                mypieces[r][c] = EMP;
            }
        }
        mypieces[2][7] = BP;
        mypieces[3][7] = BP;
        Board myboard = new Board(mypieces, BP);
        Iterator<Move> myiter = myboard.iterator();
        int numOfMoves = 0;
        while (myiter.hasNext()) {
            myiter.next();
            numOfMoves++;
        }
        assertEquals(numOfMoves, 10);
        myboard = new Board();
        myiter = myboard.iterator();
        numOfMoves = 0;
        while (myiter.hasNext()) {
            myiter.next();
            numOfMoves++;
        }
        assertEquals(numOfMoves, 36);
    }

    /** This test checks the copyFrom(), get(), and set() methods. */
    @Test
    public void copySetGetTest() {
        Board bd1 = new Board();
        Piece[][] mypieces = new Piece[8][8];
        for (int c = 0; c < M; c++) {
            for (int r = 0; r < M; r++) {
                mypieces[r][c] = EMP;
            }
        }
        mypieces[2][7] = BP;
        mypieces[3][7] = BP;
        Board bd2 = new Board(mypieces, WP);
        bd2.copyFrom(bd1);
        Piece[][] lmat = bd2.getLayout();
        assertEquals(lmat[2][7], WP);
        for (int c = 0; c < M; c++) {
            for (int r = 0; r < M; r++) {
                assertEquals(bd2.getLayout()[r][c], bd1.getLayout()[r][c]);
            }
        }
        assertEquals(bd2.getTurn(), bd1.getTurn());
        bd2.set(3, 3, WP, WP);
        assertEquals(bd1.get(3, 3), EMP);
        assertEquals(bd1.getTurn(), BP);
        assertEquals(bd2.get(3, 3), WP);
        assertEquals(bd2.getTurn(), WP);
    }

    /** This test checks piecesContiguous. */
    @Test
    public void contigTest() {
        Piece[][] mypieces = new Piece[8][8];
        for (int c = 0; c < M; c++) {
            for (int r = 0; r < M; r++) {
                mypieces[r][c] = EMP;
            }
        }
        mypieces[2][7] = BP;
        mypieces[3][7] = BP;
        mypieces[2][6] = BP;
        mypieces[2][5] = BP;
        mypieces[3][4] = BP;
        mypieces[1][1] = WP;
        mypieces[5][1] = WP;
        Board bd2 = new Board(mypieces, BP);
        assertEquals(bd2.piecesContiguous(BP), true);
        assertEquals(bd2.piecesContiguous(WP), false);
        Move move1 = Move.create(8, 6, 5, 6, bd2);
        Move move2 = Move.create(8, 6, 6, 6, bd2);
        assertEquals(bd2.isLegal(move1), true);
        assertEquals(bd2.isLegal(move2), false);
    }

    /** This test checks pieceCountAlong, which was (permanently) changed
    from private to package private. */
    @Test
    public void piececountTest() {
        Board bd1 = new Board();
        assertEquals(bd1.pieceCountAlong(1, 1, NE), 0);
        assertEquals(bd1.pieceCountAlong(1, 1, N), 6);
        assertEquals(bd1.pieceCountAlong(1, 1, SW), 0);
        assertEquals(bd1.pieceCountAlong(7, 8, W), 6);
        assertEquals(bd1.pieceCountAlong(7, 8, E), 6);
        assertEquals(bd1.pieceCountAlong(7, 8, SE), 2);
        assertEquals(bd1.pieceCountAlong(7, 8, NW), 2);
        assertEquals(bd1.pieceCountAlong(7, 8, SW), 2);
        assertEquals(bd1.pieceCountAlong(7, 8, NE), 2);
        assertEquals(bd1.pieceCountAlong(4, 5, N), 2);
        assertEquals(bd1.pieceCountAlong(4, 5, S), 2);
        assertEquals(bd1.pieceCountAlong(4, 5, NE), 2);
        assertEquals(bd1.pieceCountAlong(4, 5, SW), 2);
    }

    /** This test checks the blocked method, which was (permanently) changed
    from private to package private. */
    @Test
    public void blockedTest() {
        Piece[][] mypieces = new Piece[8][8];
        for (int c = 0; c < M; c++) {
            for (int r = 0; r < M; r++) {
                mypieces[r][c] = EMP;
            }
        }
        mypieces[2][7] = BP;
        mypieces[3][7] = BP;
        mypieces[2][6] = BP;
        mypieces[2][5] = BP;
        mypieces[3][4] = BP;
        mypieces[1][1] = WP;
        mypieces[5][1] = WP;
        mypieces[3][1] = BP;
        Board bd2 = new Board(mypieces, WP);
        Move move1 = Move.create("b7-b4", bd2);
        assertEquals(bd2.blocked(move1), true);
        Move move2 = Move.create(2, 7, 2, 4, bd2);
        assertEquals(bd2.blocked(move2), true);
    }
}
