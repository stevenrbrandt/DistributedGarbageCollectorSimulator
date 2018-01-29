package edu.lsu.cct.distgc;

public class BuildMessage extends Message implements CidMessage {

    CID cid;
    int w;
    boolean incrRCC, mandate, decrRCC;

    public CID getCid() {
        return cid;
    }

    public BuildMessage(int id, Integer edge, int w, CID cid, boolean incrRCC, boolean mandate, boolean decrRCC) {
        super(id, edge);
        this.cid = cid;
        this.w = w;
        this.incrRCC = incrRCC;
        this.mandate = mandate;
        this.decrRCC = decrRCC;
    }

    @Override
    public void runTask(Node n) {
        n.build(sender, w, cid, incrRCC, mandate, decrRCC);
    }

    @Override
    public String getName() {
        return String.format("Build:w=%d", w);
    }
}
