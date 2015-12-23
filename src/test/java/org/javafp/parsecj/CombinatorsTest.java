package org.javafp.parsecj;

import org.javafp.data.*;
import org.junit.*;

import java.util.Optional;
import java.util.function.BinaryOperator;

import static org.javafp.parsecj.Combinators.*;
import static org.javafp.parsecj.Text.*;
import static org.javafp.parsecj.TestUtils.*;

public class CombinatorsTest {

    @Test
    public void testBind_A() throws Exception {
        final Parser<Character, String> p =
            satisfy('a').bind(a ->
                satisfy('b').bind(b ->
                    retn("" + a + b)));

        assertParserFails(p, "a");
        assertParserFails(p, "b");
        assertParserSucceedsWithValue(p, "ab");
    }

    @Test
    public void testBind_B() throws Exception {
        final Parser<Character, String> p =
            satisfy('a').then(
                satisfy('b').then(
                    retn("" + "ab")));

        assertParserFails(p, "a");
        assertParserFails(p, "b");
        assertParserSucceedsWithValue(p, "ab");
    }

    @Test
    public void testSatisfy_A() throws Exception {
        final Parser<Character, Character> p = satisfy('a');
        assertParserFails(p, "ba");
        assertParserSucceedsWithValue(p, "ab", 'a');
    }

    @Test
    public void testSatisfy_B() throws Exception {
        final Parser<Character, Character> p = satisfy(Character::isDigit);
        assertParserFails(p, "ba");
        assertParserSucceedsWithValue(p, "9a", '9');
    }

    @Test
    public void testSatisfy_C() throws Exception {
        final Parser<Character, Character> p = satisfy('a', 'c');
        assertParserFails(p, "c");
        assertParserSucceedsWithValue(p, "ab", 'c');
    }

    @Test
    public void testOr() throws Exception {
        final Parser<Character, Character> p = satisfy('a').or(satisfy('b'));
        assertParserFails(p, "c");
        assertParserSucceedsWithValue(p, "a", 'a');
        assertParserSucceedsWithValue(p, "b", 'b');
    }

    @Test
    public void testLabel() throws Exception {
        final String unlikelyName = "6b%gfb$nj";
        final Parser<Character, Character> p = satisfy('A').label(unlikelyName);
        final String msg = p.parse(State.of("FAIL")).getMsg();
        Assert.assertTrue("Parse error contains label", msg.contains(unlikelyName));
    }

    @Test
    public void testAttempt() throws Exception {
        final Parser<Character, String> p = string("abcde");
        Assert.assertTrue("parse of 'abcde' should consume input", p.apply(State.of("abcde")).isConsumed());
        Assert.assertTrue("parse of 'abcd' should consume input", p.apply(State.of("abcd")).isConsumed());
        Assert.assertFalse("attempt parse of 'abcd' will not consume input", p.attempt().apply(State.of("abcd")).isConsumed());
    }

    @Test
    public void testChoice() throws Exception {
        final Parser<Character, String> p = choice(string("ab"), string("cd"), string("ef"));
        assertParserFails(p, "ac");
        assertParserSucceedsWithValue(p, "ab");
        assertParserSucceedsWithValue(p, "cd");
        assertParserSucceedsWithValue(p, "ef");
    }

    @Test
    public void testOption() throws Exception {
        final Parser<Character, String> p = string("ab").option("default");
        assertParserSucceedsWithValue(p, "ab");
        assertParserSucceedsWithValue(p, "cd", "default");
    }

    @Test
    public void testOptionalOpt() throws Exception {
        final Parser<Character, Optional<String>> p = string("ab").optionalOpt();
        assertParserSucceedsWithValue(p, "ab", Optional.of("ab"));
        assertParserSucceedsWithValue(p, "cd", Optional.empty());
    }

    @Test
    public void testOptional() throws Exception {
        final Parser<Character, Unit> p = string("ab").optional();
        assertParserSucceeds(p, "ab");
        assertParserSucceeds(p, "cd");
    }

    @Test
    public void testBetween() throws Exception {
        final Parser<Character, String> p = string("ab").between(chr('['), chr(']'));
        assertParserFails(p, "ab");
        assertParserFails(p, "[");
        assertParserSucceedsWithValue(p, "[ab]", "ab");
    }

    @Test
    public void testMany() throws Exception {
        final Parser<Character, IList<String>> p = chr('.').then(string("ab")).many();
        assertParserSucceedsWithValue(p, "x", IList.of());
        assertParserFails(p, ".a");
        assertParserSucceedsWithValue(p, ".ab.ab.ab", IList.of("ab", "ab", "ab"));

        final StringBuilder sb = new StringBuilder();
        IList<String> exp = IList.of();
        for (int i = 0; i < 10000; ++i) {
            sb.append(".ab");
            exp = exp.add("ab");
        }
        assertParserSucceedsWithValue(p, sb.toString(), exp);

        assertParserSucceedsWithValue(digit.many(), "0123", IList.of('0', '1', '2', '3'));
    }

    @Test
    public void testMany1() throws Exception {
        final Parser<Character, IList<Character>> p = digit.many1();
        assertParserFails(p, "a");
        assertParserSucceedsWithValue(p, "0", IList.of('0'));
        assertParserSucceedsWithValue(p, "0123", IList.of('0', '1', '2', '3'));
    }

    @Test
    public void testSkipMany() throws Exception {
        final Parser<Character, String> p = digit.skipMany().then(string("ab"));
        assertParserFails(p, "x");
        assertParserSucceedsWithValue(p, "ab");
        assertParserSucceedsWithValue(p, "01234ab", "ab");
    }

    @Test
    public void testSkipMany1() throws Exception {
        final Parser<Character, String> p = digit.skipMany1().then(string("ab"));
        assertParserFails(p, "x");
        assertParserFails(p, "ab");
        assertParserSucceedsWithValue(p, "01234ab", "ab");
    }

    @Test
    public void testSepBy() throws Exception {
        final Parser<Character, IList<Character>> p = digit.sepBy(chr(','));
        assertParserSucceeds(p, "a");
        assertParserSucceedsWithValue(p, "0,1,2,3", IList.of('0', '1', '2', '3'));
    }

    @Test
    public void testSepBy1() throws Exception {
        final Parser<Character, IList<Character>> p = digit.sepBy1(chr(','));
        assertParserFails(p, "a");
        assertParserFails(p, ",1");
        assertParserSucceedsWithValue(p, "0,1,2,3", IList.of('0', '1', '2', '3'));
    }

    @Test
    public void testSepEndBy() throws Exception {
        final Parser<Character, IList<Character>> p = digit.sepEndBy(chr(';'));
        assertParserSucceeds(p, "a");
        assertParserSucceedsWithValue(p, ";", IList.of());
        assertParserSucceedsWithValue(p, "0;1;2;3", IList.of('0', '1', '2', '3'));
        assertParserSucceedsWithValue(p, "0;1;2;3;", IList.of('0', '1', '2', '3'));
    }

    @Test
    public void testSepEndBy1() throws Exception {
        final Parser<Character, IList<Character>> p = digit.sepEndBy1(chr(';'));
        assertParserFails(p, "a");
        assertParserFails(p, ";");
        assertParserSucceedsWithValue(p, "0;1;2;3", IList.of('0', '1', '2', '3'));
        assertParserSucceedsWithValue(p, "0;1;2;3;", IList.of('0', '1', '2', '3'));
    }

    @Test
    public void testEndBy() throws Exception {
        final Parser<Character, IList<Character>> p = digit.endBy(chr(';'));
        assertParserSucceeds(p, "a");
        assertParserSucceedsWithValue(p, ";", IList.of());
        assertParserFails(p, "0;1;2;3");
        assertParserSucceedsWithValue(p, "0;1;2;3;", IList.of('0', '1', '2', '3'));
    }

    @Test
    public void testEndBy1() throws Exception {
        final Parser<Character, IList<Character>> p = digit.endBy1(chr(';'));
        assertParserFails(p, "a");
        assertParserFails(p, ";");
        assertParserFails(p, "0;1;2;3");
        assertParserSucceedsWithValue(p, "0;1;2;3;", IList.of('0', '1', '2', '3'));
    }

    @Test
    public void testCount() throws Exception {
        final Parser<Character, IList<Character>> p = digit.count(3);
        assertParserFails(p, "");
        assertParserFails(p, "0");
        assertParserFails(p, "01");
        assertParserSucceedsWithValue(p, "012", IList.of('0', '1', '2'));
    }

    @Test
    public void testChainr() throws Exception {
        final Parser<Character, BinaryOperator<Integer>> op =
            chr('>').then(Combinators.<Character, BinaryOperator<Integer>>retn((x, y) -> x - y));
        final Parser<Character, Integer> p = digit.bind(c -> retn(Character.getNumericValue(c))).chainr(op, 0);
        assertParserSucceedsWithValue(p, "", 0);
        assertParserSucceedsWithValue(p, "x", 0);
        assertParserSucceedsWithValue(p, "3>2>1", 3 - (2 - 1));
    }

    @Test
    public void testChainr1() throws Exception {
        final Parser<Character, BinaryOperator<Integer>> op =
            chr('>').then(Combinators.<Character, BinaryOperator<Integer>>retn((x, y) -> x - y));
        final Parser<Character, Integer> p = digit.bind(c -> retn(Character.getNumericValue(c))).chainr1(op);
        assertParserFails(p, "a");
        assertParserFails(p, ">");
        assertParserFails(p, "1>");
        assertParserFails(p, ">2");
        assertParserSucceedsWithValue(p, "3>2>1", 3 - (2 - 1));
    }

    @Test
    public void testChainl() throws Exception {
        final Parser<Character, BinaryOperator<Integer>> op =
            chr('>').then(Combinators.<Character, BinaryOperator<Integer>>retn((x, y) -> x - y));
        final Parser<Character, Integer> p = digit.bind(c -> retn(Character.getNumericValue(c))).chainl(op, 0);
        assertParserSucceedsWithValue(p, "", 0);
        assertParserSucceedsWithValue(p, "x", 0);
        assertParserSucceedsWithValue(p, "3>2>1", (3 - 2) - 1);
    }

    @Test
    public void testChainl1() throws Exception {
        final Parser<Character, BinaryOperator<Integer>> op =
            chr('>').then(Combinators.<Character, BinaryOperator<Integer>>retn((x, y) -> x - y));
        final Parser<Character, Integer> p = digit.bind(c -> retn(Character.getNumericValue(c))).chainl1(op);
        assertParserFails(p, "a");
        assertParserFails(p, "+");
        assertParserFails(p, "1>");
        assertParserFails(p, ">2");
        assertParserSucceedsWithValue(p, "3>2>1", (3 - 2) - 1);
    }
}
