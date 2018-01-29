package edu.lsu.cct.distgc;

/**
 * Do we need the majorId? It could be that the minorId is good enough.
 *
 * @author sbrandt
 *
 */
public class CID {

    final int majorId;
    final int objId;
    final int minorId;

    public CID(int mj, int ob, int mn) {
        majorId = mj;
        objId = ob;
        minorId = mn;
    }

    public boolean lessThan(CID cid) {
        if (cid == null) {
            return false;
        }
        int diff = cid.majorId - majorId;
        if (diff == 0) {
            diff = cid.objId - objId;
        }
        if (diff == 0) {
            diff = cid.minorId - minorId;
        }
        return diff > 0;
    }

    public static boolean lessThan(CID c1, CID c2) {
        if (c1 == c2) {
            return false;
        }
        if (c1 == null) {
            return false;
        }
        return c1.lessThan(c2);
    }

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        CID c = (CID) o;
        return majorId == c.majorId && objId == c.objId && minorId == c.minorId;
    }

    public static boolean equals(CID c1, CID c2) {
        if (c1 == c2) {
            return true;
        }
        if (c1 == null || c2 == null) {
            return false;
        }
        return c1.equals(c2);
    }

    public String toString() {
        return String.format("[%d,%d,%d]", majorId, objId, minorId);
    }

    public boolean lessThanEq(CID cid) {
        return lessThan(cid) || equals(cid);
    }

    public boolean greaterThan(CID cid) {
        return cid.lessThan(this);
    }
}
