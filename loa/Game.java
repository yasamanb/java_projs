package loa;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import static loa.Piece.*;
import static loa.Main.*;

/** Represents one game of Lines of Action.
 *  @author Yasaman Bahri  */
class Game {

    /** A new series of Games. */
    Game() {
        _randomSource = new Random();

        _players = new Player[2];
        _input = new BufferedReader(new InputStreamReader(System.in));
        _players[0] = new HumanPlayer(BP, this);
        _players[1] = new MachinePlayer(WP, this);
        _playing = false;
    }

    /** Return the current board. */
    Board getBoard() {
        return _board;
    }

    /** Quit the game. */
    private void quit() {
        System.exit(0);
    }

    /** Return a move.  Processes any other intervening commands as
     *  well.  Exits with null if the value of _playing changes. */
    Move getMove() {
        try {
            boolean playing0 = _playing;
            while (_playing == playing0) {
                prompt();
                String line = _input.readLine();
                if (line == null) {
                    quit();
                }

                line = line.trim();
                if (!processCommand(line)) {
                    try {
                        Move move = Move.create(line, _board);
                        if (move == null) {
                            error("invalid move: %s%n", line);
                        } else if (!_playing) {
                            error("game not started %n");
                        } else if (!_board.isLegal(move)) {
                            error("illegal move: %s%n", line);
                        } else {
                            return move;
                        }
                    } catch (IllegalArgumentException
                        | ArrayIndexOutOfBoundsException e) {
                        return null;
                    }
                }

            }
        } catch (IOException excp) {
            error(1, "unexpected I/O error on input");
        }
        return null;
    }

    /** Print a prompt for a move. */
    private void prompt() {
        System.out.print("> ");
        System.out.flush();
    }

    /** Describes a command with up to two arguments. */
    private static final Pattern COMMAND_PATN =
        Pattern.compile("(#|\\S+)\\s*(\\S*)\\s*(\\S*).*");

    /** If LINE is a recognized command other than a move, process it
     *  and return true.  Otherwise, return false. */
    private boolean processCommand(String line) {
        if (line.length() == 0) {
            return true;
        }
        Matcher command = COMMAND_PATN.matcher(line);
        if (command.matches()) {
            switch (command.group(1).toLowerCase()) {
            case "#":
                return true;
            case "manual":
                manualCommand(command.group(2).toLowerCase());
                return true;
            case "auto":
                autoCommand(command.group(2).toLowerCase());
                return true;
            case "seed":
                seedCommand(command.group(2));
                return true;
            case "clear":
                clearCommand();
                return true;
            case "start":
                startCommand();
                return true;
            case "dump":
                System.out.println(_board);
                return true;
            case "set":
                setCommand(command.group(2).toLowerCase(),
                    command.group(3).toLowerCase());
                return true;
            case "help": case "?":
                help();
                return true;
            case "quit":
                quit();
                return true;
            default:
                return false;
            }
        }
        return false;
    }

    /** Set player PLAYER ("white" or "black") to be a manual player. */
    private void manualCommand(String player) {
        try {
            Piece s = Piece.playerValueOf(player);
            _playing = false;
            _players[s.ordinal()] = new HumanPlayer(s, this);
        } catch (IllegalArgumentException excp) {
            error("unknown player: %s%n", player);
        }
    }

    /** Set player PLAYER ("white" or "black") to be an automated player. */
    private void autoCommand(String player) {
        try {
            Piece s = Piece.playerValueOf(player);
            _playing = false;
            _players[s.ordinal()] = new MachinePlayer(s, this);
        } catch (IllegalArgumentException excp) {
            error("unknown player: %s%n", player);
        }
    }

    /** Seed random-number generator with SEED (as a long). */
    private void seedCommand(String seed) {
        try {
            _randomSource.setSeed(Long.parseLong(seed));
        } catch (NumberFormatException excp) {
            error("Invalid number: %s", seed);
        }
    }

    /** My added method for clear. Abandons current game if in
    progress, clears the board, and playing is stopped. */
    private void clearCommand() {
        _playing = false;
        _board.clear();
    }

    /** My added method for starting. If _playing is false, begin
    playing from current positions. */
    private void startCommand() {
        if (_playing) {
            return;
        } else {
            _playing = true;
        }
    }

    /**My added method for set command. Sets the contents of
    SQUARE cr to p, which is either SIDE "b", "w", or "e". */
    private void setCommand(String square, String side) {
        _playing = false;
        square = square.trim();
        int c = Board.col(square);
        int r = Board.row(square);
        Piece v = Piece.setValueOf(side);
        _board.set(c, r, v, v.opposite());
    }

    /** Play this game, printing any results. */
    public void play() {
        HashSet<Board> positionsPlayed = new HashSet<Board>();
        _board = new Board();

        while (true) {
            int playerInd = _board.turn().ordinal();
            Move next;
            if (_playing) {
                if (_board.gameOver()) {
                    announceWinner();
                    _playing = false;
                    continue;
                }
                next = _players[playerInd].makeMove();
                assert !_playing || next != null;
            } else {
                getMove();
                next = null;
            }
            if (next != null) {
                assert _board.isLegal(next);
                _board.makeMove(next);
                if (_board.gameOver()) {
                    announceWinner();
                    _playing = false;
                }
            }
        }
    }

    /** Print an announcement of the winner. */
    private void announceWinner() {
        String name = _board.turn().opposite().fullName();
        String capname =
            Character.toUpperCase(name.charAt(0)) + name.substring(1);
        System.out.println(capname + " wins.");
    }

    /** Return an integer r, 0 <= r < N, randomly chosen from a
     *  uniform distribution using the current random source. */
    int randInt(int n) {
        return _randomSource.nextInt(n);
    }

    /** Print a help message. */
    void help() {
        System.out.println("Commands: Commands are whitespace-delimited."
            + " Other trailing text on a line is ignored. Comment lines begin "
            + "with # and are ignored.");
        System.out.println();
        System.out.println("b");
        System.out.println("board     Display the board, showing row and column"
            + "designations.");
        System.out.println("autoprint Toggle mode in which the board is printed"
            + "(as for b) after each AI move.");
        System.out.println("start     Start playing from the current"
            + " position.");
        System.out.println("uv-xy     A move from square uv to square xy. Here"
            + "u and v are column designations (a-h) and v and y are "
            + "row designations (1-8).");
        System.out.println("clear     Stop game and return to initial"
            + " position.");
        System.out.println("seed N    Seed the random number with integer N.");
        System.out.println("auto P    P is white or black; makes P "
            + "into an AI. Stops game.");
        System.out.println("manual P  P is white or black; takes moves"
            + " for P from terminal. Stops game.");
        System.out.println("set cr P  Put P ('w', 'b', or empty)"
            + " into square cr. Stops game.");
        System.out.println("dump      Display the board in standard format.");
        System.out.println("quit      End program.");
        System.out.println("help");
        System.out.println("?         This text.");
    }

    /** The official game board. */
    private Board _board;

    /** The _players of this game. */
    private Player[] _players = new Player[2];

    /** A source of random numbers, primed to deliver the same sequence in
     *  any Game with the same seed value. */
    private Random _randomSource;

    /** Input source. */
    private BufferedReader _input;

    /** True if actually playing (game started and not stopped or finished).
     */
    private boolean _playing;

}
