package org.javafp.parsecj.expr;

import org.javafp.parsecj.Parser;
import org.javafp.parsecj.Reply;
import org.javafp.parsecj.State;

import java.util.function.BinaryOperator;

import static org.javafp.parsecj.Combinators.*;
import static org.javafp.parsecj.Text.*;
import static org.javafp.parsecj.expr.Model.*;

public abstract class Grammar {

    // To get around circular references.
    private static final Parser.Ref<Character, Expr> expr = Parser.Ref.of();

    private static final Parser<Character, Character> open = satisfy('(');
    private static final Parser<Character, Character> close = satisfy(')');
    private static final Parser<Character, Character> comma = satisfy(',');

    private static final Parser<Character, UnaryOp> plus = satisfy('+', UnaryOp.POS);
    private static final Parser<Character, UnaryOp> minus = satisfy('-', UnaryOp.NEG);

    private static final Parser<Character, BinOp> add = satisfy('+', BinOp.ADD);
    private static final Parser<Character, BinOp> sub = satisfy('-', BinOp.SUBTRACT);
    private static final Parser<Character, BinOp> mult = satisfy('*', BinOp.MULTIPLY);
    private static final Parser<Character, BinOp> div = satisfy('/', BinOp.DIVIDE);

    private static final Parser<Character, NumExpr.Units> pct = string("%").then(retn(NumExpr.Units.PCT));
    private static final Parser<Character, NumExpr.Units> bps = string("bp").then(retn(NumExpr.Units.BPS));

    // addSub = add | sub
    private static final Parser<Character, BinaryOperator<Expr>> addSub =
        add.or(sub).bind(op -> retn(op.ctor()));

    // multDiv = mult | div
    private static final Parser<Character, BinaryOperator<Expr>> multDiv =
        mult.or(div).bind(op -> retn(op.ctor()));

    // units = % | bp
    private static final Parser<Character, NumExpr.Units> units =
        choice(pct, bps, retn(NumExpr.Units.ABS));

    // num = intr
    private static final Parser<Character, Expr> num =
        dble.bind(i -> units.bind(unts -> retn(numExpr(i, unts)))).label("num");

    // brackExpr = open expr close
    private static final Parser<Character, Expr> brackExpr =
        open.then(expr).bind(exp ->
            close.then(retn(exp))
        ).label("brack-expr");

    private static class Args {
        public final Expr arg0;
        public final Expr arg1;

        private Args(Expr arg0, Expr arg1) {
            this.arg0 = arg0;
            this.arg1 = arg1;
        }
    }

    // args = open expr comma expr close
    private static final Parser<Character, Args> args =
        open.then(expr).bind(arg0 ->
            comma.then(expr.bind(arg1 ->
                close.then(
                    retn(new Args(arg0, arg1))
                )
            ))
        ).label("args");

    private static Parser<Character, Expr> var(String name) {
        return retn(varExpr(name));
    }

    // funcN = name { args | ε }
    private static final Parser<Character, Expr> funcN =
        alphaNum.bind(name ->
            args.bind(argz ->
                retn(func2Expr(name, argz.arg0, argz.arg1))
            ).or(var(name).label("var"))
        );

    // sign = + | -
    private static final Parser<Character, UnaryOp> sign = plus.or(minus);

    // signedExpr = sign expr
    private static final Parser<Character, Expr> signedExpr =
        sign.bind(op ->
            expr.bind(x ->
                retn(unaryOpExpr(op, x))));

    // term = num | brackExpr | funcN | signedExpr
    private static final Parser<Character, Expr> term =
        choice(tryP(num), brackExpr, funcN, signedExpr);

    // prod = term chainl1 multDiv
    private static final Parser<Character, Expr> prod =
        term.chainl1(multDiv);

    // expr = prod chainl1 addSub
    private static Parser<Character, Expr> expr() {
        return prod.chainl1(addSub);
    }

    static {
        expr.set(() -> expr());
    }

    // Use a variable to help the type inference.
    private static final Parser<Character, Void> end = eof();

    // parser = expr eof
    private static Parser<Character, Expr> parser() {
        return expr.bind(x -> end.then(retn(x)));
    }

    public static final Parser<Character, Expr> parser = parser();

    public static Reply<Character, Expr> parse(String str) {
        return parser.parse(State.of(str)).getReply();
    }
}
