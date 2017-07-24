package org.javafp.parsecj.expr2;

import org.junit.*;

public class GrammarTest {

    private static void assertSuccess(String input, String expected) throws Exception {
        final String result = Grammar.parse(input).getResult().toString();
        Assert.assertEquals(expected, result);
        Assert.assertEquals(expected, Grammar.parse(result).getResult().toString());
    }

    private static void assertFailure(String input, String errorMsg) {
        Grammar.parse(input).match(
            ok -> {
                throw new RuntimeException("Expected parse to fail");
            },
            error -> {
                Assert.assertEquals(errorMsg, error.getMsg());
                return null;
            }
        );
    }

    @Test
    public void testSuccess() throws Exception {
        assertSuccess("3*-max(4%+(5bp+-x),-2bp)-1", "(3.0*-((max((4.0%+(5.0bp+-(x))),-2.0bp)-1.0)))");
    }

    @Test
    public void testFailure() throws Exception {
        assertFailure("3*-max(4%+(5bp+),-2bp)-1", "Unexpected ')' at position 15. Expecting one of [alphaNum,brack-expr,+,-,num]");
    }
}
