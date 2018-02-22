package edu.lsu.cct.distgc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class RandomSeq {

    Random rand;
    int seed;
    int next;
    final boolean useFiles = false;
    List<Integer> values = new ArrayList<>();

    public RandomSeq() {
        setSeed(new Random().nextInt(1000000));
    }

    public void setSeed(int seed) {
        this.seed = seed;
        next = 0;
        rand = new Random(seed);
        if (useFiles) {
            values.clear();
            String fname = "rand" + seed + ".txt";
            File f = new File(fname);
            if (f.isFile() && f.canRead()) {
                try {
                    Scanner s = new Scanner(f);
                    while (s.hasNext()) {
                        values.add(s.nextInt());
                        rand.nextInt();
                    }
                    s.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public int nextInt() {
        if(useFiles) {
            if (next >= values.size()) {
                for (int i = 0; i < 100; i++) {
                    values.add(rand.nextInt());
                }
                String fname = "rand" + seed + ".txt";
                try {
                    PrintWriter pw = new PrintWriter(new File(fname));
                    for (int value : values) {
                        pw.println(value);
                    }
                    pw.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
            return values.get(next++);
        } else {
            return rand.nextInt();
        }
    }

    public int nextInt(int max) {
        int val = nextInt();
        if (val < 0) {
            val = -val;
        }
        return (int) (val % max);
    }

    public int nextInt(int min, int max) {
        int val = nextInt(max - min);
        if (val < 0) {
            val = -val;
        }
        return (int) (val % max) + min;
    }
}
