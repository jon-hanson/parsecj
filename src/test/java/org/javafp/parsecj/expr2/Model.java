package org.javafp.parsecj.expr2;

import java.util.function.BinaryOperator;

/**
 * A model for simple mathematical expressions.
 */
public class Model {

    public static Expr numExpr(double val, NumExpr.Units units) {
        return new NumExpr(val, units);
    }

    public static Expr varExpr(String name) {
        return new VarExpr(name);
    }

    public static Expr unaryOpExpr(UnaryOp op, Expr expr) {
        return new UnaryOpExpr(op, expr);
    }

    public static Expr binOpExpr(Expr lhs, BinOp op, Expr rhs) {
        return new BinOpExpr(lhs, op, rhs);
    }

    public static Expr func2Expr(String name, Expr arg0, Expr arg1) {
        return new Func2Expr(name, arg0, arg1);
    }

    public static abstract class Expr {

        @Override
        public String toString() {
            return string(new StringBuilder()).toString();
        }

        public abstract StringBuilder string(StringBuilder sb);
    }

    public static class NumExpr extends Expr {

        public enum Units {
            ABS(""),
            PCT("%"),
            BPS("bp");

            private final String s;

            Units(String s) {
                this.s = s;
            }

            @Override
            public String toString() {
                return s;
            }
        }

        public final double value;
        public final Units units;

        public NumExpr(double value, Units units) {
            this.value = value;
            this.units = units;
        }

        @Override
        public StringBuilder string(StringBuilder sb) {
            return sb.append(value).append(units);
        }
    }

    public static class VarExpr extends Expr {

        public final String name;

        public VarExpr(String name) {
            this.name = name;
        }

        @Override
        public StringBuilder string(StringBuilder sb) {
            return sb.append(name);
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

    public static class UnaryOpExpr extends Expr {

        public final UnaryOp op;
        public final Expr expr;

        public UnaryOpExpr(UnaryOp op, Expr expr) {
            this.op = op;
            this.expr = expr;
        }

        @Override
        public StringBuilder string(StringBuilder sb) {
            sb.append(op);
            sb.append('(');
            sb.append(expr);
            return sb.append(')');
        }
    }

    public enum BinOp {
        ADD('+'),
        SUBTRACT('-'),
        MULTIPLY('*'),
        DIVIDE('/');

        final char code;

        BinOp(char code) {
            this.code = code;
        }

        @Override
        public String toString() {
            return String.valueOf(code);
        }

        public BinaryOperator<Expr> ctor() {
            return (lhs, rhs) -> new BinOpExpr(lhs, this, rhs);
        }
    }

    public static class BinOpExpr extends Expr {

        public final Expr lhs;
        public final BinOp op;
        public final Expr rhs;

            protected BinOpExpr(Expr lhs, BinOp op, Expr rhs) {
            this.lhs = lhs;
            this. op = op;
            this.rhs = rhs;
        }

        @Override
        public StringBuilder string(StringBuilder sb) {
            sb.append('(');
            lhs.string(sb);
            sb.append(op);
            rhs.string(sb);
            return sb.append(')');
        }
    }

    public static class Func2Expr extends Expr {
        public final String name;
        public final Expr arg0;
        public final Expr arg1;

        public Func2Expr(String name, Expr arg0, Expr arg1) {
            this.name = name;
            this.arg0 = arg0;
            this.arg1 = arg1;
        }

        @Override
        public StringBuilder string(StringBuilder sb) {
            sb.append(name).append('(');
            arg0.string(sb);
            sb.append(',');
            arg1.string(sb);
            return sb.append(')');
        }
    }
}
