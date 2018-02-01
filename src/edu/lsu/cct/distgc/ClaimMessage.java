package edu.lsu.cct.distgc;

public class ClaimMessage extends Message implements CidMessage {

    CID cid;

    public CID getCid() {
        return cid;
    }

    public ClaimMessage(int id, int edge, CID cid) {
        super(id, edge);
        this.cid = cid;
    }

    @Override
    public void runTask(Node n) {
        n.claim(sender, cid);
    }

    @Override
    public String getName() {
        return String.format("Claim");
    }

    @Override
    public String shortName() {
        return "Claim";
    }
}
