package db61b;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import java.util.ArrayList;

import static db61b.Utils.*;

/** A single table in a database.
 *  @author Yasaman Bahri
 */
class Table implements Iterable<Row> {
    /** A new Table named NAME whose columns are give by COLUMNTITLES,
     *  which must be distinct (else exception thrown). */
    Table(String name, String[] columnTitles) {
        _name = name;
        List<String> mylist = Arrays.asList(columnTitles);
        HashSet<String> uniqueColumns = new HashSet<String>();
        uniqueColumns.addAll(mylist);
        if (uniqueColumns.size() != columnTitles.length) {
            throw new DBException("Columns in table not unique.");
        } else {
            _titles = columnTitles;
        }
    }

    /** A new Table named NAME whose column names are give by COLUMNTITLES. */
    Table(String name, List<String> columnTitles) {
        this(name, columnTitles.toArray(new String[columnTitles.size()]));
    }

    /** Return the number of columns in this table. */
    int numColumns() {
        return _titles.length;
    }

    /** Returns my name. */
    String name() {
        return _name;
    }

    /** Returns a TableIterator over my rows in an unspecified order. */
    TableIterator tableIterator() {
        return new TableIterator(this);
    }

    /** Returns an iterator that returns my rows in an unspecfied order. */
    @Override
    public Iterator<Row> iterator() {
        return _rowsOf.listIterator();
    }

    /** Return the title of the Kth column.  Requires 0 <= K < columns(). */
    String title(int k) {
        return _titles[k];
    }


    /** Return the number of the column whose title is TITLE, or -1 if
     *  there isn't one. */
    int columnIndex(String title) {
        int i = 0;
        while (i < _titles.length) {
            if (title.equals(_titles[i])) {
                return i;
            }
            i = i + 1;
        }
        return -1;
    }

    /** Return the number of Rows in this table. */
    int size() {
        return _rowsOf.size();
    }

    /** Add ROW to THIS if no equal row already exists.  Return true if anything
     *  was added, false otherwise. */
    boolean add(Row row) {
        if (_titles.length != row.size()) {
            throw new DBException("Mismatching column sizes.");
        }
        boolean isRepeat = false;
        if (_rowsOf != null) {
            for (int i = 0; i < _rowsOf.size(); i++) {
                if (row.equals(_rowsOf.get(i))) {
                    isRepeat = true;
                }
            }
        }
        if (!isRepeat) {
            _rowsOf.add(row);
            return true;
        } else {
            return false;
        }
    }

    /** Read the contents of the file NAME.db, and return as a Table.
     *  Format errors in the .db file cause a DBException. */
    static Table readTable(String name) {
        BufferedReader input;
        Table table;
        input = null;
        table = null;
        try {
            input = new BufferedReader(new FileReader(name + ".db"));
            String header = input.readLine();
            if (header == null) {
                throw error("missing header in DB file");
            }
            String[] columnNames = header.split(",");
            table = new Table(name, columnNames);
            String s = input.readLine();
            while (s != null) {
                String[] rowstring = s.split(",");
                Row dbRow = new Row(rowstring);
                table.add(dbRow);
                s = input.readLine();
            }
        } catch (FileNotFoundException e) {
            throw error("could not find %s.db", name);
        } catch (IOException e) {
            throw error("problem reading from %s.db", name);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    /* Ignore IOException */
                }
            }
        }
        return table;
    }

    /** Write the contents of TABLE into the file NAME.db. Any I/O errors
     *  cause a DBException. */
    void writeTable(String name) {
        PrintStream output;
        output = null;
        try {
            String sep;
            sep = "";
            output = new PrintStream(name + ".db");
            for (int i = 0; i < _titles.length; i++) {
                output.print(_titles[i]);
                if (i != (_titles.length - 1)) {
                    output.print(",");
                } else {
                    output.println();
                }
            }
            for (int j = 0; j < _rowsOf.size(); j++) {
                Row currRow = _rowsOf.get(j);
                for (int k = 0; k < currRow.size(); k++) {
                    output.print(currRow.get(k));
                    if (k != (currRow.size() - 1)) {
                        output.print(",");
                    }
                }
                output.println();
            }
        } catch (IOException e) {
            throw error("trouble writing to %s.db", name);
        } finally {
            if (output != null) {
                output.close();
            }
        }
    }

    /** Print my contents on the standard output, separated by spaces
     *  and indented by two spaces. */
    void print() {
        for (int j = 0; j < _rowsOf.size(); j++) {
            System.out.print("  ");
            Row currRow = _rowsOf.get(j);
            for (int k = 0; k < currRow.size(); k++) {
                System.out.print(currRow.get(k));
                if (k != (currRow.size() - 1)) {
                    System.out.print(" ");
                }
            }
            System.out.println();
        }
    }

    /** Return row K of the table. Used by TableIterator. */
    Row getRow(int k) {
        if ((_rowsOf != null) & (k < _rowsOf.size())) {
            return _rowsOf.get(k);
        } else {
            return null;
        }
    }

    /** My name. */
    private final String _name;
    /** My column titles. */
    private String[] _titles;
    /** List of pointers to rows. */
    private List<Row> _rowsOf = new ArrayList<Row>();
}

