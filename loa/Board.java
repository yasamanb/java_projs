package loa;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Formatter;
import java.util.NoSuchElementException;

import java.util.regex.Pattern;

import static loa.Piece.*;
import static loa.Direction.*;

/** Represents the state of a game of Lines of Action.
 *  @author Yasaman Bahri
 */
class Board implements Iterable<Move> {

    /** Size of a board. */
    static final int M = 8;

    /** Pattern describing a valid square designator (cr). */
    static final Pattern ROW_COL = Pattern.compile("^[a-h][1-8]$");

    /** A Board whose initial contents are taken from INITIALCONTENTS
     *  and in which the player playing TURN is to move. The resulting
     *  Board has
     *        get(col, row) == INITIALCONTENTS[row-1][col-1]
     *  Assumes that PLAYER is not null and INITIALCONTENTS is MxM.
     *
     *  CAUTION: The natural written notation for arrays initializers puts
     *  the BOTTOM row of INITIALCONTENTS at the top.
     */
    Board(Piece[][] initialContents, Piece turn) {
        initialize(initialContents, turn);
    }

    /** A new board in the standard initial position. */
    Board() {
        clear();
    }

    /** A Board whose initial contents and state are copied from
     *  BOARD. */
    Board(Board board) {
        copyFrom(board);
    }

    /** Set my state to CONTENTS with SIDE to move. NOTE: The (r, c) element
    on the board will correspond to contents[M-r][c-1]. Use appropriately. */
    void initialize(Piece[][] contents, Piece side) {
        _moves.clear();
        for (int r = 1; r <= M; r += 1) {
            for (int c = 1; c <= M; c += 1) {
                set(c, r, contents[M - r][c - 1]);
            }
        }
        _turn = side;
    }

    /** Set me to the initial configuration. */
    void clear() {
        initialize(INITIAL_PIECES, BP);
    }

    /** Set my state to a copy of BOARD. */
    void copyFrom(Board board) {
        if (board == this) {
            return;
        }
        _moves.clear();
        _moves.addAll(board.getMoves());
        _turn = board.getTurn();
        Piece[][] temp = board.getLayout();
        for (int r = 1; r <= M; r += 1) {
            for (int c = 1; c <= M; c += 1) {
                _layout[M - r][c - 1] = temp[M - r][c - 1];
            }
        }
    }

    /** Getters for the private fields. This returns _moves. */
    ArrayList<Move> getMoves() {
        return _moves;
    }

    /** Returns current _turn for board. */
    Piece getTurn() {
        return _turn;
    }

    /** Returns the layout of the board. Note that this matrix printed
    looks just like the board but follows different indexing. */
    Piece[][] getLayout() {
        return _layout;
    }

    /** Return the contents of column C, row R, where 1 <= C,R <= 8,
     *  where column 1 corresponds to column 'a' in the standard
     *  notation. */
    Piece get(int c, int r) {
        return _layout[M - r][c - 1];
    }

    /** Return the contents of the square SQ.  SQ must be the
     *  standard printed designation of a square (having the form cr,
     *  where c is a letter from a-h and r is a digit from 1-8). */
    Piece get(String sq) {
        return get(col(sq), row(sq));
    }

    /** Return the column number (a value in the range 1-8) for SQ.
     *  SQ is as for {@link get(String)}. */
    static int col(String sq) {
        if (!ROW_COL.matcher(sq).matches()) {
            throw new IllegalArgumentException("bad square designator");
        }
        return sq.charAt(0) - 'a' + 1;
    }

    /** Return the row number (a value in the range 1-8) for SQ.
     *  SQ is as for {@link get(String)}. */
    static int row(String sq) {
        if (!ROW_COL.matcher(sq).matches()) {
            throw new IllegalArgumentException("bad square designator");
        }
        return sq.charAt(1) - '0';
    }

    /** Set the square at column C, row R to V, and make NEXT the next side
     *  to move, if it is not null. */
    void set(int c, int r, Piece v, Piece next) {
        _layout[M - r][c - 1] = v;
        if (next != null) {
            _turn = next;
        }
    }

    /** Set the square at column C, row R to V. */
    void set(int c, int r, Piece v) {
        set(c, r, v, null);
    }

    /** Assuming isLegal(MOVE), make MOVE. */
    void makeMove(Move move) {
        assert isLegal(move);
        _moves.add(move);
        Piece replaced = move.replacedPiece();
        int c0 = move.getCol0(), c1 = move.getCol1();
        int r0 = move.getRow0(), r1 = move.getRow1();
        if (replaced != EMP) {
            set(c1, r1, EMP);
        }
        set(c1, r1, move.movedPiece());
        set(c0, r0, EMP);
        _turn = _turn.opposite();
    }

    /** Retract (unmake) one move, returning to the state immediately before
     *  that move.  Requires that movesMade () > 0. */
    void retract() {
        assert movesMade() > 0;
        Move move = _moves.remove(_moves.size() - 1);
        Piece replaced = move.replacedPiece();
        int c0 = move.getCol0(), c1 = move.getCol1();
        int r0 = move.getRow0(), r1 = move.getRow1();
        Piece movedPiece = move.movedPiece();
        set(c1, r1, replaced);
        set(c0, r0, movedPiece);
        _turn = _turn.opposite();
    }

    /** Return the Piece representing who is next to move. */
    Piece turn() {
        return _turn;
    }

    /** Return true iff MOVE is legal for the player currently on move. Note
    that it checks for nullity.*/
    boolean isLegal(Move move) {
        try {
            return (pieceCountAlong(move) == move.length() && !blocked(move));
        } catch (NullPointerException e) {
            return false;
        }
    }

    /** Return a sequence of all legal moves from this position. */
    Iterator<Move> legalMoves() {
        return new MoveIterator();
    }

    @Override
    public Iterator<Move> iterator() {
        return legalMoves();
    }

    /** Return true if there is at least one legal move for the player
     *  on move. */
    public boolean isLegalMove() {
        return iterator().hasNext();
    }

    /** Return true iff either player has all his pieces continguous. */
    boolean gameOver() {
        return piecesContiguous(BP) || piecesContiguous(WP);
    }

    /** Return true iff SIDE's pieces are contiguous. */
    boolean piecesContiguous(Piece side) {
        Piece[][] bLayout = new Piece[M][M];
        for (int c = 0; c < M; c++) {
            for (int r = 0; r < M; r++) {
                bLayout[r][c] = _layout[r][c];
            }
        }
        int startR = 1;
        int startC = 1;
        someLabel:
        for (int c = 1; c <= M; c++) {
            for (int r = 1; r <= M; r++) {
                if (bLayout[M - r][c - 1] == side) {
                    startR = r;
                    startC = c;
                    break someLabel;
                }
            }
        }
        bLayout = markSpots(startC, startR, bLayout, side);
        for (int c = 1; c <= M; c++) {
            for (int r = 1; r <= M; r++) {
                if (bLayout[M - r][c - 1] == side) {
                    return false;
                }
            }
        }
        return true;
    }

    /** Helper method for piecesContiguous method. Takes a starting C, R and
    changes the connected pieces of SIDE to EMP in the matrix BLAYOUT.
    Made private. Returns the altered matrix.*/
    private Piece[][] markSpots(int c, int r, Piece[][] bLayout, Piece side) {
        bLayout[M - r][c - 1] = EMP;
        Direction dir = NOWHERE;
        int nbR;
        int nbC;
        dir = dir.succ();
        while (dir != null) {
            nbC = c + dir.dc;
            nbR = r + dir.dr;
            if (Move.inBounds(nbC, nbR)) {
                if (bLayout[M - nbR][nbC - 1] == side) {
                    bLayout[M - nbR][nbC - 1] = EMP;
                    markSpots(nbC, nbR, bLayout, side);
                }
            }
            dir = dir.succ();
        }
        return bLayout;
    }


    /** Helper method for MachinePlayer. Counts number of
    connected components on a board for SIDE and returns the
    integer >= 0. */
    int connComponent(Piece side) {
        Piece[][] bLayout = new Piece[M][M];
        for (int c = 0; c < M; c++) {
            for (int r = 0; r < M; r++) {
                bLayout[r][c] = _layout[r][c];
            }
        }
        int startR = 1;
        int startC = 1;
        int num = 0;
        myWhileLoop:
        while (true) {
            myForLoop:
            for (int c = 1; c <= M; c++) {
                for (int r = 1; r <= M; r++) {
                    if (bLayout[M - r][c - 1] == side) {
                        startR = r;
                        startC = c;
                        num = num + 1;
                        break myForLoop;
                    }
                    if (c == M && r == M) {
                        break myWhileLoop;
                    }
                }
            }
            bLayout = markSpots(startC, startR, bLayout, side);
        }
        return num;
    }

    /** Return the total number of moves that have been made (and not
     *  retracted).  Each valid call to makeMove with a normal move increases
     *  this number by 1. */
    int movesMade() {
        return _moves.size();
    }

    @Override
    public boolean equals(Object obj) {
        Board b = (Board) obj;
        boolean matEq = true;
        Piece[][] compL = b.getLayout();
        outerLoop:
        for (int r = 0; r < M; r++) {
            for (int c = 0; c < M; c++) {
                if (compL[r][c] != _layout[r][c]) {
                    matEq = false;
                    break outerLoop;
                }
            }
        }
        boolean pieceEq = false;
        if (b.getTurn() == _turn) {
            pieceEq = true;
        }
        boolean movesEq = false;
        if (b.getMoves().equals(_moves)) {
            movesEq = true;
        }
        return (movesEq && pieceEq && matEq);
    }

    @Override
    public int hashCode() {
        double sum = 0;
        for (int r = 0; r < M; r++) {
            for (int c = 0; c < M; c++) {
                sum = (sum + Math.pow(3, r * c)
                    * _layout[r][c].ordinal()) % Integer.MAX_VALUE;
            }
        }
        return (int) sum;
    }

    @Override
    public String toString() {
        Formatter out = new Formatter();
        out.format("===%n");
        for (int r = M; r >= 1; r -= 1) {
            out.format("    ");
            for (int c = 1; c <= M; c += 1) {
                out.format("%s ", get(c, r).abbrev());
            }
            out.format("%n");
        }
        out.format("Next move: %s%n===", turn().fullName());
        return out.toString();
    }

    /** Return the number of pieces in the line of action
    indicated by MOVE. Changed from private to package private. */
    int pieceCountAlong(Move move) {
        Direction dir = move.getDir();
        int c = move.getCol0();
        int r = move.getRow0();
        return pieceCountAlong(c, r, dir);
    }

    /** Return the number of pieces in the line of action in direction DIR and
     *  containing the square at column C and row R. Changed from private
    to package private. Note that it does not assume a piece is on c, r. */
    int pieceCountAlong(int c, int r, Direction dir) {
        int[] sign  = new int[] {1, -1};
        int currC;
        int currR;
        int num = 0;
        if (_layout[M - r][c - 1] != EMP) {
            num += 1;
        }
        for (int sgn : sign) {
            currC = c + sgn * dir.dc;
            currR = r + sgn * dir.dr;
            while (Move.inBounds(currC, currR)) {
                if (_layout[M - currR][currC - 1] != EMP) {
                    num += 1;
                }
                currC = currC + sgn * dir.dc;
                currR = currR + sgn * dir.dr;
            }
        }
        return num;
    }

    /** Return true iff MOVE is blocked by an opposing piece or by a
     *  friendly piece on the target square. Changed from private
    to package private. Note it doesn't check for null moves.*/
    boolean blocked(Move move) {
        Direction dir = move.getDir();
        int r = move.getRow0();
        int c = move.getCol0();
        Piece opp = _turn.opposite();
        for (int i = 1; i <= move.length(); i++) {
            r = r + dir.dr;
            c = c + dir.dc;
            if (i < move.length()) {
                if (opp == _layout[M - r][c - 1]) {
                    return true;
                }
            } else {
                if (_turn == _layout[M - r][c - 1]) {
                    return true;
                }
            }
        }
        return false;
    }

    /** The standard initial configuration for Lines of Action. */
    static final Piece[][] INITIAL_PIECES = {
        { EMP, BP,  BP,  BP,  BP,  BP,  BP,  EMP },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { EMP, BP,  BP,  BP,  BP,  BP,  BP,  EMP }
    };

    /** List of all unretracted moves on this board, in order. */
    private final ArrayList<Move> _moves = new ArrayList<>();
    /** Current side on move. */
    private Piece _turn;

    /** My addition. Data structure to hold board pieces, 2D array. */
    private Piece[][] _layout = new Piece[M][M];

    /** An iterator returning the legal moves from the current board. */
    private class MoveIterator implements Iterator<Move> {
        /** Current piece under consideration. */
        private int _c, _r;
        /** Next direction of current piece to return. */
        private Direction _dir;
        /** Next move. */
        private Move _move;

        /** A new move iterator for turn(). */
        MoveIterator() {
            _c = 1; _r = 1; _dir = NOWHERE;
            incr();
        }

        @Override
        public boolean hasNext() {
            return _move != null;
        }

        @Override
        public Move next() {
            if (_move == null) {
                throw new NoSuchElementException("no legal move");
            }

            Move move = _move;
            incr();
            return move;
        }

        @Override
        public void remove() {
        }

        /** Advance to the next legal move. */
        private void incr() {
            int endc;
            int endr;
            Board dummyBoard = new Board(_layout, _turn);
            for (int cc = _c; cc <= M; cc++) {
                for (int rr = _r; rr <= M; rr++) {
                    if (_layout[M - rr][cc - 1] == _turn) {
                        for (_dir = _dir.succ(); _dir != null;
                            _dir = _dir.succ()) {
                            int nums = pieceCountAlong(cc, rr, _dir);
                            endc = cc + _dir.dc * nums;
                            endr = rr + _dir.dr * nums;
                            Move suggested =
                                Move.create(cc, rr, endc, endr, dummyBoard);
                            if (suggested != null
                                && dummyBoard.isLegal(suggested)) {
                                _move = suggested;
                                _r = rr;
                                _c = cc;
                                return;
                            }
                        }
                        _dir = NOWHERE;
                    }
                    if (_r == 8) {
                        _r = 1;
                    }
                }
            }
            _move = null;
        }
    }
}
