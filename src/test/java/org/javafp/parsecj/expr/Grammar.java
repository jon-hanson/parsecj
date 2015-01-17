package org.javafp.parsecj.expr;

import org.javafp.parsecj.*;
import org.junit.Test;

import java.util.function.BinaryOperator;

import static org.javafp.parsecj.Combinators.*;
import static org.javafp.parsecj.Text.chr;
import static org.javafp.parsecj.Text.dble;

public class Grammar {
    // Forward declare expr to allow for circular references.
    private static final Parser.Ref<Character, Double> expr = Parser.Ref.of();

    // bin-op ::= '+' | '-' | '*' | '/'
    private static final Parser<Character, BinaryOperator<Double>> binOp =
        choice(
            chr('+').then(Combinators.<Character, BinaryOperator<Double>>retn((l, r) -> l + r)),
            chr('-').then(Combinators.<Character, BinaryOperator<Double>>retn((l, r) -> l - r)),
            chr('*').then(Combinators.<Character, BinaryOperator<Double>>retn((l, r) -> l * r)),
            chr('/').then(Combinators.<Character, BinaryOperator<Double>>retn((l, r) -> l / r))
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

    private static final Parser<Character, Void> end = eof();
    private static final Parser<Character, Double> parser = expr.bind(d -> end.then(retn(d)));

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
