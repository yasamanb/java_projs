package gitlet;
import java.util.Scanner;
import java.io.File;
import java.util.HashMap;
import java.util.ArrayList;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.io.IOException;
import java.util.Iterator;
import java.util.Arrays;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.AbstractSet;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.io.ByteArrayOutputStream;


/** Driver class for Gitlet, the tiny version-control system.
 *  @author C.A. & Y.B.
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) {
        try {
        	String input = args[0];
        	
            if (!loadInfo() && !args[0].equals("init")){
                System.out.println("Not in an initialized gitlet directory.");
            }
            else {
                if(!processCommand(input, args)) {
                   System.out.println("No command with that name exists.");
                }
            }	    
        }
        catch (Exception e){
        	System.out.println("Please enter a command.");
        }
    }
    /** If LINE is a recognized command, process it
     *  and return true.  Otherwise, return false. */
    private static boolean processCommand(String line, String[] args) {
        
        switch (line) {
        	case "init":
        		init(args);
        		return true;
        	case "add":
                add(args);
        		return true;
        	case "commit":
                commit(args);
        		return true;
        	case "rm":
                remove(args);
        		return true;
        	case "log":
                log(args);
        		return true;
        	case "global-log":
                globalLog(args);
        		return true;
        	case "find":
                find(args);
        		return true;
        	case "status":
                status(args);
        		return true;
            case "reset":
                reset(args);
                return true;
        	case "checkout":
                checkout(args);
        		return true;
        	case "branch":
                branch(args);
        		return true;
        	case "rm-branch":
                rmbranch(args);
        		return true;
        	case "merge":
                merge(args);
        		return true;
            case "stat":
                stat();
                return true;
        }
        return false;
    }
    /*load any information and return true if loaded. Return false if there is no 
    information to be loaded.*/
    private static boolean loadInfo() {

        String directory = System.getProperty("user.dir") + "/.gitlet";
        _currentInfo = Info.tryLoadingInfo(directory);

        if (_currentInfo == null) {
            return false;
        }

        return true;
    }
    
    /* make a file at given directory with given name*/
    private static void makeFile(String directory, String name) {
        String fileName = name;
        File dir = new File (directory);
        File real = new File (dir, fileName);
        real.mkdir();
    }
    /** Takes in args.  Creates .gitlet folder with desired contents, creaes new 
    commit and info object.*/
    private static void init(String[] args) {
    	if (args.length != 1) {
    		System.out.println("Incorrect operands.");
    	}
    	else {
            String directory = System.getProperty("user.dir") + "/.gitlet";
    		if (new File(directory).exists()) {
                System.out.println("A gitlet version-control system already exists in the current directory.");
            }
            else {

                File gitletFolder = new File(".gitlet");
                gitletFolder.mkdir();

                makeFile(directory, "staged");
                makeFile(directory, "object");

                String directoryObject = directory + "/object";
                String directoryStaged = directory + "/staged";

                makeFile(directoryObject, "files");
                makeFile(directoryObject, "commits");

                HashMap<String, String> empty = new HashMap<String, String>();

                Date date = new Date();
                SimpleDateFormat form = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                String dateToStr = form.format(date);
                Commit initialCommit = new Commit(empty, "", "initial commit", dateToStr);
                Commit.save(initialCommit, directoryObject + "/commits");

                HashMap<String, String> branchToCommit = new HashMap<String, String>();
                branchToCommit.put("master", initialCommit.getSHA());
                HashMap<String, String> shaToLog = new HashMap<String, String>();
                shaToLog.put(initialCommit.getSHA(), "initial commit");
                String header = initialCommit.getSHA();

                Info initialInfo = new Info(branchToCommit, empty, shaToLog, new ArrayList<String>(), header, "master");
                Info.saveInfo(initialInfo, directory);
            }
    	}
    }

    private static String fullSha(String givenSha) {
        String fullSha = "";
        ArrayList<String> allFiles = new ArrayList<String>();
        allFiles.addAll(Utils.plainFilenamesIn(System.getProperty("user.dir") + "/.gitlet/object/commits"));

        String currentSHA = "";
        int matchCount = 0;

        for (int x = 0; x < allFiles.size(); x++) {
            currentSHA = allFiles.get(x).substring(0, givenSha.length());
            if (currentSHA.equals(givenSha)) {
                matchCount = matchCount + 1;
                fullSha = currentSHA;
            }
        }

        if (matchCount > 1) {
            fullSha = "a";
        }
        return fullSha;
    }

    /* Add file to staging area, parsing ARGS as appropriate. */
    private static void add(String[] args) {
        /* Obtain the removed list. */
        ArrayList<String> removed = _currentInfo.getRemoved();
        String filename = args[1];
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
        } else if (removed.contains(filename)) {
            /* file is on removed list -- don't stage it, simply remove it from REMOVED. resave INFO. */
            removed.remove(filename);
            _currentInfo.setRemoved(removed);
            /* Save info again. */
            String directory = System.getProperty("user.dir") + "/.gitlet";
            Info.saveInfo(_currentInfo, directory);
        } else {
            File file = new File(filename);
            if (!file.exists()) {
                System.out.println("File does not exist.");
                return;
            }
            /** Obtain sha-1 id of file. */
            byte[] fileBytes = Utils.readContents(file);
            String fileID = Utils.sha1(fileBytes);
            /** Obtain current commit map and compare. */
            String header = _currentInfo.getHeader();
            String directory = System.getProperty("user.dir") + "/.gitlet/object/commits";
            Commit currCommit = Commit.load(header + ".ser", directory);
            HashMap<String, String> currMap = currCommit.getNames();
            if (currMap.containsKey(filename) && currMap.get(filename).equals(fileID)) {
                return;
            }
            directory = System.getProperty("user.dir") + "/.gitlet/staged/";
            Path stagedPath = Paths.get(directory + fileID);
            /** Check against current status of the staging area. */
            HashMap<String, String> stagedMap = _currentInfo.getNameShaStaged();
            if (stagedMap.containsKey(filename)) {
                if (stagedMap.get(filename).equals(fileID)) {
                    return;
                } else {
                    /** Remove the existing file. */
                    Path stagedOldPath = Paths.get(directory + stagedMap.get(filename));
                    try {
                        Files.delete(stagedOldPath);
                    } catch (IOException e) {
                        /** Currently does nothing. */
                    }
                }
            }
            /** Add to staged map. */
            stagedMap.put(filename, fileID);
            _currentInfo.setNameShaStaged(stagedMap);
            directory = System.getProperty("user.dir") + "/.gitlet";
            Info.saveInfo(_currentInfo, directory);
            /** Copy file to staging area. */
            try {
                Files.copy(file.toPath(), stagedPath); 
            } catch (IOException e) {
                /** Currently does nothing. */
            }
        }
    }

    /*create a new commit with the log message from args*/
    private static void commit(String[] args) {

        if (args.length != 2 || args[1].equals("")) {
            System.out.println("Please enter a commit message.");
        }
        else {
            String logMessage = args[1];
        
            String commitDirectory = System.getProperty("user.dir") + "/.gitlet/object/commits";
            String gitletDirectory = System.getProperty("user.dir") + "/.gitlet";
            String fileDirectory = System.getProperty("user.dir") + "/.gitlet/object/files";
            String stagedDirectory = System.getProperty("user.dir") + "/.gitlet/staged";

            String currCommitFileName = _currentInfo.getHeader() + ".ser";
            Commit currCommit = Commit.load(currCommitFileName, commitDirectory);
            String parent = currCommit.getSHA();

            HashMap<String, String> nameShaCommit = currCommit.getNames();
            HashMap<String, String> nameShaStaged = _currentInfo.getNameShaStaged();
            HashMap<String, String> names = nameShaCommit;
            Iterator keys = nameShaStaged.keySet().iterator();
            ArrayList<String> removed = _currentInfo.getRemoved();
            
            if (keys.hasNext() == false && removed.size() == 0) {
                System.out.println("No changes added to the commit.");
            }
            else
            {
                if (removed.size() > 0) {
                    for (int x = 0; x < removed.size(); x++) {
                        names.remove(removed.get(x));  
                    }
                }
                
                while(keys.hasNext()) {
                    String key = (String) keys.next();
                    String fileName = nameShaStaged.get(key);
                    Path filePath = Paths.get(fileDirectory + "/" + fileName);
                    Path stagedPath = Paths.get(stagedDirectory + "/" + fileName);

                    try {
                        /* Added REPLACE_EXISTING here. */
                        //MY MODIFICATIONS
                        Files.copy(stagedPath, filePath, StandardCopyOption.REPLACE_EXISTING);
                    }
                    catch (Exception e) {
                        /*System.out.println("failed to move file"); */
                    }
                    if (!removed.contains(key)) {
                        names.put(key, nameShaStaged.get(key));
                    }
                }


                /* Deleting files in staged. */
                Iterator keys2 = nameShaStaged.keySet().iterator();
                while (keys2.hasNext()) {
                    String filename = (String) keys2.next();
                    String fileID = nameShaStaged.get(filename);
                    File file = new File(stagedDirectory, fileID);
                    if (file.exists()) {
                        try {
                            file.delete(); 
                        } catch (Exception e) {
                            /* No message here. */
                        }
                    }
                }
               
                
                Date date = new Date();
                SimpleDateFormat form = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                String dateToStr = form.format(date);

                Commit newCommit = new Commit(names, parent, logMessage, dateToStr);
                Commit.save(newCommit, commitDirectory);

                HashMap<String, String> shaLog = _currentInfo.getShaLog();
                shaLog.put(newCommit.getSHA(), logMessage);

                HashMap<String, String>  branchCommit = _currentInfo.getBranchCommit();
                branchCommit.put(_currentInfo.getBranch(), newCommit.getSHA());
                removed.clear();

                _currentInfo.setHeader(newCommit.getSHA());
                _currentInfo.setNameShaStaged(new HashMap<String, String>());
                _currentInfo.setRemoved(removed);
                _currentInfo.setShaLog(shaLog);
                _currentInfo.setBranchCommit(branchCommit);

                Info.saveInfo(_currentInfo, gitletDirectory);
            }
        }
    }
    /*remove file given by args*/
    private static void remove(String[] args) {
        
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
        }
        else {
            String filename = args[1];
            File file = new File(filename);
          
            String commitDirectory = System.getProperty("user.dir") + "/" + ".gitlet/object/commits";

            String currCommitFileName = _currentInfo.getHeader() + ".ser";
            Commit currCommit = Commit.load(currCommitFileName, commitDirectory);

            HashMap<String, String> names = currCommit.getNames();
            HashMap<String, String> stagedFiles = _currentInfo.getNameShaStaged();
            ArrayList<String> removed = _currentInfo.getRemoved();

            String workingDirectory = System.getProperty("user.dir");
            String stagedDirectory = System.getProperty("user.dir") + "/" + ".gitlet/staged";
            String gitletDirectory = System.getProperty("user.dir") + "/" + ".gitlet";

            Path filePath = Paths.get(workingDirectory + "/" + filename);
            Path stagedPath = Paths.get(stagedDirectory + "/" + stagedFiles.get(filename));

            if (!names.containsKey(filename) && !stagedFiles.containsKey(filename)) {
                System.out.println("No reason to remove the file.");
                return;
            }
            if (removed.contains(filename)) {
                System.out.println("Already removed.");
                return;
            }
            else {
                removed.add(filename);
            }

            if (names.containsKey(filename) && file.exists()) {
                try {
                        Files.delete(filePath);
                    }
                    catch (Exception e) {
                        System.out.println("failed to delete file");
                    } 
            }
            if (stagedFiles.containsKey(filename)) {
                stagedFiles.remove(filename);
                try {
                        Files.delete(stagedPath);
                    }
                    catch (Exception e) {
                        System.out.println("failed to delete staged file");
                    } 
            }
            if (!names.containsKey(filename)) {
                removed.remove(filename);
            }
            _currentInfo.setRemoved(removed);
            _currentInfo.setNameShaStaged(stagedFiles);
            Info.saveInfo(_currentInfo, gitletDirectory);
        }   
    }
    /* check args for length and display all the parent commits of head commit*/
    private static void log(String[] args) {
        if (args.length != 1) {
            System.out.println("Incorrect operands.");
        }
        else {

            String commitDirectory = System.getProperty("user.dir") + "/" + ".gitlet/object/commits";
            String currCommitFileName = _currentInfo.getHeader() + ".ser";
            Commit currCommit = Commit.load(currCommitFileName, commitDirectory);

            printLog(currCommit);
            while (!currCommit.getParent().equals("")) {

                currCommitFileName = currCommit.getParent() + ".ser";
                currCommit = Commit.load(currCommitFileName, commitDirectory);
                printLog(currCommit);
            }
        }
    }

    private static void stat() {

        String commitDirectory = System.getProperty("user.dir") + "/" + ".gitlet/object/commits";
        String currCommitFileName = _currentInfo.getHeader() + ".ser";
        Commit currCommit = Commit.load(currCommitFileName, commitDirectory);

        HashMap <String, String> branchSha = _currentInfo.getBranchCommit();
        Iterator keys = branchSha.keySet().iterator();

        while (keys.hasNext()) {
            String key = (String) keys.next();
            System.out.println(key);
            currCommitFileName = branchSha.get(key) + ".ser";
            currCommit = Commit.load(currCommitFileName, commitDirectory);
            HashMap <String, String> names = currCommit.getNames();
            Iterator key2 = names.keySet().iterator();
            while(key2.hasNext()) {
                System.out.println(key2.next());
            }

        }
    }
   


    /*helper method that prints a given commit*/
    private static void printLog(Commit commit) {
        System.out.println("===");
        System.out.println("Commit " + commit.getSHA());
        System.out.println(commit.getTime());
        System.out.println(commit.getMsg());
        System.out.println();
    }

    /*show inof about every commit given args*/
    private static void globalLog(String[] args) {
         if (args.length != 1) {
            System.out.println("Incorrect operands.");
        }
        else {

            HashMap<String, String> shaLog = _currentInfo.getShaLog();
            Iterator keys = shaLog.keySet().iterator();
            String commitDirectory = System.getProperty("user.dir") + "/" + ".gitlet/object/commits";

            while(keys.hasNext()) {
                String key = (String) keys.next();
                
                String currCommitFileName = key + ".ser";
                Commit currCommit = Commit.load(currCommitFileName, commitDirectory);

                printLog(currCommit);
            }
        }
    }
    /*find a commit given its log message from args*/
    private static void find(String[] args) {
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
        }
        else {
            boolean commitExists = false;
            String logMessage = args[1];
            HashMap<String, String> shaLog = _currentInfo.getShaLog();
            Iterator keys = shaLog.keySet().iterator();
            String commitDirectory = System.getProperty("user.dir") + "/" + ".gitlet/object/commits";
            while(keys.hasNext()) {
                String key = (String) keys.next();
                if (shaLog.get(key).equals(args[1])) {
                    commitExists = true;
                    System.out.println(key);
                }
            } 
            if (!commitExists) {
                System.out.println("Found no commit with that message.");
            }
        }
    }

    private static void status(String[] args) {
        if (args.length != 1) {
            System.out.println("Incorrect operands.");
        }
        else {
            String commitDirectory = System.getProperty("user.dir") + "/" + ".gitlet/object/commits";
            String currCommitFileName = _currentInfo.getHeader() + ".ser";
            Commit currCommit = Commit.load(currCommitFileName, commitDirectory);

            HashMap<String, String> names = currCommit.getNames();
            HashMap<String, String> stagedFiles = _currentInfo.getNameShaStaged();
            HashMap<String, String> branches = _currentInfo.getBranchCommit();
            //modified
            Iterator keys = names.keySet().iterator();
            HashMap<String, String> shaLog = _currentInfo.getShaLog();
            //
            ArrayList<String> removed = _currentInfo.getRemoved();
            ArrayList<String> modifiedFiles = new ArrayList<String>();
            ArrayList<String> workingDirFiles = new ArrayList<String>();
            workingDirFiles.addAll(Utils.plainFilenamesIn(System.getProperty("user.dir")));
           
            String[] sortedStagedFiles = Arrays.copyOf(stagedFiles.keySet().toArray(), stagedFiles.keySet().toArray().length, String[].class);
            String[] sortedBranches = Arrays.copyOf(branches.keySet().toArray(), branches.keySet().toArray().length, String[].class);
            String[] sortedWorkingFiles = Utils.plainFilenamesInArray(System.getProperty("user.dir"));
            String[] sortedRemoved = new String[removed.size()];
            removed.toArray(sortedRemoved);

            Arrays.sort(sortedStagedFiles);
            Arrays.sort(sortedBranches);
            Arrays.sort(sortedRemoved);

            System.out.println("=== Branches ===");
            for (int x = 0; x < sortedBranches.length; x ++) {
                if(sortedBranches[x].equals(_currentInfo.getBranch())) {
                    System.out.print("*");
                }
                System.out.println(sortedBranches[x]);
               
            }
            System.out.println();
            System.out.println("=== Staged Files ===");
            for (int k = 0; k < sortedStagedFiles.length; k ++) {
                System.out.println(sortedStagedFiles[k]);
                /*staged but not in working directory*/
                if(!workingDirFiles.contains(sortedStagedFiles[k])) {
                    modifiedFiles.add(sortedStagedFiles[k]);
                }
                /*staged but different than working directory*/
                else {
                    String shaStaged = stagedFiles.get(sortedStagedFiles[k]);
                    File file = new File(sortedStagedFiles[k]);
                    byte[] fileBytes = Utils.readContents(file);
                    String shaDirectory = Utils.sha1(fileBytes);
                    
                    if(!shaDirectory.equals(shaStaged)) {
                        modifiedFiles.add(sortedStagedFiles[k]);
                    }
                }
            }
            System.out.println();
            System.out.println("=== Removed Files ===");
            for (int p = 0; p < sortedRemoved.length; p ++) {
                if (!workingDirFiles.contains(sortedRemoved[p]) && !stagedFiles.containsKey(sortedRemoved[p]) && names.containsKey(sortedRemoved[p])) {
                    modifiedFiles.add(sortedRemoved[p]);
                }
                System.out.println(sortedRemoved[p]);
            }
            for (int m = 0; m < sortedWorkingFiles.length; m ++) {
                
                File fileDir = new File(sortedWorkingFiles[m]);
                byte[] fileBytesDir = Utils.readContents(fileDir);
                String shaDir = Utils.sha1(fileBytesDir);

                if (names.containsKey(sortedWorkingFiles[m]) && !shaDir.equals(names.get(sortedWorkingFiles[m])) && !stagedFiles.containsKey(sortedWorkingFiles[m])) {
                    modifiedFiles.add(sortedWorkingFiles[m]);
                }
            }
            System.out.println();
            String[] sortedModified = new String[modifiedFiles.size()];
            modifiedFiles.toArray(sortedModified);
            Arrays.sort(sortedModified);
            System.out.println("=== Modifications Not Staged For Commit ===");
            /*
            for (int q = 0; q < sortedModified.length; q ++) {
                System.out.println(sortedModified[q]);
            }
            */
            System.out.println();
            System.out.println("=== Untracked Files ===");
            /*
            for (int r = 0; r < sortedWorkingFiles.length; r ++) {
                if (!stagedFiles.containsKey(sortedWorkingFiles[r]) && !names.containsKey(sortedWorkingFiles[r]))
                    System.out.println(sortedWorkingFiles[r]);
            }
            */
            System.out.println();
        }
    }

    private static void reset(String[] args) {
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
        }
        else {
            String givenCommitID = fullSha(args[1]);
            HashMap<String, String> commitSha = _currentInfo.getShaLog();
            ArrayList<String> workingDirFiles = new ArrayList<String>();
            workingDirFiles.addAll(Utils.plainFilenamesIn(System.getProperty("user.dir")));
            /*check if given commit exists*/
            if (!commitSha.containsKey(givenCommitID)) {
                System.out.println("No commit with that id exists.");
                return;
            }
            if (givenCommitID.equals("a")) {
                System.out.println("Given commit id is not unique.");
                return;
            }
            String[] fakeArgs = new String[4];
            fakeArgs[0] = "checkout";
            fakeArgs[1] = givenCommitID;
            fakeArgs[2] = "--";
            
            String commitDirectory = System.getProperty("user.dir") + "/" + ".gitlet/object/commits";
            String givenCommitFileName = givenCommitID + ".ser";
            Commit givenCommit = Commit.load(givenCommitFileName, commitDirectory);

            HashMap<String, String> nameSha = givenCommit.getNames();

            String currCommitFileName = _currentInfo.getHeader() + ".ser";
            Commit currCommit = Commit.load(currCommitFileName, commitDirectory);
            HashMap<String, String> names = currCommit.getNames();


            for (int k = 0; k < workingDirFiles.size(); k++) {
                if(!names.containsKey(workingDirFiles.get(k)) && nameSha.containsKey(workingDirFiles.get(k))) {
                    System.out.println("There is an untracked file in the way; delete it or add it first.");
                }
            }

            Iterator keys = nameSha.keySet().iterator();

            while(keys.hasNext()) {
                String key = (String) keys.next();
                fakeArgs[3] = key;
                checkout(fakeArgs);
            }

            Iterator keysCurr = names.keySet().iterator();

            String argsRemoved[] = new String[2];
            argsRemoved[0] = "rm";

            while(keysCurr.hasNext()) {
                String key2 = (String) keysCurr.next();
                if (!nameSha.containsKey(key2)) {
                    argsRemoved[1] = key2;
                    remove(argsRemoved);
                }  
            }

            String directory = System.getProperty("user.dir") + "/.gitlet";
            
            ArrayList<String> stagedDirFiles = new ArrayList<String>();
            stagedDirFiles.addAll(Utils.plainFilenamesIn(directory + "/staged"));
            String currentFile = "";
            Path currPath = Paths.get("");

            for (int x = 0; x < stagedDirFiles.size(); x++) {
                currentFile = stagedDirFiles.get(x);
                currPath = Paths.get(directory + "/staged/" + currentFile);
                try{ 
                    Files.delete(currPath);
                } catch (Exception e) {
                    System.out.println("File not deleted");
                }
            }
            _currentInfo.setHeader(givenCommitID);
            HashMap<String, String> branchComm = _currentInfo.getBranchCommit();
            branchComm.put(_currentInfo.getBranch(), givenCommitID);
            _currentInfo.setBranchCommit(branchComm);
            _currentInfo.setNameShaStaged(new HashMap<String, String>());
            Info.saveInfo(_currentInfo, directory);
        }
    }

    /** Checks out a particular file, particular file from a commit ID, or an entire branch,
    parsing ARGS as appropriate. */
    private static void checkout(String[] args) {
        String fileDir = System.getProperty("user.dir") + "/" + ".gitlet/object/files";
        String commitDir = System.getProperty("user.dir") + "/" + ".gitlet/object/commits";
        String workingDir = System.getProperty("user.dir");
        if (args.length == 3 && args[1].equals("--")) {
            /* Takes version of file in the current commit and puts it in working directory,
            overwriting the file previously there, if applicable. */
            String filename = args[2];
            String header = _currentInfo.getHeader();
            Commit commitIn = Commit.load(header + ".ser", commitDir);
            HashMap<String, String> commitMap = commitIn.getNames();
            if (commitMap.containsKey(filename)) {
                String fileSHA = commitMap.get(filename);
                Path source = Paths.get(fileDir + "/" + fileSHA);
                Path dest = Paths.get(workingDir + "/" + filename);
                try{ 
                    Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception e) {
                    /** Do nothing currently. */
                }
                
            } else {
                System.out.println("File does not exist in that commit.");
            }
            /* End of this case. */
        } else if (args.length == 4 && args[2].equals("--")) {
            /*Takes version of file in the commit specified and puts it in working directory,
            overwriting the file previously there if applicable. */
            String filename = args[3];
            String commitID = fullSha(args[1]);

            if (commitID.equals("")) {
                System.out.println("No commit with that id exists.");
                return;
            }
            if (commitID.equals("a")) {
                System.out.println("Given commit id is not unique.");
                return;
            }
            Commit commitIn = Commit.load(commitID + ".ser", commitDir);
            if (commitIn != null) {
                HashMap<String, String> commitMap = commitIn.getNames();
                if (commitMap.containsKey(filename)) {
                    String fileSHA = commitMap.get(filename);
                    Path source = Paths.get(fileDir + "/" + fileSHA);
                    Path dest = Paths.get(workingDir + "/" + filename);
                    try{ 
                        Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
                    } catch (Exception e) {
                        /** Do nothing currently. */
                    }
                } else {
                    System.out.println("File does not exist in that commit.");
                }
            }
            /* End of this case. */
        } else if (args.length == 2) {
            /* Checks out all the files from the current commit on the specified branch. */
            String branchName = args[1];
            if (_currentInfo.getBranchCommit().containsKey(branchName)) {
                if (_currentInfo.getBranch().equals(branchName)) {
                    System.out.println("No need to checkout the current branch.");
                    return;
                }
                //Obtain the commit ID's
                String newCID = _currentInfo.getBranchCommit().get(branchName);
                String oldCID = _currentInfo.getHeader();
                Commit newCommit = Commit.load(newCID + ".ser", commitDir);
                Commit oldCommit = Commit.load(oldCID + ".ser", commitDir);
                HashMap<String, String> newMap = newCommit.getNames();
                HashMap<String, String> oldMap = oldCommit.getNames();
                //System.out.println("What is the old map?");
                //Iterator<String> yas = oldMap.keySet().iterator();
                //while (yas.hasNext()) {
                    //System.out.println(yas.next());
                //}
                //System.out.println("Done printing OLD MAP");
                /* First check if a working file would be overwritten but is untracked in the current
                branch. Print and exit if so. */
                Iterator<String> it = newMap.keySet().iterator();
                while (it.hasNext()) {
                    String filename = it.next();
                    File file = new File(workingDir + "/" + filename);
                    if (file.exists() && (!oldMap.containsKey(filename))) {
                        System.out.println("There is an untracked file in the way; delete it or add it first.");
                        return;
                    }
                }
                /* Otherwise, no problem. */
                Iterator<String> it1 = oldMap.keySet().iterator();
                while (it1.hasNext()) {
                    String filename = it1.next();
                    //System.out.println("The file we're thinking of ");
                    //System.out.println(filename);
                    if (!newMap.containsKey(filename)) {
                        File file = new File(workingDir + "/" + filename);
                        if (file.exists()) {
                            //System.out.println("We're in the deletion loop!");
                            file.delete();
                        }
                    }
                }
                /* Now have removed all files in working dir that were present in current
                commit but not on this new branch. */
                Iterator<String> it2 = newMap.keySet().iterator();
                while (it2.hasNext()) {
                    String filename = it2.next();
                    String fileSHA = newMap.get(filename);
                    Path source = Paths.get(fileDir + "/" + fileSHA);
                    Path dest = Paths.get(workingDir + "/" + filename);
                    try{ 
                        Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
                    } catch (Exception e) {
                        /** Do nothing currently. */
                    }
                }
                /* Let's add this line to see if it fixes things. Update the branchCommit map
                with the (now old) branch name and last commit. */
                //HashMap<String, String> branchCMap = _currentInfo.getBranchCommit();
                //branchCMap.put(_currentInfo.getBranch(), oldCID);
                //_currentInfo.setBranchCommit(branchCMap);
                /* Now have taken all files in the new branch commit and put it in working
                directory, overwriting if needed. */
                _currentInfo.setNameShaStaged(new HashMap<String, String>());
                /* Clears the staging area. */
                _currentInfo.setBranch(branchName);
                /* Sets the current branch. */
                _currentInfo.setHeader(newCID);
                /* Sets the current commit on the current branch. */
                String gitletDir = System.getProperty("user.dir") + "/" + ".gitlet";
                Info.saveInfo(_currentInfo, gitletDir);
            } else {
                System.out.println("No such branch exists.");
            }
        } else {
            System.out.println("Incorrect operands.");
        }        
    }

    /** Creates a new branch with the given name, parsing ARGS as appropriate. */
    private static void branch(String[] args) {
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
        } else {
            String name = args[1];
            HashMap<String, String> branchCommitMap = _currentInfo.getBranchCommit();
            if (branchCommitMap.containsKey(name)) {
                System.out.println("A branch with that name already exists.");
            } else {
                String header = _currentInfo.getHeader();
                branchCommitMap.put(name, header);
                _currentInfo.setBranchCommit(branchCommitMap);
                String directory = System.getProperty("user.dir") + "/.gitlet";
                Info.saveInfo(_currentInfo, directory);
            }
        }
    }

    /** Removes branch with the given name, parsing ARGS as appropriate. */
    private static void rmbranch(String[] args) {
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
        } else {
            String name = args[1];
            HashMap<String, String> branchCommitMap = _currentInfo.getBranchCommit();
            if (!branchCommitMap.containsKey(name)) {
                System.out.println("A branch with that name does not exist.");
            } else if (_currentInfo.getBranch() == name) {
                System.out.println("Cannot remove the current branch.");
            } else {
                branchCommitMap.remove(name);
                _currentInfo.setBranchCommit(branchCommitMap);
                String directory = System.getProperty("user.dir") + "/.gitlet";
                Info.saveInfo(_currentInfo, directory);
            }
        }
    }
    /*takes in the sha1 of the commit given by a branch and returns the sha1 of the common ancestor with the current commit*/
    private static String getAncestor(String brCommit) {
        String ancestor = "";
        String commitDirectory = System.getProperty("user.dir") + "/" + ".gitlet/object/commits";
        String currCommitFileName = _currentInfo.getHeader() + ".ser";

        Commit currCommit = Commit.load(currCommitFileName, commitDirectory);
        String branchCommitFileName = brCommit + ".ser";
        Commit branchCommit = Commit.load(branchCommitFileName, commitDirectory);

        ArrayList<String> currCommitHistory = new ArrayList<String>();
        ArrayList<String> branchCommitHistory = new ArrayList<String>();

        currCommitHistory.add(currCommit.getSHA());
        branchCommitHistory.add(branchCommit.getSHA());

        while (!currCommit.getParent().equals("")) {
            currCommitFileName = currCommit.getParent() + ".ser";
            //System.out.println(currCommit.getParent());
            currCommit = Commit.load(currCommitFileName, commitDirectory);
            currCommitHistory.add(currCommit.getSHA());
        }
        while (!branchCommit.getParent().equals("")) {
            branchCommitFileName = branchCommit.getParent() + ".ser";
            branchCommit = Commit.load(branchCommitFileName, commitDirectory);
            branchCommitHistory.add(branchCommit.getSHA());
        }
        int sizeBranch = branchCommitHistory.size() - 1;
        int sizeCommit = currCommitHistory.size() - 1;

        String curr_ancestor = "";
        while (sizeCommit >= 0 && sizeBranch >= 0) {
            if (!branchCommitHistory.get(sizeBranch).equals(currCommitHistory.get(sizeCommit))) {
                ancestor = curr_ancestor;
                return ancestor;
            } 
            curr_ancestor = branchCommitHistory.get(sizeBranch);
            
            sizeBranch = sizeBranch - 1;
            sizeCommit = sizeCommit - 1;
        }
        
        return curr_ancestor;
    }

    /** Merges a given branch with the current branch, where given is in ARGS. */
    private static void merge(String[] args) {
        /* First we check some failure cases. 1: If there are staged additions or 
        removals present. */
        boolean passed = mergeFailureCases(args);
        if (passed) {
            String givenB = args[1];
            String currentB = _currentInfo.getBranch();
            String workingDir = System.getProperty("user.dir");
            String commitDir = System.getProperty("user.dir") + "/.gitlet/object/commits";
            String gitletDir = System.getProperty("user.dir") + "/.gitlet";
            HashMap<String, String> bcMap = _currentInfo.getBranchCommit();
            Commit currCommit = Commit.load(_currentInfo.getHeader() + ".ser", commitDir);
            HashMap<String, String> currcommitMap = currCommit.getNames();
            Commit givenCommit = Commit.load(bcMap.get(givenB) + ".ser", commitDir);
            HashMap<String, String> givencommitMap = givenCommit.getNames();
            String ancestorID = getAncestor(givenCommit.getSHA());
            Commit ancestorCommit = Commit.load(ancestorID + ".ser", commitDir);
        
            /* If split point is the same commit as the given branch. */
            if (ancestorID.equals(givenCommit.getSHA())) {
                System.out.println("Given branch is an ancestor of the current branch.");
            } else if (ancestorID.equals(currCommit.getSHA())) {
                /* Need to update current branch to the given branch. */
                _currentInfo.setHeader(givenCommit.getSHA());
                /* Need to update the branch to commit map. Will replace previous value. */
                bcMap.put(currentB, givenCommit.getSHA());
                _currentInfo.setBranchCommit(bcMap);
                Info.saveInfo(_currentInfo, gitletDir);
                System.out.println("Current branch fast-forwarded.");
            } else {
                /* When ancestor is distinct from given branch and current branch. */
                boolean isConflict = merge2(givenCommit, givencommitMap, currCommit, currcommitMap, ancestorCommit, ancestorCommit.getNames());
                /* Two different cases for printing out and commiting. */
                if (isConflict) {
                    System.out.println("Encountered a merge conflict.");
                }
                else {
                    /* Commit with the following message. */
                    String msg = "Merged " + currentB + " with " + givenB + ".";
                    String[] newargs = {"commit", msg};
                    commit(newargs);
                }
            }
        }
    }

    /** A helper method for merge, applies in the case ancestor, given, and current are distinct. Returns
    true if there were conflicted files. Commits are givenC, currentC, ancestorC. Maps are givenM, currentM, ancestorM. */
    private static boolean merge2(Commit givenC, HashMap<String, String> givenM, Commit currentC, HashMap<String, String> currentM, 
    Commit ancestorC, HashMap<String, String> ancestorM) {
        /* We first iterate through filenames in the given branch. */
        boolean returnBool = false;
        Iterator<String> it = givenM.keySet().iterator();
        while (it.hasNext()) {
            String fileBNAME = it.next();
            String givenSHA = givenM.get(fileBNAME);
            /* Two cases -- either the file is present at the ancestor or is not. Also, 
            we only do something if it's been modified since the split. */
            if (ancestorM.containsKey(fileBNAME) && (!ancestorM.get(fileBNAME).equals(givenSHA))) {
                String ancestorSHA = ancestorM.get(fileBNAME);
                if (currentM.containsKey(fileBNAME)) {
                    String currentSHA = currentM.get(fileBNAME);
                    if ((!currentSHA.equals(ancestorSHA)) && (!currentSHA.equals(givenSHA))) {
                        /* COMBINE FILE, BUT THEY HAVE A CONFLICT. DON'T STAGE */
                        combine(currentSHA, givenSHA, fileBNAME);
                        returnBool = true;

                    } else if (currentSHA.equals(ancestorSHA)) {
                        /* CHECKOUT FROM THE GIVEN BRANCH AND STAGE. OVERWRITE IF NEEDED. */
                        String[] newargs = {"checkout", givenC.getSHA(), "--", fileBNAME};
                        checkout(newargs);
                        newargs = new String[] {"add", fileBNAME};
                        add(newargs);
                    }
                } else {
                    /* In this case, file is absent in current, i.e. has been deleted from split.
                    COMBINE FILES, but DO NOT STAGE. */
                    combine(null, givenSHA, fileBNAME);
                    returnBool = true;
                }
            } else if (!ancestorM.containsKey(fileBNAME)) {
                /** File is not present at split point (ancestor). */
                if (currentM.containsKey(fileBNAME) && (!currentM.get(fileBNAME).equals(givenSHA))) {
                    /* In this case, file is present in current commit and is different. Then
                    COMBINE FILES. (They must be different.) */
                    String currentSHA = currentM.get(fileBNAME);
                    combine(currentSHA, givenSHA, fileBNAME);
                    returnBool = true;
                } else if (!currentM.containsKey(fileBNAME)) {
                    /* In this case, file is not present in current commit. CHECK IT OUT FROM GIVEN and STAGE. */
                    String[] newargs = {"checkout", givenC.getSHA(), "--", fileBNAME};
                    checkout(newargs);
                    newargs = new String[] {"add", fileBNAME};
                    add(newargs);
                }
            }
        }
        /* One remaining point: file may be absent in given branch. SO: for each file present
        at split point, if present & unmodified in current branch and absent in given branch, 
        REMOVE AND UNTRACK. */
        Iterator<String> it1 = ancestorM.keySet().iterator();
        while (it1.hasNext()) {
            String filename = it1.next();
            String ancestorSHA = ancestorM.get(filename);
            /** If the current commit contains file and is unmodified with respect to ancestor. */
            boolean check = currentM.containsKey(filename) && (!givenM.containsKey(filename)) && (ancestorSHA.equals(currentM.get(filename)));
            if (check) {
                /** DELETE file from WORKING DIR and UNTRACK. */
                String[] newargs = {"rm", filename};
                remove(newargs);
            }
        }
        /* I think there is a final case: if file is absent in given branch. AND it is modified
        in the current branch (implies with respect to the split). Then these need to be combined
        and there is conflict. */
        Iterator<String> it2 = ancestorM.keySet().iterator();
        while (it2.hasNext()) {
            String filename = it2.next();
            String ancestorSHA = ancestorM.get(filename);
            /**If the current commit contains file and is modified with respect to ancestor. */
            boolean check = currentM.containsKey(filename) && (!givenM.containsKey(filename)) && (!ancestorSHA.equals(currentM.get(filename)));
            if (check) {
                /* combine files because of conflict. DO NOT STAGE. */
                String currentSHA = currentM.get(filename);
                combine(currentSHA, null, filename);
                returnBool = true;
            }
        }
        return returnBool;
        /* Finished iterator through files in given branch commit. */
    }


    /** A helper method for MERGE that combines two files as described in specs and creates a new one, 
    overwriting existing in working directory. currentSHA is shaID of file in current commit, givenSHA 
    is in given branch, and filename is name of file. May take NULL shaID arguments, which means file doesn't exist (empty). */
    private static void combine(String currentSHA, String givenSHA, String filename) {
        String fileDir = System.getProperty("user.dir") + "/.gitlet/object/files";
        String firstLine = "<<<<<<< HEAD\n";
        String midLine = "=======\n";
        String lastLine = ">>>>>>>\n";
        String workingDir = System.getProperty("user.dir");
        File file = new File(workingDir, filename);
        byte[] currentBYTES;
        byte[] givenBYTES;
        if (currentSHA == null) {
            currentBYTES = new byte[] {};
        } else {
            File current = new File(fileDir, currentSHA);
            currentBYTES = Utils.readContents(current);
        }
        if (givenSHA == null) {
            givenBYTES = new byte[] {};
        } else {
            File given = new File(fileDir, givenSHA);
            givenBYTES = Utils.readContents(given);
        }
        /* Byte arrays need to be combined. */
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        os.write(firstLine.getBytes(), 0, firstLine.getBytes().length);
        os.write(currentBYTES, 0, currentBYTES.length);
        os.write(midLine.getBytes(), 0, midLine.getBytes().length);
        os.write(givenBYTES, 0, givenBYTES.length);
        os.write(lastLine.getBytes(), 0, lastLine.getBytes().length);
        byte[] combined = os.toByteArray();
        Utils.writeContents(file, combined);
    } 


    /** A helper method for MERGE that takes cares of the merge failure cases, 
    using ARGS. Returns true if no failures.*/
    private static boolean mergeFailureCases(String[] args) {
        String givenB = args[1];
        String currentB = _currentInfo.getBranch();
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
            return false;
        } else if ((!_currentInfo.getNameShaStaged().isEmpty()) || (!_currentInfo.getRemoved().isEmpty())) {
            System.out.println("You have uncommitted changes.");
            return false;
        } else if (!_currentInfo.getBranchCommit().containsKey(givenB)) {
            System.out.println("A branch with that name does not exist.");
            return false;
        }   else if (currentB.equals(givenB)) {
            System.out.println("Cannot merge a branch with itself.");
            return false;
        } 
        /** If there is an untracked file in current commit that would be overwritten
        or deleted by the merge, then fail. */
        String workingDir = System.getProperty("user.dir");
        String commitDir = System.getProperty("user.dir") + "/.gitlet/object/commits";
        Iterator<String> it = Utils.plainFilenamesIn(workingDir).iterator();
        Commit currCommit = Commit.load(_currentInfo.getHeader() + ".ser", commitDir);
        HashMap<String, String> currcommitMap = currCommit.getNames();
        HashMap<String, String> bcMap = _currentInfo.getBranchCommit();
        /** Note the branch must exist, already checked, and has a commit. */
        Commit givenCommit = Commit.load(bcMap.get(givenB) + ".ser", commitDir);
        HashMap<String, String> givencommitMap = givenCommit.getNames();
        while (it.hasNext()) {
            String workFilename = it.next();
            /** If not in current commit. */
            if ((!currcommitMap.containsKey(workFilename)) && givencommitMap.containsKey(workFilename)) {
                /** If it's in the given branch (commit) and has different sha-ID, then
                there will be an overwriting problem. Compute the sha-ID of working file. */
                File file = new File(workFilename);
                byte[] fileBytes = Utils.readContents(file);
                String fileID = Utils.sha1(fileBytes);
                if (!fileID.equals(givencommitMap.get(workFilename))) {
                    System.out.println("There is an untracked file in the way; delete it or add it first.");
                    return false;
                }
            }
        }
        return true;
    }

    /* represents the current state of information*/
    private static Info _currentInfo;
}
