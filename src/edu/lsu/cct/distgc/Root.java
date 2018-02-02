package edu.lsu.cct.distgc;

import java.util.ArrayList;
import java.util.List;

public class Root {

    static List<Root> roots = new ArrayList<>();
    private Node root;
    int index = -1;

    Root(Integer... forceId) {
        roots.add(this);
        if (forceId.length == 0) {
            root = new Node();
        } else {
            assert forceId.length == 1;
            try {
                root = new Node(forceId[0]);
            } catch (Exception e) {
                System.out.println("Node id " + forceId[0] + "already exists!");
                assert false;
            }
        }

        // Store all root references in a table
        // so that we can find them later.
        for (int i = 0; i < roots.size(); i++) {
            if (roots.get(i) == null) {
                index = i;
                break;
            }
        }
        if (index < 0) {
            index = roots.size();
        }

        Message m = new IncrMessage(0, root.id, 0, false, null);
        m.runMe();
    }

    public void set(Node node) {
        set(node,false);
    }
    public void set(Node node,boolean runNow) {
        if (node != null) {
            Message m = new IncrMessage(0, node.id, 0, false, null);
            if(runNow)
                m.run();
            else
                m.runMe();
        }
        if (root != null) {
            Message m = new DecrMessage(0, root.id, 0, false, null);
            if(runNow)
                m.queueMe();
            else
                m.run();
        }
        root = node;
    }

    public Node get() {
        return root;
    }

    public int getId() {
        return get().id;
    }
}
