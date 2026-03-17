package com.luxof.remilia.cursed;

import java.util.ArrayList;
import java.util.List;

/** <h1>𝑰 can do 𝑨𝑵𝒀𝑻𝑯𝑰𝑵𝑮!</h1>
 * <p>patchouli's regex but it handles nested parens.
 * <p>specialized because i'm not going to implement regex. */
public class JevilsMatcher {
    public List<Integer> starts = new ArrayList<>();
    public List<Integer> ends = new ArrayList<>();
    public String operatingOn;

    public JevilsMatcher(String over) {
        operatingOn = over;
        int nest = 0;
        int start = 0;
        String buffer = "";
        ends.add(0);

        char[] chars = over.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char chr = chars[i];

            if (chr == '$') {
                if (buffer.equals("")) {
                    start = i;
                    buffer += "$";
                } else if (buffer.startsWith("$(")) {
                    buffer += "$";
                } else {
                    buffer = "";
                }

            } else if (chr == '(') {
                if (buffer.equals("$")) {
                    buffer += "(";
                } else if (buffer.startsWith("$(")) {
                    nest += 1;
                    buffer += "(";
                }

            } else if (chr == ')' && buffer.startsWith("$(")) {
                if (nest == 0) {
                    starts.add(start);
                    ends.add(i + 1);
                    buffer = "";
                } else {
                    nest -= 1;
                    buffer += ")";
                }

            } else {
                if (buffer.startsWith("$(")) {
                    buffer += chr;
                } else if (buffer.equals("$")) {
                    buffer = "";
                }
            }
        }
    }

    public String group(int group) {
        String ret = operatingOn.substring(
            starts.get(group-1) + 2, // because i don't want $( in my match
            ends.get(group) - 1 // and i don't want ) in it either
        );
        starts.remove(0);
        ends.remove(0);
        return ret;
    }

    public boolean find() { return starts.size() > 0; }

    public void getUpToNext(StringBuilder sb) {
        sb.append(operatingOn.substring(ends.get(0), starts.get(0)));
    }

    public StringBuffer getRest(StringBuffer sb) {
        sb.append(operatingOn.substring(ends.get(0)));
        return sb;
    }
}
