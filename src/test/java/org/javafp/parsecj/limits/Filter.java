package org.javafp.parsecj.limits;

import com.google.common.collect.Lists;
import org.immutables.value.Value;

import java.util.List;

@Value.Enclosing
@DefaultStyle
public abstract class Filter {

    public static abstract class TypeException extends RuntimeException {
        public final Expr expr;
        public final String exprName;

        public TypeException(Expr expr, String exprName) {
            this.expr = expr;
            this.exprName = exprName;
        }

        public abstract String getMessage();
    }

    public static class GeneralTypeException extends TypeException {

        public final String message;

        public GeneralTypeException(Expr expr, String exprName, String message) {
            super(expr, exprName);
            this.message = message;
        }

        @Override
        public String getMessage() {
            return exprName + " failed to type-check - " + message;
        }
    }

    public static class TypeDiffException extends TypeException {
        public final Expr expr1;
        public final String exprName1;
        public final Expr expr2;
        public final String exprName2;

        public TypeDiffException(
                Expr expr,
                String exprName,
                Expr expr1,
                String exprName1,
                Expr expr2,
                String exprName2) {
            super(expr, exprName);
            this.expr1 = expr1;
            this.exprName1 = exprName1;
            this.expr2 = expr2;
            this.exprName2 = exprName2;
        }

        @Override
        public String getMessage() {
            return exprName + " failed to type-check - " +
                exprName1 + " has type " + expr1.type() +
                " whereas " + exprName2 + " has type " + expr2.type() + " - both need to have the same type";
        }
    }

    public static class TypeFail {
        public final Expr expr;
        public final String exprName;
        public final ExprType expectedType;

        public TypeFail(Expr expr, String exprName, ExprType expectedType) {
            this.expr = expr;
            this.exprName = exprName;
            this.expectedType = expectedType;
        }

        public String message() {
            return exprName + " had type " + expr.type() + ", expected type " + expectedType;
        }
    }

    public static class TypeSingleException extends TypeException {
        public final TypeFail typeFail;

        public TypeSingleException(Expr expr, String exprName, TypeFail typeFail) {
            super(expr, exprName);
            this.typeFail = typeFail;
        }


        @Override
        public String getMessage() {
            return exprName + " failed to type-check - " + typeFail.message();
        }
    }

    public static class TypeMultiException extends TypeException {
        public final List<TypeFail> typeFails;

        public TypeMultiException(Expr expr, String exprName, List<TypeFail> typeFails) {
            super(expr, exprName);
            this.typeFails = typeFails;
        }

        @Override
        public String getMessage() {
            StringBuilder sb = new StringBuilder(exprName).append(" failed to type-check:");
            for (TypeFail typeFail : typeFails) {
                sb.append(System.lineSeparator()).append("    ").append(typeFail.message());
            }
            return sb.toString();
        }
    }

    private static void typeCheck(Expr expr, String exprName, ExprType type, Expr lhs, Expr rhs) {
        final List<TypeFail> typeFails = Lists.newLinkedList();

        if (lhs.type() != type) {
            typeFails.add(new TypeFail(lhs, "1st arg", type));
        }

        if (rhs.type() != type) {
            typeFails.add(new TypeFail(rhs, "2nd arg", type));
        }

        switch(typeFails.size()) {
            case 0:
                break;
            case 1:
                throw new TypeSingleException(expr, exprName, typeFails.get(0));
            default:
                throw new TypeMultiException(expr, exprName, typeFails);
        }
    }

    private static void typeCheck(Expr expr, String exprName, Expr arg1, Expr arg2) {
        if (arg1.type() != arg2.type()) {
            throw new TypeDiffException(
                expr,
                exprName,
                arg1,
                "1st arg",
                arg2,
                "2nd arg"
            );
        }
    }

    private static void typeCheck(Expr expr, String exprName, int shift, List<Expr> args) {
        if (args.size() > 0) {
            ExprType type = args.get(0).type();
            for (int i = 1; i < args.size(); ++i) {
                if (args.get(i).type() != type) {
                    throw new GeneralTypeException(
                        expr,
                        exprName,
                        "arg " + (shift + i) + " has different type (" + args.get(i).type() +
                            ") to the preceding args (" + type + ")"
                    );
                }
            }
        }
    }

    private static void typeCheck(Expr expr, String exprName, ExprType type, Expr arg) {
        if (arg.type() != type) {
            throw new TypeSingleException(
                expr,
                exprName,
                new TypeFail(arg, "arg", type)
            );
        }
    }

    public static class FormulaBuilder {
        private StringBuilder sb = new StringBuilder();

        @Override
        public String toString() {
            return sb.toString();
        }

        public FormulaBuilder append(char c) {
            sb.append(c);
            return this;
        }

        public FormulaBuilder append(String s) {
            sb.append(s);
            return this;
        }

        public FormulaBuilder append(int i) {
            sb.append(i);
            return this;
        }

        public FormulaBuilder append(double d) {
            sb.append(d);
            return this;
        }

        public <E extends Enum<E>> FormulaBuilder append(Enum<E> e) {
            sb.append(e.toString());
            return this;
        }

        public FormulaBuilder append(Expr expr) {
            return expr.formula(this);
        }


        public <E extends Expr> FormulaBuilder append(List<E> exprs) {
            boolean comma = false;
            for (E expr : exprs) {
                if (comma) {
                    sb.append(',');
                } else {
                    comma = true;
                }
                append(expr);
            }
            return this;
        }
    }

    public enum ExprType {
        BOOL,
        NUMBER,
        STRING
    }

    public static abstract class Expr {
        public String formula() {
            return formula(new FormulaBuilder()).toString();
        }

        protected abstract FormulaBuilder formula(FormulaBuilder sb);

        public abstract ExprType type();

        public void typeCheck() {
            // Empty default implementation.
        }
    }

    public static abstract class BoolExpr extends Expr {
        public ExprType type() {
            return ExprType.BOOL;
        }
    }

    public static abstract class StrExpr extends Expr {
        public ExprType type() {
            return ExprType.STRING;
        }
    }

    public static abstract class NumExpr extends Expr {
        public ExprType type() {
            return ExprType.NUMBER;
        }
    }

    @Value.Immutable
    public static abstract class Dimension extends StrExpr {
        @Value.Parameter
        public abstract String name();

        protected FormulaBuilder formula(FormulaBuilder fb) {
            return fb
                .append('[')
                .append(name())
                .append(']');
        }
    }

    @Value.Immutable
    public static abstract class Measure extends NumExpr {
        @Value.Parameter
        public abstract String name();

        protected FormulaBuilder formula(FormulaBuilder sb) {
            return sb
                .append('{')
                .append(name())
                .append('}');
        }
    }

    @Value.Immutable
    public static abstract class StrVal extends StrExpr {
        @Value.Parameter
        public abstract String value();

        protected FormulaBuilder formula(FormulaBuilder sb) {
            return sb
                .append('"')
                .append(value())
                .append('"');
        }
    }

    public static abstract class NumVal extends NumExpr {
        public abstract int asInt();
        public abstract double asDbl();
    }

    @Value.Immutable
    public static abstract class IntVal extends NumVal {
        @Value.Parameter
        public abstract int value();

        public int asInt() {
            return value();
        }

        public double asDbl() {
            return value();
        }

        @Override
        protected FormulaBuilder formula(FormulaBuilder sb) {
            return sb.append(value());
        }
    }

    @Value.Immutable
    public static abstract class DblVal extends NumVal {
        @Value.Parameter
        public abstract double value();

        public int asInt() {
            return (int)value();
        }

        public double asDbl() {
            return value();
        }

        @Override
        protected FormulaBuilder formula(FormulaBuilder sb) {
            return sb.append(value());
        }
    }

    public enum UnaryOp {
        POS('+'),
        NEG('-');

        final char code;

        UnaryOp(char code) {
            this.code = code;
        }

        @Override
        public String toString() {
            return String.valueOf(code);
        }
    }

    @Value.Immutable
    public static abstract class UnaryOpExpr extends NumExpr {

        @Value.Parameter
        public abstract UnaryOp op();

        @Value.Parameter
        public abstract NumExpr expr();

        @Override
        protected FormulaBuilder formula(FormulaBuilder sb) {
            return sb
                .append(op())
                .append('(')
                .append(expr())
                .append(')');
        }
    }

    public enum BinaryOp {
        ADD('+'),
        SUBTRACT('-'),
        MULTIPLY('*'),
        DIVIDE('/');

        final char code;

        BinaryOp(char code) {
            this.code = code;
        }

        @Override
        public String toString() {
            return String.valueOf(code);
        }
    }

    @Value.Immutable
    public static abstract class BinaryOpExpr extends NumExpr {

        @Value.Parameter
        public abstract BinaryOp op();

        @Value.Parameter
        public abstract NumExpr lhs();

        @Value.Parameter
        public abstract NumExpr rhs();

        @Override
        public void typeCheck() {
            Filter.typeCheck(this, op().name(), lhs(), rhs());
        }

        @Override
        protected FormulaBuilder formula(FormulaBuilder sb) {
            return sb
                .append('(')
                .append(lhs())
                .append(op())
                .append(rhs())
                .append(')');
        }
    }

    public enum EqualityOp {
        EQ("="),
        NEQ("<>"),
        LT("<"),
        LTE("<="),
        GT(">"),
        GTE(">=");

        final String code;

        EqualityOp(String code) {
            this.code = code;
        }

        @Override
        public String toString() {
            return String.valueOf(code);
        }
    }

    @Value.Immutable
    public static abstract class EqualityOpExpr extends BoolExpr {

        @Value.Parameter
        public abstract EqualityOp op();

        @Value.Parameter
        public abstract NumExpr lhs();

        @Value.Parameter
        public abstract NumExpr rhs();

//        @Override
//        public void typeCheck() {
//            Filter.typeCheck(this, op().name(), lhs(), rhs());
//        }

        @Override
        protected FormulaBuilder formula(FormulaBuilder sb) {
            return sb
                .append('(')
                .append(lhs())
                .append(op())
                .append(rhs())
                .append(')');
        }
    }

    @Value.Immutable
    public static abstract class NotExpr extends BoolExpr {

        @Value.Parameter
        public abstract BoolExpr arg();

        @Override
        protected FormulaBuilder formula(FormulaBuilder sb) {
            return sb
                .append("NOT(")
                .append(arg())
                .append(')');
        }

//        @Override
//        public void typeCheck() {
//            Filter.typeCheck(this, "NOT", ExprType.BOOL, arg());
//        }
    }

    @Value.Immutable
    public static abstract class AndExpr extends BoolExpr {

        @Value.Parameter
        public abstract BoolExpr lhs();

        @Value.Parameter
        public abstract BoolExpr rhs();

//        @Override
//        public void typeCheck() {
//            Filter.typeCheck(this, "AND", ExprType.BOOL, lhs(), rhs());
//        }

        @Override
        protected FormulaBuilder formula(FormulaBuilder sb) {
            return sb
                .append("(")
                .append(lhs())
                .append(" AND ")
                .append(rhs())
                .append(')');
        }
    }

    @Value.Immutable
    public static abstract class OrExpr extends BoolExpr {

        @Value.Parameter
        public abstract BoolExpr lhs();

        @Value.Parameter
        public abstract BoolExpr rhs();

//        @Override
//        public void typeCheck() {
//            Filter.typeCheck(this, "AND", ExprType.BOOL, lhs(), rhs());
//        }

        @Override
        protected FormulaBuilder formula(FormulaBuilder sb) {
            return sb
                .append("(")
                .append(lhs())
                .append(" OR ")
                .append(rhs())
                .append(')');
        }
    }

    @Value.Immutable
    public static abstract class IfNumExpr extends NumExpr {

        @Value.Parameter
        public abstract BoolExpr cond();

        @Value.Parameter
        public abstract NumExpr trueExpr();

        @Value.Parameter
        public abstract NumExpr falseExpr();

        @Override
        public void typeCheck() {
            Filter.typeCheck(this, "AND", trueExpr(), falseExpr());
        }

        @Override
        protected FormulaBuilder formula(FormulaBuilder sb) {
            return sb
                .append("IF(")
                .append(cond())
                .append(',')
                .append(trueExpr())
                .append(',')
                .append(falseExpr())
                .append(')');
        }
    }

    @Value.Immutable
    public static abstract class IsNullExpr extends BoolExpr {

        @Value.Parameter
        public abstract Dimension arg();

//        @Override
//        public void typeCheck() {
//            Filter.typeCheck(this, "ISNULL", ExprType.STRING, arg());
//        }

        @Override
        protected FormulaBuilder formula(FormulaBuilder sb) {
            return sb
                .append("ISNULL(")
                .append(arg())
                .append(')');
        }
    }


    @Value.Immutable
    public static abstract class InListExpr extends BoolExpr {

        @Value.Parameter
        public abstract StrExpr expr();

        @Value.Parameter
        public abstract List<Expr> listExpr();

        @Override
        public void typeCheck() {
            Filter.typeCheck(this, "INLIST", 1, listExpr());
        }

        @Override
        protected FormulaBuilder formula(FormulaBuilder sb) {
            return sb
                .append("INLIST(")
                .append(expr())
                .append(',')
                .append(listExpr())
                .append(')');
        }
    }
}
