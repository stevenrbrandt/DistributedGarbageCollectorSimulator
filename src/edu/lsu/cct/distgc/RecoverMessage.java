package edu.lsu.cct.distgc;

public class RecoverMessage extends Message implements CidMessage {

    CID cid;
    boolean incrRCC;

    public CID getCid() {
        return cid;
    }

    public RecoverMessage(int s, int r, CID cid, boolean incrRCC) {
        super(s, r);
        this.cid = cid;
        this.incrRCC = incrRCC;
    }

    @Override
    public void runTask(Node n) {
        n.recover(cid, sender, incrRCC);
    }

}
