package org.javafp.parsecj;

import java.util.function.Function;

/**
 * An unsuccessful parse result.
 * @param <S> Input stream symbol type.
 * @param <A> Parse result type
 */
public final class Error<S, A> extends Reply<S, A> {

    Error(Message.Ref<S> msg) {
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
        throw new Exception(msg.get().toString());
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
