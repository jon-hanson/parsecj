package org.javafp.parsecj;

import org.junit.Assert;
import org.junit.Test;

import static org.javafp.parsecj.Combinators.*;
import static org.javafp.parsecj.Text.dble;
import static org.javafp.parsecj.Text.intr;

public class ParserTest {

    private static final double THRESHOLD = 1e-8;

    private static final Parser<Character, Double> eof = eof();
    private static final Parser<Character, Double> dbl_eof = dble.bind(d -> eof.then(retn(d)));

    private static <S, A> boolean isError(Reply<S, A> reply) {
        return reply.match(ok -> false, error -> true);
    }

    private double parseDbl(String s) throws Exception {
        return dbl_eof.parse(State.of(s)).getReply().getResult();
    }

    private Reply<Character, Double> parseErrorDbl(String s) throws Exception {
        return dbl_eof.parse(State.of(s)).getReply();
    }

    @Test
    public void testDouble() throws Exception {
        Assert.assertEquals(0.0, parseDbl("0"), THRESHOLD);
        Assert.assertEquals(0.0, parseDbl("0.0"), THRESHOLD);
        Assert.assertEquals(.1, parseDbl(".1"), THRESHOLD);
        Assert.assertEquals(1.0, parseDbl("1"), THRESHOLD);
        Assert.assertEquals(1.0, parseDbl("1.0"), THRESHOLD);
        Assert.assertEquals(1.2, parseDbl("1.2"), THRESHOLD);
        Assert.assertEquals(-1.2, parseDbl("-1.2"), THRESHOLD);
        Assert.assertEquals(123456789.123456789, parseDbl("123456789.123456789"), THRESHOLD);
        Assert.assertEquals(-123456789.123456789, parseDbl("-123456789.123456789"), THRESHOLD);
        Assert.assertEquals(12345.6789e12, parseDbl("12345.6789e12"), THRESHOLD);
        Assert.assertEquals(-12345.6789e12, parseDbl("-12345.6789e12"), THRESHOLD);
        Assert.assertEquals(12345.6789e-12, parseDbl("12345.6789e-12"), THRESHOLD);
        Assert.assertEquals(-12345.6789e-12, parseDbl("-12345.6789e-12"), THRESHOLD);

        Assert.assertTrue("", isError(parseErrorDbl("")));
        Assert.assertTrue("", isError(parseErrorDbl("+")));
        Assert.assertTrue("", isError(parseErrorDbl("-")));
        Assert.assertTrue("1.1.", isError(parseErrorDbl("1.1.")));
        Assert.assertTrue("+-1", isError(parseErrorDbl("+-1")));
        Assert.assertTrue("e", isError(parseErrorDbl("e")));
        Assert.assertTrue("0-0", isError(parseErrorDbl("0-0")));
        Assert.assertTrue("0+0", isError(parseErrorDbl("0+0")));
        Assert.assertTrue("+0+", isError(parseErrorDbl("+0+")));
        Assert.assertTrue("1 0", isError(parseErrorDbl("1 0")));
        Assert.assertTrue("0 1", isError(parseErrorDbl("0 1")));
    }

    @Test
    public void testBind1() throws Exception {
        int i = intr.bind(x -> satisfy('+').then(intr.bind(y -> retn(x+y)))).parse(State.of("1+2")).getReply().getResult();
        final Parser<Character, String> p =
            satisfy('a').bind(a ->
                satisfy('b').bind(b ->
                    retn("" + a + b)));

        Assert.assertTrue("'a' should fail to parse", isError(p.parse(State.of("a")).getReply()));
        Assert.assertTrue("'b' should fail to parse", isError(p.parse(State.of("b")).getReply()));
        Assert.assertEquals("'ab' should parse", "ab", p.parse(State.of("ab")).getReply().getResult());
    }

    @Test
    public void testBind2() throws Exception {
        final Parser<Character, String> p =
            satisfy('a').then(
                satisfy('b').then(
                    retn("" + "ab")));

        Assert.assertTrue("'a' should fail to parse", isError(p.parse(State.of("a")).getReply()));
        Assert.assertTrue("'b' should fail to parse", isError(p.parse(State.of("b")).getReply()));
        Assert.assertEquals("'ab' should parse", "ab", p.parse(State.of("ab")).getReply().getResult());
    }


    @Test
    public void testOr() throws Exception {
        final Parser<Character, Character> p = satisfy('a').or(satisfy('b'));

        Assert.assertEquals("'a' should parse", 'a', p.parse(State.of("a")).getReply().getResult().charValue());
        Assert.assertEquals("'b' should parse", 'b', p.parse(State.of("b")).getReply().getResult().charValue());
        Assert.assertTrue("'c' should fail to parse", isError(p.parse(State.of("c")).getReply()));
    }
}
