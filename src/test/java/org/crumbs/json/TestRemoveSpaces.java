package org.crumbs.json;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestRemoveSpaces {

    @Test
    public void shouldRemoveSpacesOutsideStrings() {
        String input = "{ \"aaaa    \": \"a a a a\"       ,           \"bb b\":        23232}";

        String out = new String(Util.spaceRemover(input.toCharArray()));

        assertEquals("{\"aaaa    \":\"a a a a\",\"bb b\":23232}", out);
    }

    @Test
    public void shouldRemoveSpacesAndNewLine() {
        String input = FileUtil.readResource("testInput4.json");

        String out = new String(Util.spaceRemover(input.toCharArray()));
        System.out.println(out);
        assertEquals("{\"test\":\"test test\",\"al  doi\":222,\"alta key\":443}", out);
    }
}
