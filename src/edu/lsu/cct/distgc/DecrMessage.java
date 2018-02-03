package edu.lsu.cct.distgc;

public class DecrMessage extends Message implements HasAdversary {

    int weight;
    boolean phantom_flag;
    int sender;
    CID cid;
    Adversary adv;

    public Adversary adversary() { return adv; }

    public DecrMessage(int sender, int receiver, int weight, boolean phantom_flag, CID cid,Adversary adv) {
        super(sender, receiver);
        this.weight = weight;
        this.phantom_flag = phantom_flag;
        this.cid = cid;
        this.sender = sender;
        this.adv = adv;
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
