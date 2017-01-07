package db61b;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.List;

/** Testing the Row Class
 *  @author Yasaman Bahri
 */

public class UnitTests {

    /** Tests the Row class. */
    @Test
    public void testRow() {
        List<Column> listLit = new ArrayList<Column>();
        listLit.add((Column) new Literal("Apple"));
        listLit.add((Column) new Literal("Carrot"));
        listLit.add((Column) new Literal("Milk"));
        Row row1 = Row.make(listLit);
        assertEquals(row1.get(2), "Milk");
        assertEquals(row1.size(), 3);
    }

    /** Tests the authored functions in Table.java and TableIterator.java. */
    @Test
    public void testTable() {
        List<Column> listLit = new ArrayList<Column>();
        listLit.add((Column) new Literal("Apple"));
        listLit.add((Column) new Literal("Carrot"));
        listLit.add((Column) new Literal("Milk"));
        Row row1 = Row.make(listLit);
        String[] columnTitles = {"Fruit", "Vegetable", "Dairy"};
        Table groups = new Table("FoodGroups", columnTitles);
        groups.add(row1);
        Row row2 = new Row(new String[] {"Berry", "Spinach", "Cheese"});
        assertEquals(groups.add(row2), true);
        assertEquals(groups.add(row2), false);
        Row row3 = new Row(new String[] {"Orange", "Cabbage", "Butter"});
        groups.add(row3);
        assertEquals(groups.numColumns(), 3);
        assertEquals(groups.columnIndex("Dairy"), 2);
        assertEquals(groups.columnIndex("Sweets"), -1);
        TableIterator groupIter = groups.tableIterator();
        assertEquals(groupIter.hasRow(), true);
        assertEquals(groupIter.next(), row1);
        assertEquals(groupIter.next(), row2);
        assertEquals(groupIter.next(), row3);
        assertEquals(groupIter.hasRow(), false);
        groupIter.reset();
        assertEquals(groupIter.next(), row1);
    }
    public static void main(String[] args) {
        System.exit(ucb.junit.textui.runClasses(UnitTests.class));
    }
}
