package edu.lsu.cct.distgc;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public final class SimulationExecutor {

    Adversary adv = new Adversary();

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
        // Show summary information if we're done
        if(Message.msgs.size()==0)
            Message.runAll();
        else
            Message.stateOfNodes();
        pendingMessages();
    }

    public void pendingMessages() {
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

    // Parses CREATE <num> statements and performs necessary action.
    public void createStatement(List<String> tokens, int lineNo) {
        System.out.println("create: "+tokens);
        assert tokens.size() >= 2 : " Line : " + lineNo + " error. Create statement syntax is CREATE <number> .";
        Node prev = null;
        for(int i=1;i<tokens.size();i++) {
            int id = Integer.parseInt(tokens.get(i));
            assert id > 0 : " Line : " + lineNo + " error. Node Id cannot be less 0 or negative number. ";
            Node node = Node.nodeMap.get(id);
            if(node == null) {
                node = new Node(id);
                if(prev != null)
                    node.weight = prev.weight+1;
            }
            assert node.id == id;
            if(i == 1) {
                if(roots.get(id)==null)
                    roots.put(id, new Root(adv,node));
                else
                    assert tokens.size() > 2 : "Create statement does nothing: line="+lineNo;
            } else {
                prev.createEdge(id,adv);
            }

            prev = node;
        }
    }

    // Parses UNROOT <num> statements and performs necessary action.
    public void unRootNode(List<String> tokens, int lineNo) {
        int id = Integer.parseInt(tokens.get(2));
        assert id > 0 : " Line : " + lineNo + " error. Node Id cannot be less 0 or negative number. line="+lineNo;
        Root node = roots.get(id);
        assert node != null : "No edge from 0->"+id+" exists: line="+lineNo;
        assert node.get() != null : "Edge 0->"+id+" is already deleted: line="+lineNo;
        node.set(null,adv);
    }

    public void processRunAll(List<String> tokens, int lineNo) {
        Message.runAll();
    }

	public void processCheckStat(List<String> tokens, int lineNo) {
		Message.checkStat();
	}


    // runmsg <nodeId> <MsgId> lines are parsed, checked and executed.
	public void processMsgId(List<String> tokens, int lineNo) {
        assert tokens.size() == 3 : "Line: " + lineNo
                + " error. runmsg <nodeId> <msgId> is the syntaxt for the statement.";
        int nodeId = Integer.parseInt(tokens.get(1));
        int msgId = Integer.parseInt(tokens.get(2));
        assert nodeId > 0 && msgId >= 0 : "Line: " + lineNo
        		+ " error. nodeId or msg Id must be positive number.";
        try {
            Message.runMsg(nodeId, msgId);
        } catch(NoSuchMessage nsm) {
            throw new NoSuchMessage(String.format("%s %s %s",tokens.get(0),tokens.get(1),tokens.get(2)));
        }
	}

    // EDGE <source-node> <dest-node> lines are parsed, checked and executed.
    public void createEdge(List<String> tokens, int lineNo) {
        assert tokens.size() == 3 : "Line: " + lineNo
                + " error. EDGE <source-node> <dest-nod> is the syntaxt for the statement.";
        int sourceNode = Integer.parseInt(tokens.get(1));
        int destNode = Integer.parseInt(tokens.get(2));
        if(sourceNode == 0) {
            boolean found = false;
            Root r = roots.get(destNode);
            if(r != null && r.get() != null)
                throw new RuntimeException("A root edge to node to "+destNode+" already exists: line="+lineNo);
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
            if(r == null) r = new Root(adv);
            r.set(Node.nodeMap.get(destNode),adv);
            roots.put(destNode,r);
        } else if(sourceNode > 0) {
            assert destNode > 0 : "Line: " + lineNo
                + " error. dest node cannot be root node (0) or negative number";
            Node source = roots.get(sourceNode).get();
            if(source == null)
                throw new RuntimeException("edge 0->"+sourceNode+" is required");
            Node dest = roots.get(destNode).get();
            if(dest == null)
                throw new RuntimeException("edge 0->"+destNode+" is required");
            source.createEdge(destNode,adv);
        } else {
            throw new RuntimeException("Source node cannot be negative. line="+lineNo);
        }
    }

    // DELEDGE <source-node> <dest-node> lines are parsed, checked and executed.
    public void deleteEdge(List<String> tokens, int lineNo) {
        assert tokens.size() == 3 : "Line: " + lineNo
                + " error. EDGE <source-node> <dest-nod> is the syntaxt for the statement: line="+lineNo;
        int sourceNode = Integer.parseInt(tokens.get(1));
        int destNode = Integer.parseInt(tokens.get(2));
        if(sourceNode == 0) {
            unRootNode(tokens,lineNo);
            return;
        }
        assert sourceNode > 0 && destNode > 0 : "Line: " + lineNo
                + " error. source or dest node cannot be root node (0) or negative number: line="+lineNo;
        Root root = roots.get(sourceNode);
        assert root != null : "No edge from the root to node "+sourceNode+" exists: line="+lineNo;
        Node source = root.get();
        if(source == null)
            throw new RuntimeException("No edge from a root to node id=" + sourceNode + ": line="+lineNo);
        boolean success = source.removeEdge(destNode, adv);
        assert success : "Remove non-existent edge";
    }

    static Pattern noopPat = Pattern.compile("^\\s*(#.*|$)");
    static Pattern msgPat  = Pattern.compile("^\\s*(\\w+)\\s*(\\d+)\\s*->\\s*(\\d+)");
    static Pattern parse   = Pattern.compile("^\\s*(\\w+)(?:\\s+(\\d+)(?:\\s*->\\s*(?:\\d+))*|)");
    static Pattern arrow   = Pattern.compile("->\\s*(\\d+)");

    // Parses individual lines and delegates the action to particular action
    // methods.
    public boolean parseAndPerform(String strLine, int lineNo) {
        List<String> tokens = new ArrayList<>();
        Matcher mtok = parse.matcher(strLine);
        if(mtok.matches()) {
            int count = 0;
            for(int i=0;i<mtok.groupCount();i++)
                if(mtok.group(i+1) != null)
                    count++;
            for(int i=0;i<count;i++) {
                tokens.add(mtok.group(i+1));
            }
            Matcher amatch = arrow.matcher(strLine);
            while(amatch.find()) {
                tokens.add(amatch.group(1));
            }
        } else if(noopPat.matcher(strLine).matches()) {
            tokens.add("#");
        } else {
            throw new RuntimeException(strLine);
        }
        switch (tokens.get(0).toLowerCase()) {
            case "#":
                // Ignore line as comment.
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
            default:
                Message found = null;
                for(Message msg : Message.msgs) {
                    if(msg.shortName().equals(tokens.get(0))
                            && msg.sender == Integer.parseInt(tokens.get(1))
                            && msg.recipient == Integer.parseInt(tokens.get(2)))
                    {
                        if(found != null) throw new RuntimeException("Non-unique message specifier: "+strLine+" at line no: "+lineNo);
                        found = msg;
                    }
                }
                if(found == null) {
                    pendingMessages();
                    throw new RuntimeException("Invalid instruction: "+strLine+" at line no: "+lineNo);
                }
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
