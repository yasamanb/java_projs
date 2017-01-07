package loa;

/** A Player that prompts for moves and reads them from its Game.
 *  @author Yasaman Bahri */
class HumanPlayer extends Player {

    /** A HumanPlayer that plays the SIDE pieces in GAME.  It uses
     *  GAME.getMove() as a source of moves.  */
    HumanPlayer(Piece side, Game game) {
        super(side, game);
        _side = this.side();
        _game = this.getGame();
    }

    @Override
    Move makeMove() {
        return _game.getMove();
    }

    /** This player's side. */
    private final Piece _side;
    /** The game this player is part of. */
    private Game _game;

}
