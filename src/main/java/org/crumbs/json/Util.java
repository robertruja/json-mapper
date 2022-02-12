package org.crumbs.json;

import java.util.Arrays;

public class Util {

    public static char[] spaceRemover(char[] input) {
        boolean insideString = false;
        char[] out = new char[input.length];

        int outLength = 0;

        for (int i = 0; i < input.length; i++) {
            char current = input[i];
            // ascii for "
            if(current == 34) {
                insideString = !insideString;
            }
            if(!insideString) {
                // ascii LF         ascii CR         ascii SPC
                if(current == 10 || current == 13 || current == 32) {
                    continue;
                }
            }
            out[outLength++] = current;
        }
        return Arrays.copyOfRange(out, 0, outLength);
    }
}
