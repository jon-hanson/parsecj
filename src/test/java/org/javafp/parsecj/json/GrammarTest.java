package org.javafp.parsecj.json;

import org.javafp.parsecj.Reply;
import org.junit.Test;

public class GrammarTest {
    @Test
    public void test() throws Exception {
        final Reply<Character, Node> reply = Grammar.parse("{\"a\" : 1}");
        Node node = reply.getResult();
    }
}
