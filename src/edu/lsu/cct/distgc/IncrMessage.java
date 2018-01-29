package edu.lsu.cct.distgc;

// TODO: If a recovered node creates a link, it needs to increment the target rcc.
public class IncrMessage extends Message {

    int weight;
    boolean phantom_flag;
    CID cid;

    public IncrMessage(int sender, int receiver, int weight, boolean phantom_flag, CID cid) {
        super(sender, receiver);
        this.weight = weight;
        this.phantom_flag = phantom_flag;
        this.cid = cid;
        action = false;
    }

    @Override
    public void runTask(Node n) {
        n.incr(weight, phantom_flag, cid);
    }

    @Override
    public String getName() {
        return String.format("Incr:w=%d", weight);
    }
}
