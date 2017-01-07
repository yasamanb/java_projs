package gitlet;
import java.util.HashMap;
import java.util.ArrayList;
import java.io.Serializable;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.File;

/** Commit object class.
 *  @author Y.B. & C.A.
 */

class Info implements Serializable {
	/*contructor for creating info object*/
	public Info(HashMap<String, String> branchCommit, HashMap<String, String> nameShaStaged, HashMap<String, String> shaLog, ArrayList<String> removed, String header, String branch) {
		_branchCommit = branchCommit;
		_nameShaStaged = nameShaStaged;
		_shaLog = shaLog;
		_removed = removed;
		_header = header;
		_branch = branch;
	}
	
	/** filler*/
	public static void saveInfo(Info myInfo, String directory) {
        if (myInfo == null) {
            return;
        }
        try {
        	File dir = new File (directory);
            File myInfoFile = new File(dir, "myInfo.ser");
            FileOutputStream fileOut = new FileOutputStream(myInfoFile);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(myInfo);
        } catch (IOException e) {
            String msg = "IOException while saving myInfo.";
            System.out.println(msg);
        }
    }
    /** filler*/
	public static Info tryLoadingInfo(String directory) {
        Info myInfo = null;
        File dir = new File (directory);
        File myInfoFile = new File(dir, "myInfo.ser");
        if (myInfoFile.exists()) {
            try {
                FileInputStream fileIn = new FileInputStream(myInfoFile);
                ObjectInputStream objectIn = new ObjectInputStream(fileIn);
                myInfo = (Info) objectIn.readObject();
            } catch (IOException e) {
                String msg = "IOException while loading myInfo.";
                System.out.println(msg);
            } catch (ClassNotFoundException e) {
                String msg = "ClassNotFoundException while loading myInfo.";
                System.out.println(msg);
            }
        }
        return myInfo;
    }
    /** filler*/
	HashMap<String, String> getBranchCommit() {
		return _branchCommit;
	}
	/** filler*/
	HashMap<String, String> getNameShaStaged() {
		return _nameShaStaged;
	}
	/** filler*/
	HashMap<String, String> getShaLog() {
		return _shaLog;
	}
	/** filler*/
	ArrayList<String> getRemoved() {
		return _removed;
	}
	/** filler*/
	String getBranch() {
		return _branch;
	}
	String getHeader() {
		return _header;
	}

	/** filler*/
	void setBranchCommit(HashMap<String, String> newMap) {
		_branchCommit = newMap;
	}
	/** filler*/
	void setNameShaStaged(HashMap<String, String> newMap) {
		_nameShaStaged = newMap;
	}
	/** filler*/
	void setShaLog(HashMap<String, String> newMap) {
		_shaLog = newMap;
	}
	/** filler*/
	void setRemoved(ArrayList<String> newArray) {
		_removed = newArray;
	}
	/** filler*/
	void setBranch(String newBranch) {
		_branch = newBranch;
	}
	/** filler*/
	void setHeader(String newHeader) {
		_header = newHeader;
	}


	/** Map from branch names to sha-1 string id associated with Commit. */
	private HashMap<String, String> _branchCommit;
	/** Map from file names to sha-1 string id associated with File in Staged. */
	private HashMap<String, String> _nameShaStaged;
	/** Map from file names to sha-1 string id associated with File in File folder. */
	private HashMap<String, String> _shaLog;
	/** List of removed items*/
	private ArrayList<String> _removed;
	/** Current branch*/
	private String _branch;
	/** Current commit */
	private String _header;


}
