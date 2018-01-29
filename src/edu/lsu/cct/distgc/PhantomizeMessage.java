package edu.lsu.cct.distgc;

public class PhantomizeMessage extends Message implements CidMessage {

    int weight;
    CID cid;

    public CID getCid() {
        return cid;
    }

    public PhantomizeMessage(int id, int edge, int weight, CID cid) {
        super(id, edge);
        this.weight = weight;
        this.cid = cid;
    }

    @Override
    public void runTask(Node n) {
        n.phantomize(sender, weight, cid);
    }

    @Override
    public String getName() {
        return String.format("Phan:w=%d", weight);
    }
}
