package db61b;

import java.io.PrintStream;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import static db61b.Utils.*;
import static db61b.Tokenizer.*;

/** An object that reads and interprets a sequence of commands from an
 *  input source.
 *  @author Yasaman Bahri */
class CommandInterpreter {

    /* STRATEGY.
     *
     *   This interpreter parses commands using a technique called
     * "recursive descent." The idea is simple: we convert the BNF grammar,
     * as given in the specification document, into a program.
     *
     * First, we break up the input into "tokens": strings that correspond
     * to the "base case" symbols used in the BNF grammar.  These are
     * keywords, such as "select" or "create"; punctuation and relation
     * symbols such as ";", ",", ">="; and other names (of columns or tables).
     * All whitespace and comments get discarded in this process, so that the
     * rest of the program can deal just with things mentioned in the BNF.
     * The class Tokenizer performs this breaking-up task, known as
     * "tokenizing" or "lexical analysis."
     *
     * The rest of the parser consists of a set of functions that call each
     * other (possibly recursively, although that isn't needed for this
     * particular grammar) to operate on the sequence of tokens, one function
     * for each BNF rule. Consider a rule such as
     *
     *    <create statement> ::= create table <table name> <table definition> ;
     *
     * We can treat this as a definition for a function named (say)
     * createStatement.  The purpose of this function is to consume the
     * tokens for one create statement from the remaining token sequence,
     * to perform the required actions, and to return the resulting value,
     * if any (a create statement has no value, just side-effects, but a
     * select clause is supposed to produce a table, according to the spec.)
     *
     * The body of createStatement is dictated by the right-hand side of the
     * rule.  For each token (like create), we check that the next item in
     * the token stream is "create" (and report an error otherwise), and then
     * advance to the next token.  For a metavariable, like <table definition>,
     * we consume the tokens for <table definition>, and do whatever is
     * appropriate with the resulting value.  We do so by calling the
     * tableDefinition function, which is constructed (as is createStatement)
     * to do exactly this.
     *
     * Thus, the body of createStatement would look like this (_input is
     * the sequence of tokens):
     *
     *    _input.next("create");
     *    _input.next("table");
     *    String name = name();
     *    Table table = tableDefinition();
     *    _input.next(";");
     *
     * plus other code that operates on name and table to perform the function
     * of the create statement.  The .next method of Tokenizer is set up to
     * throw an exception (DBException) if the next token does not match its
     * argument.  Thus, any syntax error will cause an exception, which your
     * program can catch to do error reporting.
     *
     * This leaves the issue of what to do with rules that have alternatives
     * (the "|" symbol in the BNF grammar).  Fortunately, our grammar has
     * been written with this problem in mind.  When there are multiple
     * alternatives, you can always tell which to pick based on the next
     * unconsumed token.  For example, <table definition> has two alternative
     * right-hand sides, one of which starts with "(", and one with "as".
     * So all you have to do is test:
     *
     *     if (_input.nextIs("(")) {
     *          _input.next();
     *                                   +
     *         // code to process "<name>,  )"
     *     } else {
     *         // code to process "as <select clause>"
     *     }
     *
     * or for convenience,
     *
     *     if (_input.nextIf("(")) {
     *                                   +
     *         // code to process "<name>,  )"
     *     } else {
     *     ...
     *
     * combining the calls to .nextIs and .next.
     *
     * You can handle the list of <name>s in the preceding in a number
     * of ways, but personally, I suggest a simple loop:
     *
     *     call name() and do something with it;
     *     while (_input.nextIs(",")) {
     *         _input.next(",");
     *         call name() and do something with it;
     *     }
     *
     * or if you prefer even greater concision:
     *
     *     call name() and do something with it;
     *     while (_input.nextIf(",")) {
     *         call name() and do something with it;
     *     }
     *
     * (You'll have to figure out what do with the names you accumulate, of
     * course).
     *
     */

    /** A new CommandParser executing commands read from INP, writing
     *  prompts on PROMPTER, if it is non-null, and using DATABASE
     *  to map names of tables to corresponding Tables. */
    CommandInterpreter(Map<String, Table> database,
                       Scanner inp, PrintStream prompter) {
        _input = new Tokenizer(inp, prompter);
        _database = database;
    }

    /** Parse and execute one statement from the token stream.  Return true
     *  iff the command is something other than quit or exit. */
    boolean statement() {
        switch (_input.peek()) {
        case "create":
            createStatement();
            break;
        case "load":
            loadStatement();
            break;
        case "exit": case "quit":
            exitStatement();
            return false;
        case "*EOF*":
            return false;
        case "insert":
            insertStatement();
            break;
        case "print":
            printStatement();
            break;
        case "select":
            selectStatement();
            break;
        case "store":
            storeStatement();
            break;
        default:
            throw error("unrecognizable command");
        }
        return true;
    }

    /** Parse and execute a create statement from the token stream. */
    private void createStatement() {
        _input.next("create");
        _input.next("table");
        String name = name();
        Table table = tableDefinition(name);
        if (_database.containsKey(name)) {
            _database.remove(name);
        }
        _database.put(name, table);
    }

    /** Parse and execute an exit or quit statement. Actually does nothing
     *  except check syntax, since statement() handles the actual exiting. */
    private void exitStatement() {
        if (!_input.nextIf("quit")) {
            _input.next("exit");
        }
        _input.next(";");
        System.out.println();
    }

    /** Parse and execute an insert statement from the token stream. */
    private void insertStatement() {
        _input.next("insert");
        _input.next("into");
        Table table = tableName();
        _input.next("values");
        ArrayList<String> values = new ArrayList<>();
        values.add(literal());
        while (_input.nextIf(",")) {
            values.add(literal());
        }
        table.add(new Row(values.toArray(new String[values.size()])));
        _input.next(";");
    }

    /** Parse and execute a load statement from the token stream. */
    private void loadStatement() {
        _input.next("load");
        String tabName = _input.next();
        _database.put(tabName, Table.readTable(tabName));
        _input.next(";");
        System.out.println("Loaded " + tabName + ".db");

    }

    /** Parse and execute a store statement from the token stream. */
    private void storeStatement() {
        _input.next("store");
        String name = _input.peek();
        Table table = tableName();
        table.writeTable(name);
        _input.next(";");
        System.out.println("Stored " + table.name() + ".db");
    }

    /** Parse and execute a print statement from the token stream. */
    private void printStatement() {
        _input.next("print");
        Table table = tableName();
        _input.next(";");
        System.out.println("Contents of " + table.name() + ":");
        table.print();
    }

    /** Parse and execute a select statement from the token stream. */
    private void selectStatement() {
        _input.next("select");
        String tableName;
        tableName = "dummy";
        Table result = selectClause(tableName);
        System.out.println("Search results:");
        result.print();
    }

    /** Parse and execute a table definition for a Table named NAME,
     *  returning the specified table. */
    Table tableDefinition(String name) {
        if (_input.nextIs("(")) {
            _input.next();
            List<String> newColNames = new ArrayList<String>();
            newColNames.add(name());
            while (_input.nextIs(",")) {
                _input.next();
                newColNames.add(name());
            }
            _input.next(")");
            _input.next(";");
            return new Table(name, newColNames);
        } else if (_input.nextIs("as")) {
            _input.next();
            _input.next("select");
            return selectClause(name);
        } else {
            throw new DBException();
        }
    }

    /** Parse and execute a select clause from the token stream, returning the
     *  resulting table, with name TABLENAME. */
    Table selectClause(String tableName) {
        ArrayList<Column> cols = new ArrayList<Column>();
        boolean loop = true;
        List<String> replaceNames = new ArrayList<String>();
        List<Integer> locations = new ArrayList<Integer>();
        int counter = 0;
        while (loop) {
            loop = false;
            Column currCol = columnSelector();
            if (_input.nextIs("as")) {
                _input.next();
                replaceNames.add(name());
                locations.add(counter);
            }
            cols.add(currCol);
            if (_input.nextIf(",")) {
                loop = true;
                counter = counter + 1;
            }
        }
        _input.next();
        List<Table> tableList = new ArrayList<Table>();
        tableList.add(tableName());
        while (_input.nextIs(",")) {
            _input.next();
            tableList.add(tableName());
        }
        List<TableIterator> iterList = new ArrayList<TableIterator>();
        for (int i = 0; i < tableList.size(); i++) {
            iterList.add(tableList.get(i).tableIterator());
        }
        for (int j = 0; j < cols.size(); j++) {
            cols.get(j).resolve(iterList);
        }
        List<String> columnTitles = new ArrayList<String>();
        for (int i = 0; i < cols.size(); i++) {
            columnTitles.add(cols.get(i).name());
        }
        if (!replaceNames.isEmpty()) {
            for (int i = 0; i < locations.size(); i++) {
                columnTitles.set(locations.get(i), replaceNames.get(i));
            }
        }
        Table resultTab = new Table(tableName, columnTitles);
        List<Condition> condList = new ArrayList<Condition>();
        if (_input.nextIs("where")) {
            condList = conditionClause(iterList);
        }
        _input.next(";");
        select(resultTab, cols, iterList, condList);
        return resultTab;
    }

    /** Parse and return a valid name (identifier) from the token stream.
     *  The identifier need not have a meaning. */
    String name() {
        return _input.next(Tokenizer.IDENTIFIER);
    }

    /** Parse valid column designation (name or table.name), and
     *  return as an unresolved Column. */
    Column columnSelector() {
        String holder = name();
        Table thisTab = null;
        boolean unqualified = false;
        try {
            thisTab = tableName(holder);
        } catch (DBException e) {
            unqualified = true;
        }
        Column result;
        if (unqualified) {
            result = new Column(thisTab, holder);
        } else {
            _input.next();
            result = new Column(thisTab, name());
        }
        return result;
    }

    /** Parse and return a column designator, after resolving against
     *  ITERATORS. */
    Column columnSelector(List<TableIterator> iterators) {
        Column col = columnSelector();
        col.resolve(iterators);
        return col;
    }

    /** Parse a valid table name from the token stream, and return the Table
     *  that it designates, which must be loaded. */
    Table tableName() {
        String name = name();
        Table table = _database.get(name);
        if (table == null) {
            throw error("unknown table: %s", name);
        }
        return table;
    }

    /** Parse a valid table name from the token stream, and return the Table
     *  that it designates, which must be loaded. Use string NAME for name. */
    Table tableName(String name) {
        Table table = _database.get(name);
        if (table == null) {
            throw error("unknown table: %s", name);
        }
        return table;
    }


    /** Parse a literal and return the string it represents (i.e., without
     *  single quotes). */
    String literal() {
        String lit = _input.next(Tokenizer.LITERAL);
        return lit.substring(1, lit.length() - 1).trim();
    }


    /** Parse and return a list of Conditions that apply to TABLES from the
     *  token stream.  This denotes the conjunction (`and') of zero
     *  or more Conditions.  Resolves all Columns within the clause
     *  against ITERATORS. */
    List<Condition> conditionClause(List<TableIterator> iterators) {
        _input.next();
        List<Condition> condList = new ArrayList<Condition>();
        condList.add(condition(iterators));
        while (_input.nextIs("and")) {
            _input.next();
            condList.add(condition(iterators));
        }
        return condList;
    }

    /** Parse and return a Condition that applies to ITERATORS from the
     *  token stream. */
    Condition condition(List<TableIterator> iterators) {
        int loop = 0;
        Column[] colArray = new Column[2];
        Column temp;
        String relOp = "null";
        while (loop < 2) {
            if (_input.nextIs(Tokenizer.LITERAL)) {
                colArray[loop] = new Literal(literal());
            } else {
                colArray[loop] = columnSelector(iterators);
            }
            if (_input.nextIs(Tokenizer.RELATION)) {
                relOp = _input.next();
            }
            loop = loop + 1;
        }
        Condition returnCond =
            new Condition(colArray[0], relOp, colArray[1]);
        return returnCond;
    }

    /** Fill TABLE with the result of selecting COLUMNS from the rows returned
     *  by ITERATORS that satisfy CONDITIONS.  ITERATORS must have size 1 or 2.
     *  All selected Columns and all Columns mentioned in CONDITIONS must be
     *  resolved to iterators listed among ITERATORS.  The number of
     *  COLUMNS must equal TABLE.columns(). */
    private void select(Table table, ArrayList<Column> columns,
                        List<TableIterator> iterators,
                        List<Condition> conditions) {
        TableIterator iter1 = iterators.get(0);
        while (iter1.hasRow()) {
            if (iterators.size() > 1) {
                TableIterator iter2 = iterators.get(1);
                while (iter2.hasRow()) {
                    if (Condition.test(conditions)) {
                        table.add(Row.make(columns));
                    }
                    iter2.next();
                }
                iter2.reset();
            } else {
                if (Condition.test(conditions)) {
                    table.add(Row.make(columns));
                }
            }
            iter1.next();
        }
    }

    /** Advance the input past the next semicolon. */
    void skipCommand() {
        while (true) {
            try {
                while (!_input.nextIf(";") && !_input.nextIf("*EOF*")) {
                    _input.next();
                }
                return;
            } catch (DBException excp) {
                /* No action */
            }
        }
    }

    /** The command input source. */
    private Tokenizer _input;
    /** Database containing all tables. */
    private Map<String, Table> _database;
}
