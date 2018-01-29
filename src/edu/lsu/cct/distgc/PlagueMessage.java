package edu.lsu.cct.distgc;

public class PlagueMessage extends Message {

    CID cid;

    public PlagueMessage(int id, int edge, CID cid) {
        super(id, edge);
        this.cid = cid;
        action = false;
    }

    @Override
    public void runTask(Node n) {
        n.plague(sender, cid, true);
    }

}
