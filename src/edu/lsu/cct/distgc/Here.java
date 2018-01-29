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
}
