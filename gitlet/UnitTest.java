package gitlet;

import ucb.junit.textui;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.HashMap;
import java.util.ArrayList;
import java.io.File;

/** The suite of all JUnit tests for the gitlet package.
 *  @author
 */
public class UnitTest {

    /** Run the JUnit tests in the loa package. Add xxxTest.class entries to
     *  the arguments of runClasses to run other JUnit tests. */
    public static void main(String[] ignored) {
        textui.runClasses(UnitTest.class);
    }
    @Test
    public void dummyInfo() {
    	
    }
    /** A dummy test to avoid complaint. 
    @Test
    public void testInfo() {

			String directory = System.getProperty("user.dir") + "/.gitlet";

    		/* create an Info object 
    	 	HashMap<String, String> test = new HashMap<String, String>();
            test.put("A", "a");
            HashMap<String, String> test0 = new HashMap<String, String>();
            test0.put("C", "c");
            HashMap<String, String> test1 = new HashMap<String, String>();
            test1.put("B", "b");
            HashMap<String, String> test2 = new HashMap<String, String>();
            test2.put("C", "c");
            HashMap<String, String> test3 = new HashMap<String, String>();
            test3.put("D", "d");
            ArrayList<String> test4 = new ArrayList<String>();
            test4.add("hi");
            String test5 = "bye";

            Info testInfo = new Info(test, test1, test3, test4, test5, "master");
            Info.saveInfo(testInfo, directory);
            Info readInfo = Info.tryLoadingInfo(directory);

            assertEquals("a", readInfo.getBranchCommit().get("A"));
            
            Info newInfo = new Info(test0, test1, test3, test4, test5, "master");
            Info.saveInfo(newInfo, directory);
            Info readInfo2 = Info.tryLoadingInfo(directory);

            assertEquals("c", newInfo.getBranchCommit().get("C"));
            
    }

    @Test
    public void testCommit() {

    		String directory = System.getProperty("user.dir") + "/.gitlet/object/commits";
    		
    		/* create a Commit object 
    	 	HashMap<String, String> test = new HashMap<String, String>();
            test.put("A", "a");
            String parent = "parent";
            String msg = "test commit";
            String time = "10";
            
            Commit testCommit = new Commit(test, parent, msg, time);
            Commit.save(testCommit, directory);

            Commit readCommit = Commit.load(testCommit.getSHA() + ".ser", directory);

            assertEquals("a", readCommit.getNames().get("A"));
    }

   */


}


