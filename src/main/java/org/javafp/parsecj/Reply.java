package org.javafp.parsecj;

import org.javafp.data.Unit;
import org.javafp.parsecj.input.Input;

import java.util.function.Function;

/**
 * A Parser result, essentially a discriminated union between a Success and an Error.
 * @param <I>       input stream symbol type
 * @param <A>       the parser value type
 */
public abstract class Reply<I, A> {

    public static <I, A> Ok<I, A> ok(A result, Input<I> tail, Message<I> msg) {
        return new Ok<I, A>(result, tail, msg);
    }

    public static <I> Ok<I, Unit> ok(Input<I> tail, Message<I> msg) {
        return new Ok<  I, Unit>(null, tail, msg);
    }

    public static <I, A> Error<I, A> error(Message<I> msg) {
        return new Error<I, A>(msg);
    }

    public final Message<I> msg;

    Reply(Message<I> msg) {
        this.msg = msg;
    }

    public abstract <B> B match(Function<Ok<I, A>, B> ok, Function<Error<I, A>, B> error);

    public abstract A getResult() throws Exception;

    public abstract boolean isOk();
    public abstract boolean isError();

    public String getMsg() {
        return msg.toString();
    }

    /**
     * A successful parse result.
     * @param <I>       input stream symbol type
     * @param <A>       the parser value type
     */
    public static final class Ok<I, A> extends Reply<I, A> {

        /**
         * The parsed result.
         */
        public final A result;

        /**
         * The remaining input stream state.
         */
        public final Input<I> rest;

        Ok(A result, Input<I> rest, Message<I> msg) {
            super(msg);
            this.result = result;
            this.rest = rest;
        }

        @Override
        public <U> U match(Function<Ok<I, A>, U> ok, Function<Error<I, A>, U> error) {
            return ok.apply(this);
        }

        @Override
        public A getResult() {
            return result;
        }

        @Override
        public boolean isOk() {
            return true;
        }

        @Override
        public boolean isError() {
            return false;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Ok ok = (Ok) o;

            if (!msg.equals(ok.msg)) return false;
            if (!rest.equals(ok.rest)) return false;

            return result == null ? ok.result == null : result.equals(ok.result);
        }

        @Override
        public int hashCode() {
            int hc = msg.hashCode();
            hc = 31 * hc + (result != null ? result.hashCode() : 0);
            hc = 31 * hc + rest.hashCode();
            return hc;
        }

        @Override
        public String toString() {
            return "Ok{" +
                "msg=" + msg +
                ", result=" + result +
                ", rest=" + rest +
                '}';
        }
    }

    /**
     * An unsuccessful parse result.
     * @param <I>       input stream symbol type
     * @param <A>       the parser value type
     */
    public static final class Error<I, A> extends Reply<I, A> {

        Error(Message<I> msg) {
            super(msg);
        }

        @Override
        public <B> B match(Function<Ok<I, A>, B> ok, Function<Error<I, A>, B> error) {
            return error.apply(this);
        }

        public <B> Reply<I, B> cast() {
            return (Error<I, B>)this;
        }

        @Override
        public A getResult() throws Exception {
            throw new Exception(msg.toString());
        }

        @Override
        public boolean isOk() {
            return false;
        }

        @Override
        public boolean isError() {
            return true;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Error error = (Error) o;

            return msg.equals(error.msg);
        }

        @Override
        public int hashCode() {
            return msg.hashCode();
        }

        @Override
        public String toString() {
            return "Error{msg=" + msg + '}';
        }
    }
}
