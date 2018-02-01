package edu.lsu.cct.distgc;

/**
 * This class provides basic logging for the framework. All messages are
 * tagged with file and line number.
 * @author sbrandt
 */
public class Here {

    public final static boolean VERBOSE = System.getProperty("verbose", "no").equals("yes");

    public static void log(Object o) {
        if (!VERBOSE) {
            return;
        }
        Throwable t = new Throwable();
        StackTraceElement stack = t.getStackTrace()[1];
        System.out.printf(" %s: %s%n",
                stack, o == null ? "null" : o.toString());
    }

    public static void log() {
        if (!VERBOSE) {
            return;
        }
        Throwable t = new Throwable();
        StackTraceElement stack = t.getStackTrace()[1];
        System.out.printf(" %s: %s%n",
                stack, "");
    }

    public static void bar(String msg) {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        final int width = 60;
        int m = (width - msg.length())/2;
        for(int i=0;i<m;i++) sb.append('=');
        for(int i=0;i<msg.length();i++) {
            char c = msg.charAt(i);
            if(c == ' ' || c == '\t')
                sb.append('=');
            else
                sb.append(c);
        }
        while(sb.length() < width+1)
            sb.append('=');
        sb.append(']');
        System.out.println(sb);
    }
}
