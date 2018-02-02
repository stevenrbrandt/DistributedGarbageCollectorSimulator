package edu.lsu.cct.distgc;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public final class SimulationExecutor {

    public boolean CONGEST_mode = false;

    // Parses the input file and runs the simulation.
    public SimulationExecutor(String fileloc) throws Exception {
        roots = new HashMap<>();
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
        // If there's only one message in the queue,
        // just run it. The user has no choice.
        while(Message.msgs.size()==1) {
            System.out.println("Running only pending message");
            Message.runOne();
        }
        // Show summary information if we're done
        if(Message.msgs.size()==0)
            Message.runAll();
        Here.bar("Pending Messages");
        int count = 0;
        for(Message m : Message.msgs) {
            if(!m.done()) {
                System.out.println("m="+m+" node: "+Node.nodeMap.get(m.recipient));
                count++;
            }
        }
        if(count == 0)
            System.out.println("None");
    }

    public void unSetSyncMode() {
        Message.CONGEST_mode = false;
        System.out.println("sync mode off");
    }

    public void actionFinished() {
        //Message.CONGEST_mode = CONGEST_mode;
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
        try {
            Message.runMsg(nodeId, msgId);
        } catch(NoSuchMessage nsm) {
            throw new NoSuchMessage(String.format("%s %s %s",tokens[0],tokens[1],tokens[2]));
        }
	}

    static boolean pendingDelIncr() {
        for(Message m : Message.msgs) {
            if(m instanceof DecrMessage)
                return true;
            if(m instanceof IncrMessage)
                return true;
        }
        return false;
    }


    // EDGE <source-node> <dest-node> lines are parsed, checked and executed.
    public void createEdge(String[] tokens, int lineNo) {
        assert !pendingDelIncr() : "Cannot create a new edge while there are pending delete or create messages: line="+lineNo;
        assert tokens.length == 3 : "Line: " + lineNo
                + " error. EDGE <source-node> <dest-nod> is the syntaxt for the statement.";
        int sourceNode = Integer.parseInt(tokens[1]);
        int destNode = Integer.parseInt(tokens[2]);
        if(sourceNode == 0) {
            boolean found = false;
            if(roots.get(destNode) != null)
                throw new RuntimeException("A root edge to node to "+sourceNode+" already exists: line="+lineNo);
            for(Root root : roots.values()) {
                Node node = root.get();
                if(node != null) {
                    if(node.id == destNode)
                        found = true;
                    for(Integer edge : node.edges) {
                        if(edge == destNode)
                            found = true;
                    }
                    if(found) break;
                }
            }
            if(!found)
                throw new RuntimeException(
                    "To add a root edge to a Node N, there must be a node M such that 0 -> M -> N. line="+lineNo);
            Root r = new Root();
            r.set(Node.nodeMap.get(destNode));
            roots.put(destNode,r);
        } else if(sourceNode > 0) {
            assert destNode > 0 : "Line: " + lineNo
                + " error. dest node cannot be root node (0) or negative number";
            Node source = roots.get(sourceNode).get();
            if(source == null)
                throw new RuntimeException("Node is dead: "+sourceNode);
            Node dest = roots.get(destNode).get();
            if(dest == null)
                throw new RuntimeException("Node is dead: "+destNode);
            source.createEdge(destNode,true);
        } else {
            throw new RuntimeException("Source node cannot be negative. line="+lineNo);
        }
        actionFinished();
    }

    public void CONGESTModeOn(String[] tokens, int lineNo) {
        System.out.println("sync mode on");
        Message.CONGEST_mode = true;
        CONGEST_mode = true;
    }

    // DELEDGE <source-node> <dest-node> lines are parsed, checked and executed.
    public void deleteEdge(String[] tokens, int lineNo) {
        assert !pendingDelIncr() : "Cannot delete an edge while there are pending delete or create messages: line="+lineNo;
        assert tokens.length == 3 : "Line: " + lineNo
                + " error. EDGE <source-node> <dest-nod> is the syntaxt for the statement.";
        int sourceNode = Integer.parseInt(tokens[1]);
        int destNode = Integer.parseInt(tokens[2]);
        assert sourceNode > 0 && destNode > 0 : "Line: " + lineNo
                + " error. source or dest node cannot be root node (0) or negative number";
        Node source = roots.get(sourceNode).get();
        if(source == null)
            throw new RuntimeException("No edge from a root to node id=" + sourceNode + ": line="+lineNo);
        boolean success = source.removeEdge(destNode, true);
        assert success : "Remove non-existent edge";
    }

    static Pattern noopPat = Pattern.compile("^\\s*(#.*|$)");
    static Pattern msgPat  = Pattern.compile("^\\s*(\\w+)\\s*(\\d+)\\s*->\\s*(\\d+)");
    static Pattern parse   = Pattern.compile("^\\s*(\\w+)(?:\\s+(\\d+)(?:\\s*->\\s*(\\d+)|)|)");

    // Parses individual lines and delegates the action to particular action
    // methods.
    public boolean parseAndPerform(String strLine, int lineNo) {
        String[] tokens = {};
        Matcher mtok = parse.matcher(strLine);
        if(mtok.matches()) {
            int count = 0;
            for(int i=0;i<mtok.groupCount();i++)
                if(mtok.group(i+1) != null)
                    count++;
            tokens = new String[count];
            for(int i=0;i<count;i++) {
                tokens[i] = mtok.group(i+1);
            }
        } else if(noopPat.matcher(strLine).matches()) {
            tokens = new String[]{"#"};
        } else {
            throw new RuntimeException(strLine);
        }
        switch (tokens[0].toLowerCase()) {
            case "#":
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
                Message found = null;
                for(Message msg : Message.msgs) {
                    if(msg.shortName().equals(tokens[0])
                            && msg.sender == Integer.parseInt(tokens[1])
                            && msg.recipient == Integer.parseInt(tokens[2]))
                    {
                        if(found != null) throw new RuntimeException("Non-unique message specifier: "+strLine+" at line no: "+lineNo);
                        found = msg;
                    }
                }
                if(found == null) throw new RuntimeException("Invalid instruction: "+strLine+" at line no: "+lineNo);
                try {
                    Message.runMsg(found.recipient,found.msg_id);
                } catch(NoSuchMessage nsm) {
                    throw new NoSuchMessage(strLine);
                }
        }

        return true;
    }

    private HashMap<Integer, Root> roots;
}
