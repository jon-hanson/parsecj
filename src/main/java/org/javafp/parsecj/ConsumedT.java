package org.javafp.parsecj;

import java.util.function.Supplier;

/**
 * Discriminated union between a Consumed type and an Empty type.
 * Wraps a parse result (Reply) and indicates whether the parser consumed input
 * in the process of computing the parse result.
 * @param <S> Input stream symbol type.
 * @param <A> Parse result type
 */
public interface ConsumedT<S, A> {

    public static <S, A> ConsumedT<S, A> Consumed(Supplier<Reply<S, A>> supplier) {
        return new Consumed<S, A>(supplier);
    }

    public static <S, A> ConsumedT<S, A> Empty(Reply<S, A> reply) {
        return new Empty<S, A>(reply);
    }

    public boolean isConsumed();

    public Reply<S, A> getReply();

    public default <B> ConsumedT<S, B> cast() {
        return (ConsumedT<S, B>)this;
    }
}

/**
 * A parse result that indicates the parser did consume some input.
 */
final class Consumed<S, A> implements ConsumedT<S, A> {

    // Lazy Reply supplier.
    private Supplier<Reply<S, A>> supplier;

    // Lazy-initialised Reply.
    private Reply<S, A> reply;

    Consumed(Supplier<Reply<S, A>> supplier) {
        this.supplier = supplier;
    }

    @Override
    public boolean isConsumed() {
        return true;
    }

    @Override
    public Reply<S, A> getReply() {
        if (supplier != null) {
            reply = supplier.get();
            supplier = null;
        }

        return reply;
    }
}

/**
 * A parse result that indicates the parser did not consume any input.
 */
final class Empty<S, A> implements ConsumedT<S, A> {

    private final Reply<S, A> reply;

    Empty(Reply<S, A> reply) {
        this.reply = reply;
    }

    @Override
    public boolean isConsumed() {
        return false;
    }

    @Override
    public Reply<S, A> getReply() {
        return reply;
    }
}
