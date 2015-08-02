package org.javafp.parsecj.limits;

import com.nomura.rcit.core.limits.Filter.*;
import com.nomura.rcit.core.limits.ImmutableFilter.*;
import org.javafp.data.IList;
import org.javafp.parsecj.*;

import java.util.function.BinaryOperator;

import static org.javafp.parsecj.Combinators.*;
import static org.javafp.parsecj.Text.*;

public class Grammar {

    private static Parser<Character, String> token(String tok) {
        return attempt(string(tok));
    }

    // To get around circular references.
    private static final Parser.Ref<Character, Filter.BoolExpr> boolExpr = Parser.ref();
    private static final Parser.Ref<Character, Filter.NumExpr> numExpr = Parser.ref();

    // Terminal symbols.

    private static final Parser<Character, Character> open = chr('(');
    private static final Parser<Character, Character> close = chr(')');
    private static final Parser<Character, Character> comma = chr(',');
    private static final Parser<Character, Character> dblQt = chr('"');

    private static final Parser<Character, Character> openS = chr('[');
    private static final Parser<Character, Character> closeS = chr(']');

    private static final Parser<Character, Character> openA = chr('{');
    private static final Parser<Character, Character> closeA = chr('}');

    private static final Parser<Character, UnaryOp> plus = satisfy('+', UnaryOp.POS);
    private static final Parser<Character, UnaryOp> minus = satisfy('-', UnaryOp.NEG);

    private static final Parser<Character, BinaryOp> add = satisfy('+', BinaryOp.ADD);
    private static final Parser<Character, BinaryOp> sub = satisfy('-', BinaryOp.SUBTRACT);
    private static final Parser<Character, BinaryOp> mult = satisfy('*', BinaryOp.MULTIPLY);
    private static final Parser<Character, BinaryOp> div = satisfy('/', BinaryOp.DIVIDE);

    private static final Parser<Character, EqualityOp> neq = token("<>").then(retn(EqualityOp.NEQ));
    private static final Parser<Character, EqualityOp> lte = token("<=").then(retn(EqualityOp.LTE));
    private static final Parser<Character, EqualityOp> gte = token(">=").then(retn(EqualityOp.GTE));
    private static final Parser<Character, EqualityOp> eq = satisfy('=', EqualityOp.EQ);
    private static final Parser<Character, EqualityOp> lt = satisfy('<', EqualityOp.LT);
    private static final Parser<Character, EqualityOp> gt = satisfy('>', EqualityOp.GT);

    private static final Parser<Character, EqualityOp> eqOp =
        choice(neq, lte, gte, eq, lt, gt).label("equality-op");

    private static BinaryOperator<NumExpr> binOpCtor(BinaryOp op) {
        return (lhs, rhs) -> BinaryOpExprImpl.of(op, lhs, rhs);
    }

    // addSub = add | sub
    private static final Parser<Character, BinaryOperator<NumExpr>> addSub =
        add.or(sub).bind(op -> retn(binOpCtor(op)));

    // multDiv = mult | div
    private static final Parser<Character, BinaryOperator<NumExpr>> multDiv =
        mult.or(div).bind(op -> retn(binOpCtor(op)));

    // intExpr = <integer>
//    private static final Parser<Character, IntVal> intVal =
//        intr.bind(i -> retn(IntValImpl.of(i)));

    // dblExpr = <double>
    private static final Parser<Character, DblVal> dblVal =
        dble.bind(d -> retn(DblValImpl.of(d)));

    // numExpr = dblExpr | intExpr
    private static final Parser<Character, NumVal> numVal =
        dblVal.bind(dv ->
            retn((NumVal)dv)
        );
//        dblVal.bind(de -> retn((NumVal) de))
//            .or(intVal.bind(ie -> retn((NumVal) ie)));

    // dimExpr = dblQt <string> dblQt
    private static final Parser<Character, Measure> measure =
        strBetween(openA, closeA).bind(s ->
            retn((Measure) MeasureImpl.of(s))
        ).label("measure");

    // brackNumExpr = open numExpr close
    private static final Parser<Character, NumExpr> brackNumExpr =
        attempt(
            open.then(numExpr).bind(exp ->
                close.then(retn(exp))
            )
        ).label("brack-num-expr");

    // sign = plus | minus
    private static final Parser<Character, UnaryOp> sign = plus.or(minus);

    // signedExpr = sign expr
    private static final Parser<Character, NumExpr> signedExpr =
        sign.bind(op ->
                numExpr.bind(x ->
                        retn((NumExpr) UnaryOpExprImpl.of(op, x))
                )
        ).label("signed-expr");

    // ifNumExpr = "if" open boolExpr comma numExpr comma numExpr close
    private static final Parser<Character, IfNumExpr> ifNumExpr =
        attempt(string("IF")).then(
            open.then(
                boolExpr.bind(cond ->
                    comma.then(
                        numExpr.bind(tne ->
                            comma.then(
                                numExpr.bind(fne ->
                                    close.then(
                                        retn((IfNumExpr) IfNumExprImpl.of(cond, tne, fne))
                                    )
                                )
                            )
                        )
                    )
                )
            )
        ).label("if-expr");

    // numTerm = numVal | ifNumExpr | brackMathExpr | signedExpr
    private static final Parser<Character, NumExpr> numTerm =
        choice(
            numVal,
            measure,
            ifNumExpr,
            brackNumExpr,
            signedExpr
        );

    // prod = numTerm chainl1 multDiv
    private static final Parser<Character, NumExpr> prod =
        numTerm.chainl1(multDiv);

    // numExpr = prod chainl1 addSub
    private static Parser<Character, NumExpr> numExpr() {
        return prod.chainl1(addSub);
    }

    // strVal = dblQt <string> dblQt
    private static final Parser<Character, StrVal> strVal =
        strBetween(dblQt, dblQt).bind(s -> retn(StrValImpl.of(s)));

    // strExpr = strVal
    private static final Parser<Character, StrExpr> strExpr =
        choice(strVal, strVal);

    // PARSERS FOR LOGICAL EXPRESSIONS.

    // brackBoolExpr = open boolExpr close
    private static final Parser<Character, BoolExpr> brackBoolExpr =
        attempt(
            open.then(
                boolExpr.bind(be ->
                        close.then(
                            retn(be)
                        )
                )
            )
        ).label("brack-bool-expr");

    // notExpr = NOT expr
    private static final Parser<Character, NotExpr> notExpr =
        attempt(string("NOT")).then(
            open.then(
                boolExpr.bind(be ->
                        close.then(
                            retn((NotExpr) NotExprImpl.of(be))
                        )
                )
            )
        ).label("not-expr");

    // dimExpr = dblQt <string> dblQt
    private static final Parser<Character, Dimension> dimExpr =
        strBetween(openS, closeS).bind(s -> retn(DimensionImpl.of(s)));

    // isNullExpr = "ISNULL" open dimExpr close
    private static final Parser<Character, IsNullExpr> isNullExpr =
        attempt(string("ISNULL")).then(
            open.then(
                dimExpr.bind(de ->
                        retn((IsNullExpr) IsNullExprImpl.of(de))
                )
            )
        ).label("isnull-expr");

    // strExprList = strExpr , strExpr , ...
    private static final Parser<Character, IList<StrExpr>> strExprList =
        sepBy(strExpr, chr(','));

    // inListExpr = "INLIST" open dimExpr strExprList close
    private static final Parser<Character, InListExpr> inListExpr =
        attempt(string("INLIST")).then(
            open.then(
                strExpr.bind(se ->
                    strExprList.bind(sel ->
                        close.then(
                            retn((InListExpr) InListExprImpl.of(se, sel))
                        )
                    )
                )
            )
        ).label("inlist-expr");

    // eqExpr = numExpr eqOp numExpr
    private static final Parser<Character, BoolExpr> eqExpr =
        numExpr.bind(lhs ->
                eqOp.bind(op ->
                        numExpr.bind(rhs ->
                                retn((BoolExpr) EqualityOpExprImpl.of(op, lhs, rhs))
                        )
                )
        ).label("eq-expr");

    // boolTerm = brackBoolExpr | notExpr
    private static final Parser<Character, BoolExpr> boolTerm =
        choice(
            eqExpr,
            notExpr,
            brackBoolExpr,
            isNullExpr,
            inListExpr
        );

    private static final Parser<Character, BinaryOperator<BoolExpr>> andFunc =
        token("AND").then(
            retn(AndExprImpl::of)
        );

    // andTerm = boolTerm chainl1 andExpr
    private static final Parser<Character, BoolExpr> andTerm =
        boolTerm.chainl1(andFunc);

    private static final Parser<Character, BinaryOperator<BoolExpr>> orFunc =
        token("OR").then(
            retn(OrExprImpl::of)
        );

    // boolExpr = andTerm chainl1 orExpr
    private static final Parser<Character, BoolExpr> boolExpr() {
        return andTerm.chainl1(orFunc);
    }

    static {
        //expr.set(expr());
        numExpr.set(numExpr());
        boolExpr.set(boolExpr());
    }

    // Use a variable to help the type inference.
    private static final Parser<Character, Void> end = eof();

    // parser = expr eof
    private static Parser<Character, BoolExpr> parser() {
        return boolExpr.bind(x -> end.then(retn(x)));
    }

    public static final Parser<Character, BoolExpr> parser = parser();

    public static Reply<Character, BoolExpr> parse(String s) {
        return parser.parse(State.of(s));
    }
}

/*

    // andExpr = expr AND expr
    private static final Parser<Character, AndExpr> andExpr =
        boolExpr.bind(lhs ->
                attempt(string("AND")).then(
                    boolExpr.bind(rhs ->
                            retn((AndExpr) AndExprImpl.of(lhs, rhs))
                    )
                )
        ).label("and-expr");

    // orExpr = expr OR expr
    private static final Parser<Character, OrExpr> orExpr =
        boolExpr.bind(lhs ->
                attempt(string("OR")).then(
                    boolExpr.bind(rhs ->
                            retn((OrExpr) OrExprImpl.of(lhs, rhs))
                    )
                )
        ).label("or-expr");

    // argList = expr , expr , ...
    private static final Parser<Character, IList<Expr>> argList =
        sepBy(expr, chr(','));

    // funcExpr = <string> ( argList )
    private static final Parser<Character, Expr> funcExpr =
        alphaNum.bind(name ->
            open.then(
                argList.bind(args ->
                    close.then(
                        retn(FunctionRegistry.constructExpr(name, args))
                    )
                )
            )
        ).label("func-expr");

    public static class FunctionRegistry {
        public static class ExprFunction {

            public static class Param {
                public final String name;
                public final ExprType type;

                public Param(String name, ExprType type) {
                    this.name = name;
                    this.type = type;
                }
            }

            public final String name;
            public final ExprType returnType;
            public final List<Param> params;
            public final Function<IList<Expr>, Expr> ctor;

            public ExprFunction(String name, ExprType returnType, List<Param> params, Function<IList<Filter.Expr>, Filter.Expr> ctor) {
                this.name = name;
                this.returnType = returnType;
                this.params = params;
                this.ctor = ctor;
            }

            public Expr construct(IList<Expr> args) {

                // Check arg list size matches parameters.
                if (args.size() != params.size()) {
                    throw new RuntimeException(
                        this + " called with only " + args.size() + " arguments)");
                }

                // Check each arg has correct type.
                final Iterator<Expr> exprIter = args.iterator();
                for (Param param : params) {
                    final Expr expr = exprIter.next();
                    if (param.type != expr.type()) {
                        throw new RuntimeException(
                            this + " called with incorrect type " + expr.type() +
                                " for parameter " + param.name
                        );
                    }
                }
                return ctor.apply(args);
            }

            @Override
            public String toString() {
                final StringBuilder sb = new StringBuilder(name);
                sb.append('(');
                boolean comma = false;
                for (Param param : params) {
                    if (comma) {
                        sb.append(", ");
                    } else {
                        comma = true;
                    }
                    sb.append(param.name).append(" : ").append(param.type);
                }

                return sb.append(')').toString();
            }
        }

        private static final List<ExprFunction> functions = Lists.newLinkedList();

        public void register(
                String name,
                ExprType returnType,
                List<ExprFunction.Param> params,
                Function<IList<Expr>, Expr> ctor) {
            functions.add(new ExprFunction(name, returnType, params, ctor));
        }

        public static Expr constructExpr(String name, IList<Expr> args) {
            for (ExprFunction func : functions) {
                if (func.name.equals(name)) {
                    return func.construct(args);
                }
            }

            throw new RuntimeException("Function name " + name + " is not recognised");
        }
    }

 */
