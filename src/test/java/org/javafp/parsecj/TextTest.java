package org.javafp.parsecj;

import org.junit.*;

import static org.javafp.parsecj.Combinators.eof;
import static org.javafp.parsecj.Combinators.retn;
import static org.javafp.parsecj.Text.dble;

public class TextTest {

    private static final double THRESHOLD = 1e-8;

    private static final Parser<Character, Void> eof = eof();
    private static final Parser<Character, Double> dble_eof = dble.bind(d -> eof.then(retn(d)));

    private static <S, A> boolean isError(Reply<S, A> reply) {
        return reply.match(ok -> false, error -> true);
    }

    private double parseDbl(String s) throws Exception {
        return dble_eof.apply(State.state(s)).getReply().getResult();
    }

    private Reply<Character, Double> parseErrorDbl(String s) {
        return dble_eof.apply(State.state(s)).getReply();
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
}
