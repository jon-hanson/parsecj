package org.javafp.parsecj.expr;

import org.javafp.parsecj.*;
import org.junit.Test;

import java.util.function.BinaryOperator;

import static org.javafp.parsecj.Combinators.*;
import static org.javafp.parsecj.Text.*;

public class Grammar {
    // Forward declare expr to allow for circular references.
    private static final Parser.Ref<Character, Double> expr = Parser.ref();

    // Inform the compiler of the type of retn.
    private static final Parser<Character, BinaryOperator<Double>> add = retn((l, r) -> l + r);
    private static final Parser<Character, BinaryOperator<Double>> subt = retn((l, r) -> l - r);
    private static final Parser<Character, BinaryOperator<Double>> times = retn((l, r) -> l * r);
    private static final Parser<Character, BinaryOperator<Double>> divide = retn((l, r) -> l / r);

    // bin-op ::= '+' | '-' | '*' | '/'
    private static final Parser<Character, BinaryOperator<Double>> binOp =
        choice(
            chr('+').then(add),
            chr('-').then(subt),
            chr('*').then(times),
            chr('/').then(divide)
        );

    // bin-expr ::= '(' expr bin-op expr ')'
    private static final Parser<Character, Double> binOpExpr =
        chr('(')
            .then(expr.bind(
                l -> binOp.bind(
                    op -> expr.bind(
                        r -> chr(')')
                            .then(retn(op.apply(l, r)))))));

    static {
        // expr ::= dble | binOpExpr
        expr.set(choice(dble, binOpExpr));
    }

    // Inform the compiler of the type of eof.
    private static final Parser<Character, Void> eof = eof();

    // parser = expr end
    private static final Parser<Character, Double> parser = expr.bind(d -> eof.then(retn(d)));

    private static void evaluate(String s) throws Exception {
        System.out.println(s + " = " + parser.parse(State.of(s)).getResult());
    }

    @Test
    public void test() throws Exception {
        evaluate("1.0");
        evaluate("(1+2.2)");
        evaluate("((2.2+5.3)*3.56)");
        evaluate("(2.1+(5.2*3.2))");
        evaluate("((2.9+5.99)*(3/7.123))");
    }
}
