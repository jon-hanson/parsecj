package org.javafp.parsecj.json;

import org.javafp.parsecj.Reply;
import org.junit.Test;

public class GrammarTest {
    @Test
    public void test1() throws Exception {
        final Reply<Character, Node> reply = Grammar.parse(
            "{\"array\":[1,2,3],\"boolean\":true,\"null\":null,\"number\":123,\"object\":{\"a\":\"b\",\"c\":\"d\",\"e\":\"f\"},\"string\":\"Hello World\"}"
        );
        Node node = reply.getResult();
        System.out.println(node);
    }

    @Test
    public void test2() throws Exception {
        final Reply<Character, Node> reply = Grammar.parse(
            " { \"array\" : [ 1 , 2 , 3 ] , \"boolean\" : true , \"null\" : null , \"number\" : 123 , \"object\" : { \"a\" : \"b\" , \"c\" : \"d\" , \"e\" : \"f\" } , \"string\" : \"Hello World\" } "
        );
        Node node = reply.getResult();
        System.out.println(node);
    }
}
