package org.javafp.parsecj;

import java.util.function.Function;

/**
 * A Parser result, essentially a discriminated union between a Success and an Error.
 * @param <S> Input stream symbol type.
 * @param <A> Parse result type
 */
public abstract class Reply<S, A> {

    public static <S, A> Ok<S, A> ok(A result, State<S> tail, Message<S> msg) {
        return new Ok<S, A>(result, tail, msg);
    }

    public static <S> Ok<S, Void> ok(State<S> tail, Message<S> msg) {
        return new Ok<S, Void>(null, tail, msg);
    }

    public static <S, A> Error<S, A> error(Message<S> msg) {
        return new Error<S, A>(msg);
    }

    public final Message<S> msg;

    Reply(Message<S> msg) {
        this.msg = msg;
    }

    public abstract <B> B match(Function<Ok<S, A>, B> ok, Function<Error<S, A>, B> error);

    public abstract A getResult() throws Exception;

    public abstract boolean isOk();
    public abstract boolean isError();

    public String getMsg() {
        return msg.toString();
    }

    /**
     * A successful parse result.
     */
    public static final class Ok<S, A> extends Reply<S, A> {

        /**
         * The parsed result.
         */
        public final A result;

        /**
         * The remaining input stream state.
         */
        public final State<S> rest;

        Ok(A result, State<S> rest, Message<S> msg) {
            super(msg);
            this.result = result;
            this.rest = rest;
        }

        @Override
        public <U> U match(Function<Ok<S, A>, U> ok, Function<Error<S, A>, U> error) {
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
     * @param <S> Input stream symbol type.
     * @param <A> Parse result type
     */
    public static final class Error<S, A> extends Reply<S, A> {

        Error(Message<S> msg) {
            super(msg);
        }

        @Override
        public <B> B match(Function<Ok<S, A>, B> ok, Function<Error<S, A>, B> error) {
            return error.apply(this);
        }

        public <B> Reply<S, B> cast() {
            return (Error<S, B>)this;
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

