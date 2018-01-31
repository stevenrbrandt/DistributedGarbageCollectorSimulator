package edu.lsu.cct.distgc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Message {

    private static int msg_seq = 1;
    public static boolean CONGEST_mode = false;
    final int msg_id = msg_seq++;
    final int sender, recipient;
    private boolean done;
    boolean action = true;

    Message(int s, int r) {
        sender = s;
        recipient = r;
    }

    public boolean isDone() {
        return done;
    }

    static int roundCount = 0;
    static int messageCount = 0;

    public void run() {
        assert !done;
        done = true;
        Node r = Node.nodeMap.get(recipient);
        if (Here.VERBOSE) {
            System.out.printf("%s: %s%n", sendStr(), r);
        }
        r.preCheck(sender, action);
        runTask(r);
        if (Here.VERBOSE) {
            System.out.printf("~%s: %s%n", sendStr(), r);
        }
        r.postCheck(sender, action);
        messageCount++;
    }

    public abstract void runTask(Node n);

    public String getName() {
        String nm = getClass().getName();
        int b = nm.lastIndexOf('.') + 1;
        int e = nm.indexOf("Message");
        return nm.substring(b, e);
    }

    final static RandomSeq RAND = new RandomSeq();

    static {
        int s = RAND.nextInt(10000);
        String seedStr = System.getProperty("seed");
        if (seedStr != null) {
            s = Integer.parseInt(seedStr);
        }
        System.out.printf("seed=%d%n", s);
        RAND.setSeed(s);
    }

    /**
     * The global message table. Once messages are enqueued, they run in a
     * completely random order.
     */
    static MessageQueue msgs = new MessagesOvertake();
    // static MessageQueue msgs = new MessageDoNotOvertake();

    public String sendStr() {
        return String.format("%s(mid=%d,%d->%d)", getName(), msg_id, sender, recipient);
    }

    public static void send(Message m) {
        StackTraceElement[] elems = new Throwable().getStackTrace();
        StackTraceElement elem = null;
        if(elems.length > 2) {
            elem = elems[2];
        } else {
            elem = elems[elems.length-1];
        }
        if (Here.VERBOSE) {
            System.out.printf(" -->%s / (%s:%d)%n", m.sendStr(), elem.getFileName(), elem.getLineNumber());
        }
        msgs.sendMessage(m);
    }

    /**
     * Run a single message
     *
     * @return
     */
    public static boolean runOne() {
        if (CONGEST_mode) {
            return runOneRound();
        }
        Message m = msgs.nextToRun();
        if (m == null) {
            return false;
        }
        m.run();
        return true;
    }

    static int timeStep = 1;

    /**
     * Run one message randomly chosen in each node
     * @return true if a message was run
     */
    public static boolean runOneRound() {
        List<Message> mails = msgs.nextRoundToRun();
        if (mails.isEmpty()) {
            return false;
        } else {
            if (Here.VERBOSE) {
                System.out.printf("Time Step=%d%n", timeStep++);
            }
            roundCount++;
            for (Message msg : mails) {
                msg.run();
            }
            return true;
        }
    }

    /**
     * Send the current message and wait until it completes.
     */
    public void runMe() {
        send(this);
        if (CONGEST_mode) {
            while (!isDone()) {
                boolean success = runOneRound();
                assert success;
            }
        } else {
            while (!isDone()) {
                boolean success = runOne();
                assert success;
            }
        }
    }

    /**
     * Send the current message, but don't wait for it to complete.
     */
    public void queueMe() {
        assert sender == 0 || Node.nodeMap.get(sender) != null;
        assert Node.nodeMap.get(recipient) != null : "No such object " + recipient;
        send(this);
    }

    static void checkCounts(Map<Integer, Node.Counts> counts, Node node) {
        List<Node> nodesToCheck = new ArrayList<>();
        List<Node> moreNodesToCheck = new ArrayList<>();
        nodesToCheck.add(node);
        while (true) {
            for (Node n : nodesToCheck) {
                checkCounts(counts, n, moreNodesToCheck);
            }
            if (moreNodesToCheck.size() == 0) {
                break;
            } else {
                nodesToCheck = moreNodesToCheck;
                moreNodesToCheck = new ArrayList<>();
            }
        }
    }

    static void checkCounts(Map<Integer, Node.Counts> counts, Node node, List<Node> nodesToCheck) {
        Node.Counts c0 = counts.get(node.id);
        if (c0.marked) {
            return;
        }
        c0.marked = true;
        for (Integer edge : node.edges) {
            if (edge != null) {
                Node target = Node.nodeMap.get(edge);
                Node.Counts c = counts.get(target.id);
                if (c == null) {
                    counts.put(target.id, c = new Node.Counts());
                }
                if (node.weight < target.weight) {
                    c.strong++;
                } else {
                    c.weak++;
                }
                System.out.println("link => " + target.id + " c=" + c);
                // checkCounts(counts,target);
                nodesToCheck.add(target);
            }
        }
    }

    /**
     * Start from the roots and check the strong and weak counts of all nodes
     * and make sure they are correct.
     */
    static void checkCounts() {
        Map<Integer, Node.Counts> counts = new HashMap<>();
        for (Root root : Root.roots) {
            Node node = root.get();
            if (node == null) {
                continue;
            }
            Node.Counts c = counts.get(node.id);
            if (c == null) {
                counts.put(node.id, c = new Node.Counts());
            }
            c.strong++;
            // c.marked=true;
            System.out.println("root => " + node.id + " c=" + c);
            Message.checkCounts(counts, node);
        }
        System.out.println("==State of nodes==");
        for (Node node : Node.nodeMap.values()) {
            if (node.cd == null || node.cd.state != CollectorState.dead_state) {
                System.out.println(node);
            }
        }
        System.out.println("==================");
        for (Node node : Node.nodeMap.values()) {
            Node.Counts c = counts.get(node.id);
            if (node.cd != null) {
                assert node.cd.state == CollectorState.dead_state : "FAILURE: Node is neither recovered nor dead.";

                if (c == null) {
                    assert node.strong_count == 0 : "strong count on deleted node: " + node;
                    assert node.weak_count == 0 : "weak count on deleted node " + node;
                } else {
                    assert node.strong_count == c.strong : String.format("%d: %d != %d", node.id, node.strong_count,
                            c.strong);
                    assert node.weak_count == c.weak : String.format("%d: %d != %d", node.id, node.weak_count, c.weak);
                }
            } else {
                assert node.strong_count > 0 : "strong count = 0 on live node " + node;
                assert c != null : node;
                assert c.strong == node.strong_count : "Bad strong: " + c + " != " + node;
                assert c.weak == node.weak_count : "Bad weak: " + c + " != " + node;
                assert c.marked;
            }
        }
    }

    static int edgeCount() {
        int edges = 0;
        for (Node node : Node.nodeMap.values()) {
            for (Integer edge : node.edges) {
                if (edge != null) {
                    edges++;
                }
            }
            for (Integer edge : node.del_edges) {
                if (edge != null) {
                    edges++;
                }
            }
        }
        return edges;
    }

    /**
     * Execute all messages.
     */
    public static int runAll() {
        int rcount = roundCount;
        int mcount = messageCount;
        int edges = edgeCount();

        Here.log("Run All");
        while (runOne())
			;
        System.out.printf("Number of edges: %d%n", edges);
        System.out.printf("Number of rounds to converge: %d/%d%n", roundCount - rcount, roundCount);
        System.out.printf("Number of messages to converge: %d/%d%n", messageCount - mcount, messageCount);
        Message.checkCounts();
        Message.markAndSweep();
        return rcount;
    }
    
    /** 
     * Execute msg specified
     */
    public static void runMsg(int nodeId, int msgId) {
    	Message msg = msgs.getMessage(nodeId, msgId);
    	msg.run();
    }
    
    /**
     * Performs post collection checks
     */
    public static void checkStat(){
    	Message.checkCounts();
    	Message.markAndSweep();
    }

    private static void markAndSweep() {
        for (Node node : Node.nodeMap.values()) {
            node.marked = false;
        }
        for (Root root : Root.roots) {
            if (root.get() != null) {
                markAndSweep(root.get());
            }
        }
        for (Node node : Node.nodeMap.values()) {
            if (node.marked) {
                assert node.cd == null && node.strong_count > 0 : "" + node;
            } else {
                assert node.cd.state == CollectorState.dead_state && node.cd.wait_count == 0;
            }
        }
    }

    private static void markAndSweep(Node startNode) {
        List<Node> nodesToSweep = new ArrayList<>();
        List<Node> moreNodesToSweep = new ArrayList<>();
        nodesToSweep.add(startNode);
        while (true) {
            for (Node node : nodesToSweep) {
                markAndSweep(node, moreNodesToSweep);
            }
            if (moreNodesToSweep.size() == 0) {
                break;
            } else {
                nodesToSweep = moreNodesToSweep;
                moreNodesToSweep = new ArrayList<>();
            }
        }
    }

    private static void markAndSweep(Node node, List<Node> nodesToSweep) {
        if (node.marked) {
            return;
        }
        node.marked = true;
        for (Integer edge : node.edges) {
            if (edge != null) {
                Node edgeNode = node.nodeMap.get(edge);
                if (!edgeNode.marked) {
                    nodesToSweep.add(edgeNode);
                }
            }
        }
        for (Integer edge : node.del_edges) {
            if (edge != null) {
                Node edgeNode = node.nodeMap.get(edge);
                if (!edgeNode.marked) {
                    nodesToSweep.add(edgeNode);
                }
            }
        }
    }

    public static int count() {
        return msgs.size();
    }
}
