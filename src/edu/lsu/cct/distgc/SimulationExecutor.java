package edu.lsu.cct.distgc;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.HashMap;

public final class SimulationExecutor {

    public boolean CONGEST_mode = false;

    // Parses the input file and runs the simulation.
    public SimulationExecutor(String fileloc) {
        roots = new HashMap<>();
        try {
            // Open the file
            FileInputStream fstream = new FileInputStream(fileloc);
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
            String strLine;
            int lineNo = 1;
            // Read File Line By Line
            while ((strLine = br.readLine()) != null) {
                // Print the content on the console
                if (Here.VERBOSE) {
                    System.out.println("Processing line: " + strLine);
                }
                if (!parseAndPerform(strLine, lineNo)) {
                    System.out.println("Input file is not compilable!");
                    return;
                }
                lineNo++;
            }
            // Close the input stream
            br.close();
            System.out.println("[=============Pending=Messages==============]");
            int count = 0;
            for(Message m : Message.msgs) {
                if(!m.done()) {
                    System.out.println("m="+m);
                    count++;
                }
            }
            if(count == 0)
                System.out.println("None");
        } catch (Exception e) {
            System.out.println("Exception is " + e.getMessage());
            e.printStackTrace(new PrintStream(System.out));
        }
    }

    public void unSetSyncMode() {
        Message.CONGEST_mode = false;
        System.out.println("sync mode off");
    }

    public void actionFinished() {
        Message.CONGEST_mode = CONGEST_mode;
    }

    // Parses CREATE <num> statements and performs necessary action.
    public void createStatement(String[] tokens, int lineNo) {
        assert tokens.length == 2 : " Line : " + lineNo + " error. Create statement syntax is CREATE <number> .";
        int id = Integer.parseInt(tokens[1]);
        assert id > 0 : " Line : " + lineNo + " error. Node Id cannot be less 0 or negative number. ";
        roots.put(id, new Root(id));
        actionFinished();
    }

    // Parses UNROOT <num> statements and performs necessary action.
    public void unRootNode(String[] tokens, int lineNo) {
        assert tokens.length == 2 : " Line : " + lineNo + " error. unroot statement syntax is UNROOT <number> .";
        int id = Integer.parseInt(tokens[1]);
        assert id > 0 : " Line : " + lineNo + " error. Node Id cannot be less 0 or negative number. ";
        Root node = roots.get(id);
        node.set(null);
    }

    public void processRunAll(String[] tokens, int lineNo) {
        Message.runAll();
    }

	public void processCheckStat(String[] tokens, int lineNo) {
		Message.checkStat();
	}


    // runmsg <nodeId> <MsgId> lines are parsed, checked and executed.
	public void processMsgId(String[] tokens, int lineNo) {
        assert tokens.length == 3 : "Line: " + lineNo
                + " error. runmsg <nodeId> <msgId> is the syntaxt for the statement.";
        int nodeId = Integer.parseInt(tokens[1]);
        int msgId = Integer.parseInt(tokens[2]);
        assert nodeId > 0 && msgId >= 0 : "Line: " + lineNo
        		+ " error. nodeId or msg Id must be positive number.";
        Message.runMsg(nodeId, msgId);
	}


    // EDGE <source-node> <dest-node> lines are parsed, checked and executed.
    public void createEdge(String[] tokens, int lineNo) {
        assert tokens.length == 3 : "Line: " + lineNo
                + " error. EDGE <source-node> <dest-nod> is the syntaxt for the statement.";
        int sourceNode = Integer.parseInt(tokens[1]);
        int destNode = Integer.parseInt(tokens[2]);
        assert sourceNode > 0 && destNode > 0 : "Line: " + lineNo
                + " error. source or dest node cannot be root node (0) or negative number";
        Root source = roots.get(sourceNode);
        source.get().createEdge(destNode);
        actionFinished();
    }

    public void CONGESTModeOn(String[] tokens, int lineNo) {
        System.out.println("sync mode on");
        Message.CONGEST_mode = true;
        CONGEST_mode = true;
    }

    // DELEDGE <source-node> <dest-node> lines are parsed, checked and executed.
    public void deleteEdge(String[] tokens, int lineNo) {
        assert tokens.length == 3 : "Line: " + lineNo
                + " error. EDGE <source-node> <dest-nod> is the syntaxt for the statement.";
        int sourceNode = Integer.parseInt(tokens[1]);
        int destNode = Integer.parseInt(tokens[2]);
        assert sourceNode > 0 && destNode > 0 : "Line: " + lineNo
                + " error. source or dest node cannot be root node (0) or negative number";
        Root source = roots.get(sourceNode);
        source.get().removeEdge(destNode);
    }

    // Parses individual lines and delegates the action to particular action
    // methods.
    public boolean parseAndPerform(String strLine, int lineNo) {
        String[] tokens = strLine.split("\\s+");
        assert tokens.length >= 1 : "Instruction \"" + strLine + " \" in line no " + lineNo + " is incomplete!";
        switch (tokens[0].toLowerCase()) {
            case "#":
                // Ignore line as comment.
                break;
            case "//":
                // Ignore line as comment.
                break;
            case "CONGESTmode":
                CONGESTModeOn(tokens, lineNo);
                break;
            case "create":
                createStatement(tokens, lineNo);
                break;
            case "runall":
                processRunAll(tokens, lineNo);
                break;
            case "runmsg":
            	processMsgId(tokens, lineNo);
            	break;
            case "checkstat":
            	processCheckStat(tokens, lineNo);
            	break;
            case "edge":
                createEdge(tokens, lineNo);
                break;
            case "deledge":
                deleteEdge(tokens, lineNo);
                break;
            case "unroot":
                unRootNode(tokens, lineNo);
                break;
            default:
                if (Here.VERBOSE) {
                    System.out.println("Default no-op action exectued for line " + strLine + " at line no: " + lineNo);
                }
        }

        return true;
    }

    private HashMap<Integer, Root> roots;
}
