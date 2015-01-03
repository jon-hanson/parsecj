package org.javafp.parsecj;

import java.util.function.Function;

/**
 * A Parser result, essentially a discriminated union between a Success and an Error.
 * @param <S> Input stream symbol type.
 * @param <A> Parse result type
 */
public abstract class Reply<S, A> {

    static <S, A> Ok<S, A> ok(A result, State<S> tail, Message.Ref<S> msg) {
        return new Ok<S, A>(result, tail, msg);
    }

    static <S, A> Error<S, A> error(Message.Ref<S> msg) {
        return new Error<S, A>(msg);
    }

    public final Message.Ref<S> msg;

    Reply(Message.Ref<S> msg) {
        this.msg = msg;
    }

    public abstract <B> B match(Function<Ok<S, A>, B> ok, Function<Error<S, A>, B> error);

    public abstract A getResult() throws Exception;

    public String getMsg() {
        return msg.get().msg();
    }
}

