package edu.lsu.cct.distgc;

import java.util.ArrayList;
import java.util.List;

public class Main {

    /**
     * Create a random graph and then remove the support from root edges
     *
     * @param n a number proportional to the number of edges
     * @param m1 if one root edge should be retained
     */
    public static void RandTest(int n, boolean m1) {
        List<Root> roots = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            roots.add(new Root());
        }
        shuffle(roots);
        for (int i = 0; i < 2*n; i++) {
            int from = Message.RAND.nextInt(n);
            int to = Message.RAND.nextInt(n);
            Root rfrom = roots.get(from);
            Root rto = roots.get(to);
            rfrom.get().createEdge(rto.getId());
        }
        for (int i = 0; i < n; i++) {
            if (m1 && i + 1 == n) {
                continue;
            }
            Root r = roots.get(i);
            r.get().delete_outgoing_edges();
            r.set(null);
        }
        Message.runAll();
    }

    /**
     * Create a grid of nodes with four outgoing and incoming edges each.
     *
     * @param n2 a number proportional to the number of edges
     * @param m1 if one root edge should be retained
     */
    public static void GridTest(int n2, boolean m1) {
        final int n = (int) Math.ceil(Math.sqrt(n2));
        Here.log("n=" + n + " n2=" + n2);
        List<Root> grid = new ArrayList<>();
        outer:
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i * n + j > n2) {
                    break outer;
                }
                grid.add(new Root());
            }
        }
        shuffle(grid);
        outer:
        for (int i = 1; i < n; i++) {
            for (int j = 1; j < n; j++) {
                if (i * n + j > n2) {
                    break outer;
                }
                Root r1 = grid.get(n * i + j);
                Root r2 = grid.get(n * (i - 1) + j);
                Root r3 = grid.get(n * (i - 1) + (j - 1));
                Root r4 = grid.get(n * i + (j - 1));
                r1.get().createEdge(r2.getId());
                r2.get().createEdge(r3.getId());
                r3.get().createEdge(r4.getId());
                r4.get().createEdge(r1.getId());
            }
        }
        outer:
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i * n + j > n2) {
                    break outer;
                }
                if (m1 && i + 1 == n && j + 1 == n) {
                    continue;
                }
                grid.get(n * i + j).set(null);
            }
        }
        Message.runAll();
    }

    /**
     * Create a densely connected set of nodes (in some cases forming a clique)
     * and then remove the support from root edges
     *
     * @param n2 a number proportional to the number of edges
     * @param m1 if one root edge should be retained
     */
    public static void CliqueTest(int n2, boolean m1) {
        final int n = (int) Math.ceil(Math.sqrt(n2));
        List<Root> roots = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            roots.add(new Root());
        }
        shuffle(roots);
        int count = 0;
        outer:
        for (int off = 1; off < n; off++) {
            for (int i = 0; i < n; i++) {
                Root ri = roots.get(i);
                Root rj = roots.get((i + off) % n);
                ri.get().createEdge(rj.getId());
                count++;
                if (count == n2) {
                    break outer;
                }
            }
        }
        for (int i = 0; i < n; i++) {
            if (m1 && i + 1 == n) {
                continue;
            }
            roots.get(i).set(null);
        }
        Message.runAll();
    }

    /**
     * Create a cycle of nodes with one way connections and then remove the
     * support from root edges
     *
     * @param n a number proportional to the number of edges
     * @param m1 if one root edge should be retained
     */
    public static void CycleTest(int n, boolean m1) {
        List<Root> roots = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            roots.add(new Root());
        }
        shuffle(roots);
        for (int i = 0; i < n; i++) {
            int next = (i + 1) % n;
            Root r1 = roots.get(i);
            Root r2 = roots.get(next);
            r1.get().createEdge(r2.getId());
        }
        for (int i = 0; i < n; i++) {
            if (m1 && i + 1 == n) {
                break;
            }
            roots.get(i).set(null);
        }
        Message.runAll();
    }

    /**
     * Create a cycle of nodes with one way connections and then remove the
     * support from root edges
     *
     * @param n a number proportional to the number of edges
     * @param m1 if one root edge should be retained
     */
    public static void DoubleCycleTest(int n, boolean m1) {
        List<Root> roots = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            roots.add(new Root());
        }
        shuffle(roots);
        for (int i = 0; i < n; i++) {
            int next = (i + 1) % n;
            Root r1 = roots.get(i);
            Root r2 = roots.get(next);
            r1.get().createEdge(r2.getId());
            int prev = (i + n - 1) % n;
            Root r3 = roots.get(prev);
            r1.get().createEdge(r3.getId());
        }
        for (int i = 0; i < n; i++) {
            if (m1 && i + 1 == n) {
                break;
            }
            roots.get(i).set(null);
        }
        Message.runAll();
    }

    static void shuffle(List<Root> roots) {
        if (System.getProperty("shuffle", "yes").equalsIgnoreCase("yes")) {
            assert false;
            for (int i = roots.size() - 1; i > 0; i--) {
                int n = Message.RAND.nextInt(i + 1);
                Root r1 = roots.get(i);
                Root r2 = roots.get(n);
                roots.set(i, r2);
                roots.set(n, r1);
            }
        }
    }

    /**
     * Create a doubly-linked list of nodes with one way connections and then
     * remove the support from root edges
     *
     * @param n a number proportional to the number of edges
     * @param m1 if one root edge should be retained
     */
    public static void DoublyLinkedList(int n, boolean m1) {
        List<Root> roots = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            Root r = new Root();
            roots.add(r);
        }
        shuffle(roots);
        for (int i = 0; i < n; i++) {
            Root r = roots.get(i);
            if (i + 1 < n) {
                Root rnext = roots.get(i + 1);
                r.get().createEdge(rnext.getId());
            }
            if (i - 1 >= 0) {
                Root rprev = roots.get(i - 1);
                r.get().createEdge(rprev.getId());
            }
        }
        for (int i = 0; i < n; i++) {
            if (m1 && i + 1 == n) {
                break;
            }
            roots.get(i).set(null);
        }
        Message.runAll();
    }

    public static void main(String[] args) {
        try {
            // Require that assertions are enabled
            assert false;
            throw new Error("Enable assertions");
        } catch (AssertionError ae) {
        }
        if (System.getProperty("CONGEST_mode", "no").toLowerCase().equals("yes")) {
            Message.CONGEST_mode = true;
        }
        String test = System.getProperty("test", "cycle");
        String size = System.getProperty("size", "2");
        String fileloc = System.getProperty("fileloc", "file-input-format.txt");
        if (test.equals("file-input")) {
            System.out.println("fileloc=" + fileloc);
        }
        System.out.println("test=" + test);
        System.out.println("size=" + size);
        int sizeI = Integer.parseInt(size);
        if (test.equals("file-input")) {
            System.out.println("File Input is chosen.");
            System.out.println("Input file location: " + fileloc);
            SimulationExecutor sim = new SimulationExecutor(fileloc);
        } else if (test.equals("cycle")) {
            CycleTest(sizeI, false);
        } else if (test.equals("cyclem1")) {
            CycleTest(sizeI, true);
        } else if (test.equals("dcycle")) {
            DoubleCycleTest(sizeI, false);
        } else if (test.equals("dcyclem1")) {
            DoubleCycleTest(sizeI, true);
        } else if (test.equals("dlink")) {
            DoublyLinkedList(sizeI, false);
        } else if (test.equals("dlinkm1")) {
            DoublyLinkedList(sizeI, true);
        } else if (test.equals("clique")) {
            CliqueTest(sizeI, false);
        } else if (test.equals("cliquem1")) {
            CliqueTest(sizeI, true);
        } else if (test.equals("grid")) {
            GridTest(sizeI, false);
        } else if (test.equals("gridm1")) {
            GridTest(sizeI, true);
        } else if (test.equals("rand")) {
            RandTest(sizeI, false);
        } else if (test.equals("randm1")) {
            RandTest(sizeI, true);
        } else {
            assert false;
        }
    }
}
