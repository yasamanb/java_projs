package gitlet;

import java.util.HashMap;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/** Commit object class.
 *  @author Y.B. & C.A.
 */
class Commit implements Serializable {


	Commit(HashMap<String, String> names, String parent, String msg, String timestamp) {
		_names = names;
		_parent = parent;
		_msg = msg;
		_timestamp = timestamp; 
		try {
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
			ObjectOutputStream objectOut = new ObjectOutputStream(byteStream);
			objectOut.writeObject(names);
			byte[] mapBytes = byteStream.toByteArray();
			_shaID = Utils.sha1(mapBytes, parent, msg, timestamp);
		} catch (IOException e) {
			System.out.println("IOException while converting to bytes");
		}
	}


	/** Loads a serialized commit from file with name string FILENAME. Static method. */
	static Commit load(String filename, String directory) {
		Commit commitObj = null;
		File dir = new File (directory);
		File file = new File(dir, filename);
		if (file.exists()) {
			try {
                FileInputStream fileIn = new FileInputStream(file);
                ObjectInputStream objectIn = new ObjectInputStream(fileIn);
                commitObj = (Commit) objectIn.readObject();
        	} catch (IOException e) {
                String msg = "IOException while loading.";
                System.out.println(msg);
        	} catch (ClassNotFoundException e) {
                String msg = "ClassNotFoundException while loading.";
                System.out.println(msg);
        	}
		} else {
			System.out.println("No commit with that id exists.");
		}
        return commitObj;
	}

	/** Saves a commit into serialized form. Should be static method. */
	static void save(Commit commitObj, String directory){
		try {
			File dir = new File (directory);
            File commitFile = new File(dir, commitObj.getSHA() + ".ser");  
            FileOutputStream fileOut = new FileOutputStream(commitFile);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(commitObj);
            objectOut.close();
        } catch (IOException e) {
        	System.out.println("IOException while saving.");
        }
	}

	/**Getter method for _shaID. */
	String getSHA() {
		return _shaID;
	}
	/** filler*/
	HashMap<String, String> getNames() {
		return _names;
	}
	/** filler*/
	String getParent() {
		return _parent;
	}
	/** filler*/
	String getMsg() {
		return _msg;
	}
	/** filler*/
	String getTime() {
		return _timestamp;
	}


	/** Map from file names to sha-1 string id associated with Commit. */
	private HashMap<String, String> _names;
	/** Sha-1 string id of parent commit. Possibly null if very first commit. */
	private String _parent;
	/** Message associated with commit, possibly null. */
	private String _msg;
	/** Time stamp associated with commit. */
	private String _timestamp;
	/**Sha-1 ID for this commit. */
	private String _shaID;

}