package edu.lsu.cct.distgc;

import java.awt.Container;
import java.awt.Graphics;
import javax.swing.JComponent;
import javax.swing.JFrame;
import java.awt.Dimension;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Gui {
    
    static class MessageHolder {
        Message m;
    }

    public static void main(String[] args) throws Exception {
        final JFrame jf = new JFrame("Distributed GC GUI");
        Container c = jf.getContentPane();
        c.setPreferredSize(new Dimension(800,600));
        final MessageHolder mh = new MessageHolder();
        c.add(new JComponent() {
            @Override
            public void paint(Graphics g) {
                if(mh.m != null) {
                    g.drawString(mh.m.toString(),0,100);
                }
            }
        });
        jf.pack();
        jf.setVisible(true);
        
        Message.addListener(new MessageListener() {
            boolean ready = true;
            
            @Override
            public void before(Message m) {
            }

            @Override
            public void after(Message m) {
                mh.m = m;
                jf.repaint();
                
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ex) {
                }
                
                ready = true;
            }

            @Override
            public boolean ready() {
                boolean r = ready;
                ready = false;
                return r;
            }
        });
        
        System.setProperty("CONGEST_mode", "yes");
        System.setProperty("test","cycle");
        System.setProperty("size", "4");
        System.setProperty("verbose","yes");
        
        Main.main(new String[0]);
    }
}
