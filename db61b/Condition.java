package db61b;

import java.util.List;
import java.util.Arrays;

/** Represents a single 'where' condition in a 'select' command.
 *  @author Yasaman Bahri  */
class Condition {

    /** Internally, we represent our relation as a 3-bit value whose
     *  bits denote whether the relation allows the left value to be
     *  greater than the right (GT), equal to it (EQ),
     *  or less than it (LT). */
    private static final int GT = 1, EQ = 2, LT = 4;

    /** A Condition representing COL1 RELATION COL2, where COL1 and COL2
     *  are column designators. and RELATION is one of the
     *  strings "<", ">", "<=", ">=", "=", or "!=". */
    Condition(Column col1, String relation, Column col2) {
        _left = col1;
        _right = col2;
        _relation = relation;
        switch (_relation) {
        case ">":
            code = GT;
            break;
        case "=":
            code = EQ;
            break;
        case ">=":
            code = GT | EQ;
            break;
        case "<":
            code = LT;
            break;
        case "!=":
            code = (~EQ) + 8;
            break;
        case "<=":
            code = LT | EQ;
            break;
        default:
            throw new DBException("Illegal relation.");
        }
    }

    /** A Condition representing COL1 RELATION 'VAL2', where COL1 is
     *  a column designator, VAL2 is a literal value (without the
     *  quotes), and RELATION is one of the strings "<", ">", "<=",
     *  ">=", "=", or "!=".
     */
    Condition(Column col1, String relation, String val2) {
        this(col1, relation, new Literal(val2));
    }

    /** Assuming that ROWS are rows from the respective tables from which
     *  my columns are selected, returns the result of performing the test I
     *  denote. */
    boolean test() {
        int tVal = _left.value().compareTo(_right.value());
        Boolean[] truths = new Boolean[6];
        Arrays.fill(truths, Boolean.FALSE);
        if (tVal > 0) {
            truths[GT - 1] = true;
            truths[(GT | EQ) - 1] = true;
            truths[(~EQ) + 8 - 1] = true;
        } else if (tVal < 0) {
            truths[LT - 1] = true;
            truths[(LT | EQ) - 1] = true;
            truths[(~EQ) + 8 - 1] = true;
        } else {
            truths[EQ - 1] = true;
            truths[(GT | EQ) - 1] = true;
            truths[(LT | EQ) - 1] = true;
        }
        return truths[code - 1];
    }

    /** Return true iff all CONDITIONS are satified. */
    static boolean test(List<Condition> conditions) {
        for (int i = 0; i < conditions.size(); i++) {
            if (!conditions.get(i).test()) {
                return false;
            }
        }
        return true;
    }

    /** Column on left of relation. */
    protected Column _left;
    /** Column on right of relation. */
    protected Column _right;
    /** Relation operator. */
    protected String _relation;
    /** _relation translated to an integer from 1-6. */
    protected int code;
}
