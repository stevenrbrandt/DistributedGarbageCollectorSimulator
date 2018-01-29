package edu.lsu.cct.distgc;

/**
 * This class holds variables which are only needed while a node is undergoing a
 * collection.
 *
 * @author sbrandt
 */
public class CollectionData {

    int parent;
    private CID cid;
    int phantom_count, rcc, wait_count;
    boolean mandate;
    CID start_over, recoverCid;
    CollectorState state = CollectorState.healthy_state;
    CollectorState recv = CollectorState.healthy_state;
    public boolean incrRCC;

    public void setCid(CID cid) {
        assert cid != null;
        if (this.cid == null) {
            this.cid = cid;
            this.rcc = 0;
            this.setIncrRCC();
        } else if (this.cid.equals(cid)) {
            this.cid = cid;
        } else if (this.cid.lessThan(cid)) {
            this.cid = cid;
            this.rcc = 0;
            this.setIncrRCC();
        } else {
            assert false : "" + this.cid + " <= " + cid;
        }
    }

    public CID getCid() {
        return cid;
    }

    public CollectionData(int id) {
        setCid(new CID(0, id, 0));
    }

    public CollectionData(CID cid) {
        setCid(cid);
    }

    char b(boolean bn) {
        return bn ? 'T' : 'F';
    }

    void setIncrRCC() {
        if (!incrRCC) {
            incrRCC = true;
            if (state == CollectorState.recover_state) {
                state = CollectorState.phantom_state;
            }
        }
    }

    public String toString() {
        char orig = parent == 0 ? '*' : ' ';
        String cids = cid == null ? "" : cid.toString();
        return String.format(" pc=%d rcc=%d wait=%d %s/%s parent=%d sro=%s %s%c incrRCC=%c m=%c",
                phantom_count, rcc, wait_count, recv, state, parent,
                start_over, cids, orig, b(incrRCC), b(mandate));
    }

    boolean phantom_flag() {
        return state == CollectorState.phantom_state
                || state == CollectorState.recover_state
                || state == CollectorState.infected_state;
    }
}
