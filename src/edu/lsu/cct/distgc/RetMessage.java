package edu.lsu.cct.distgc;

public class RetMessage extends Message {

    CID start_recovery_over;

    public RetMessage(int s, int r, CID start_recovery_over) {
        super(s, r);
        assert s != r;
        action = false;
        this.start_recovery_over = start_recovery_over;
    }

    @Override
    public void runTask(Node n) {
        n.ret(start_recovery_over);
    }

    @Override
    public String shortName() {
        return "Ret";
    }

}
