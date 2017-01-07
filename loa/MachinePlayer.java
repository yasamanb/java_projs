package loa;

import java.util.Iterator;
import static loa.Piece.*;
import static loa.Direction.*;

/** An automated Player.
 *  @author Yasaman Bahri*/
class MachinePlayer extends Player {

    /** A MachinePlayer that plays the SIDE pieces in GAME. */
    MachinePlayer(Piece side, Game game) {
        super(side, game);
        _side = this.side();
        _game = this.getGame();
        _currentbest = null;
    }


    @Override
    Move makeMove() {
        Board copy = new Board(_game.getBoard());
        double val = alphabeta(copy, maxDepth,
            Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, true);
        if (_currentbest != null && _game.getBoard().isLegal(_currentbest)) {
            System.out.println(_side.abbrev().toUpperCase()
                + "::" + _currentbest);
        }
        return _currentbest;
    }

    /** Alpha-beta pruning method. Important note: at intermediate levels,
    even if heuristic shows game is won, I continue running till the full
    depth (playing even past game won), even though this is inefficient.
    This code allows me to easily change to a more general heuristic later.
    Returns DOUBLE corresponds to value of best move. BD is board,
    DEPTH is depth of calculation, ALPHA, BETA are pruning parameters,
    MAXPLAYER is true if the player in question wishes to maximize
    the evaluation result. */
    double alphabeta(Board bd, int depth, double alpha,
        double beta, boolean maxplayer) {
        if (depth == 0) {
            return evaluateGame(bd, _side);
        }
        Iterator<Move> bditer = bd.iterator();
        if (maxplayer) {
            double v = Double.NEGATIVE_INFINITY;
            double vold = Double.NEGATIVE_INFINITY;
            double alphaOld = alpha;
            while (bditer.hasNext()) {
                Move move = bditer.next();
                bd.makeMove(move);
                vold = v;
                v = Math.max(v, alphabeta(bd, depth - 1, alpha,
                    beta, !maxplayer));
                alphaOld = alpha;
                alpha = Math.max(v, alpha);
                if (depth == maxDepth && (alphaOld != alpha)) {
                    _currentbest = move;
                }
                if (depth == maxDepth && bd.piecesContiguous(_side)) {
                    break;
                }
                bd.retract();
                if (beta <= alpha) {
                    break;
                }
            }
            return v;
        } else {
            double v = Double.POSITIVE_INFINITY;
            double vold = Double.POSITIVE_INFINITY;
            double betaOld = beta;
            while (bditer.hasNext()) {
                Move move = bditer.next();
                bd.makeMove(move);
                vold = v;
                v = Math.min(v, alphabeta(bd,
                    depth - 1, alpha, beta, !maxplayer));
                betaOld = beta;
                beta = Math.min(v, beta);
                if (depth == maxDepth && (betaOld != beta)) {
                    _currentbest = move;
                }
                bd.retract();
                if (beta <= alpha) {
                    break;
                }
            }
            return v;
        }
    }

    /** Helper function for alphabeta which takes in board
    BD and piece SIDE and evaluates if game is won from
    some point of view. Returns the integer evaluation. */
    double evaluateGame1(Board bd, Piece side) {
        if (bd.piecesContiguous(side)) {
            return 1.0;
        } else if (bd.piecesContiguous(side.opposite())) {
            return -1.0;
        } else {
            return 0.0;
        }
    }

    /** A different helper evaluation function for alphabeta.
    This counts the number of connected components on
    the BD with piece SIDE. It returns a numerical float
    derived from the integer result.*/
    double evaluateGame(Board bd, Piece side) {
        int numConn = bd.connComponent(side);
        return (double) 1.0 / numConn;
    }

    /** This player's side. */
    private final Piece _side;
    /** The game this player is part of. */
    private Game _game;
    /** A move, either it is the last returned or currently
    being modified/computed for being returned by makeMove(). */
    protected Move _currentbest;
    /** Max depth to be using for minimax/alphabeta pruning tree. */
    protected int maxDepth = 3;

}
