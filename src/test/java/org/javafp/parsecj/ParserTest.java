package org.javafp.parsecj;

import org.junit.*;

import static org.javafp.parsecj.Combinators.*;
import static org.javafp.parsecj.Text.dble;

public class ParserTest {

    private static final double THRESHOLD = 1e-8;

    private static final Parser<Character, Void> eof = eof();
    private static final Parser<Character, Double> dble_eof = dble.bind(d -> eof.then(retn(d)));

    private static <S, A> boolean isError(Reply<S, A> reply) {
        return reply.match(ok -> false, error -> true);
    }

    private double parseDbl(String s) throws Exception {
        return dble_eof.apply(State.of(s)).getReply().getResult();
    }

    private Reply<Character, Double> parseErrorDbl(String s) {
        return dble_eof.apply(State.of(s)).getReply();
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

        Assert.assertTrue("9e99999999", Double.isInfinite(parseDbl("9e99999999")));
        Assert.assertTrue("-9e99999999", Double.isInfinite(parseDbl("-9e99999999")));

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
        final Parser<Character, String> p =
            satisfy('a').bind(a ->
                satisfy('b').bind(b ->
                    retn("" + a + b)));

        Assert.assertTrue("'a' should fail to parse", isError(p.apply(State.of("a")).getReply()));
        Assert.assertTrue("'b' should fail to parse", isError(p.apply(State.of("b")).getReply()));
        Assert.assertEquals("'ab' should parse", "ab", p.apply(State.of("ab")).getReply().getResult());
    }

    @Test
    public void testBind2() throws Exception {
        final Parser<Character, String> p =
            satisfy('a').then(
                satisfy('b').then(
                    retn("" + "ab")));

        Assert.assertTrue("'a' should fail to parse", isError(p.apply(State.of("a")).getReply()));
        Assert.assertTrue("'b' should fail to parse", isError(p.apply(State.of("b")).getReply()));
        Assert.assertEquals("'ab' should parse", "ab", p.apply(State.of("ab")).getReply().getResult());
    }


    @Test
    public void testOr() throws Exception {
        final Parser<Character, Character> p = satisfy('a').or(satisfy('b'));

        Assert.assertEquals("'a' should parse", 'a', p.apply(State.of("a")).getReply().getResult().charValue());
        Assert.assertEquals("'b' should parse", 'b', p.apply(State.of("b")).getReply().getResult().charValue());
        Assert.assertTrue("'c' should fail to parse", isError(p.apply(State.of("c")).getReply()));
    }
}
