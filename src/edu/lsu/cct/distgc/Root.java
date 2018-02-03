package edu.lsu.cct.distgc;

import java.util.ArrayList;
import java.util.List;

public class Root {

    static List<Root> roots = new ArrayList<>();
    private Node root;
    int index = -1;
    Adversary adv;

    Root(Adversary adv) {
        this(adv,new Node());
    }

    Root(Adversary adv,Node node) {
        roots.add(this);
        this.adv = adv;
        assert adv != null;

        root = node;

        Message m = new IncrMessage(0, root.id, 0, false, null, adv);
        m.queueMe();
    }

    Root(Adversary adv,int forceId) {
        this(adv,new Node(forceId));
    }

    public void set(Node node,Adversary adv) {
        assert this.adv == adv;
        assert adv != null;
        if (node != null) {
            Message m = new IncrMessage(0, node.id, 0, false, null, adv);
            m.queueMe();
        }
        if (root != null) {
            Message m = new DecrMessage(0, root.id, 0, false, null, adv);
            m.queueMe();
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
