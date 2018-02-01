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
            // If there's only one message in the queue,
            // just run it. The user has no choice.
            while(Message.msgs.size()==1) {
                System.out.println("Running only pending message");
                Message.runOne();
            }
        }
        // Close the input stream
        br.close();
        // Show summary information if we're done
        if(Message.msgs.size()==0)
            Message.runAll();
        Here.bar("Pending Messages");
        int count = 0;
        for(Message m : Message.msgs) {
            if(!m.done()) {
                System.out.println("m="+m.shortName()+" "+m.sender+"->"+m.recipient+" (runmsg "+m.recipient+" "+m.msg_id+")");
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
        assert sourceNode > 0 && destNode > 0 : "Line: " + lineNo
                + " error. source or dest node cannot be root node (0) or negative number";
        Node source = findFromRoot(sourceNode);
        if(source == null)
            throw new RuntimeException("Node is dead: "+sourceNode);
        Node dest = findFromRoot(destNode);
        if(dest == null)
            throw new RuntimeException("Node is dead: "+destNode);
        source.createEdge(destNode);
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
        Node source = findFromRoot(sourceNode);
        if(source == null)
            throw new RuntimeException("Node id=" + sourceNode + " is not reachable from the root");
        boolean success = source.removeEdge(destNode);
        assert success : "Remove non-existent edge";
    }

    static Pattern noopPat = Pattern.compile("^\\s*(#.*|$)");
    static Pattern msgPat  = Pattern.compile("^\\s*(\\w+)\\s*(\\d+)\\s*->\\s*(\\d+)");

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
                if(noopPat.matcher(strLine).matches()) {
                    ;
                } else {
                    Matcher m = msgPat.matcher(strLine);
                    if(m.matches()) {
                        Message found = null;
                        for(Message msg : Message.msgs) {
                            if(msg.shortName().equals(m.group(1))
                                && msg.sender == Integer.parseInt(m.group(2))
                                && msg.recipient == Integer.parseInt(m.group(3)))
                            {
                                if(found != null) throw new RuntimeException("Non-unique message specifier: "+strLine+" at line no: "+lineNo);
                                found = msg;
                            }
                        }
                        if(found == null) throw new RuntimeException("No such message: "+strLine+" at line no: "+lineNo);
                        try {
                            Message.runMsg(found.recipient,found.msg_id);
                        } catch(NoSuchMessage nsm) {
                            throw new NoSuchMessage(strLine);
                        }
                    } else {
                        throw new RuntimeException("Default no-op action exectued for line " + strLine + " at line no: " + lineNo);
                    }
                }
        }

        return true;
    }

    private HashMap<Integer, Root> roots;

    Node findFromRoot(int needle) {
        for (Node node : Node.nodeMap.values()) {
            node.marked = false;
        }
        Node n = null;
        for (Root root : roots.values()) {
            Node node = root.get();
            if(node != null) {
                n = findFromRoot(node,needle);
                if(n != null) {
                    break;
                }
            }
        }
        return n;
    }
    Node findFromRoot(Node node,int needle) {
        if(node.marked)
            return null;
        node.marked = true;
        if(node.id == needle) {
            return node;
        }
        for(Integer edge : node.edges) {
            if(edge != null) {
                Node edgeNode = Node.nodeMap.get(edge);
                Node n = findFromRoot(edgeNode,needle);
                if(n != null) {
                    return n;
                }
            }
        }
        return null;
    }
}
