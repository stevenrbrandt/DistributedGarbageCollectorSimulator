package edu.lsu.cct.distgc;

public class DecrMessage extends Message {

    int weight;
    boolean phantom_flag;
    int sender;
    CID cid;

    public DecrMessage(int sender, int receiver, int weight, boolean phantom_flag, CID cid) {
        super(sender, receiver);
        this.weight = weight;
        this.phantom_flag = phantom_flag;
        this.cid = cid;
        this.sender = sender;
        action = false;
    }

    @Override
    public void runTask(Node n) {
        n.decr(sender, weight, phantom_flag, cid);
    }

    @Override
    public String getName() {
        return String.format("Decr:w=%d", weight);
    }

    @Override
    public String shortName() {
        return "Decr";
    }
}
