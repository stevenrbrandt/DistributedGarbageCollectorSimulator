package edu.lsu.cct.distgc;

// TODO: If a recovered node creates a link, it needs to increment the target rcc.
public class IncrMessage extends Message implements HasAdversary {

    int weight;
    boolean phantom_flag;
    CID cid;
    Adversary adv;

    public Adversary adversary() { return adv; }

    public IncrMessage(int sender, int receiver, int weight, boolean phantom_flag, CID cid,Adversary adv) {
        super(sender, receiver);
        this.weight = weight;
        this.phantom_flag = phantom_flag;
        this.cid = cid;
        this.adv = adv;
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

    @Override
    public String shortName() {
        return "Incr";
    }
}
