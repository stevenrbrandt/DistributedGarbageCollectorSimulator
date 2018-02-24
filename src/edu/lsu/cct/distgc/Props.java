package edu.lsu.cct.distgc;

import java.util.*;
import java.util.regex.*;

class PropInfo {
    final String regex;
    final String defaultValue;
    PropInfo(String r,String d) {
        regex = r;
        defaultValue = d;
    }
    PropInfo(String r) {
        regex = r;
        defaultValue = null;
    }

}
public class Props {
    private static Map<String,PropInfo> propDefs = new HashMap<>();
    static {
        propDefs.put("CheckCounts",new PropInfo("(yes|no)","yes"));
        propDefs.put("CONGEST_mode",new PropInfo("(yes|no)","yes"));
        propDefs.put("shuffle",new PropInfo("(yes|no)","yes"));
        propDefs.put("verbose",new PropInfo("(yes|no)","no"));
        propDefs.put("test",new PropInfo("\\w+","cycle"));
        propDefs.put("fileloc",new PropInfo("\\w+","test1.txt"));
        propDefs.put("seed",new PropInfo("\\d+"));
        propDefs.put("size",new PropInfo("\\d+","2"));
        propDefs.put("adv-priority",new PropInfo("\\d+","0"));

        for(String name : System.getProperties().stringPropertyNames()) {
            if(propDefs.containsKey(name)) {
                PropInfo pi = propDefs.get(name);
                Pattern p = Pattern.compile("^"+pi.regex+"$");
                String value = System.getProperty(name,pi.defaultValue);
                Matcher m = p.matcher(value);
                if(m.matches()) {
                    System.out.printf("Property: %s=%s%n",name,value);
                } else {
                    System.err.printf("Property name %s: value '%s' does not match %s%n",name,value,pi.regex);
                    System.exit(2);
                }
            } else {
                if(name.indexOf('.') >= 0)
                    continue;
                System.err.println("Undefined property: "+name);
                System.exit(2);
            }
        }
    }
    static String get(String key) {
        PropInfo pi = propDefs.get(key);
        return System.getProperty(key,pi.defaultValue);
    }
}
