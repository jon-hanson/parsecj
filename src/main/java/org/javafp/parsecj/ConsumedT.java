package org.javafp.parsecj;

import java.util.function.Supplier;

/**
 * ConsumedT is a discriminated union between a Consumed type and an Empty type.
 * Wraps a parse result (Reply) and indicates whether the parser consumed input
 * in the process of computing the parse result.
 * @param <I> Input stream symbol type.
 * @param <A> Parse result type
 */
public interface ConsumedT<I, A> {

    static <I, A> ConsumedT<I, A> consumed(Supplier<Reply<I, A>> supplier) {
        return new Consumed<I, A>(supplier);
    }

    static <I, A> ConsumedT<I, A> empty(Reply<I, A> reply) {
        return new Empty<I, A>(reply);
    }

    static <I, A> ConsumedT<I, A> of(boolean consumed, Supplier<Reply<I, A>> supplier) {
        return consumed ?
            ConsumedT.consumed(supplier) :
            ConsumedT.empty(supplier.get());
    }

    boolean isConsumed();

    Reply<I, A> getReply();

    default <B> ConsumedT<I, B> cast() {
        return (ConsumedT<I, B>)this;
    }
}

/**
 * A parse result that indicates the parser did consume some input.
 * Consumed is lazy with regards to the reply it wraps.
 */
final class Consumed<I, A> implements ConsumedT<I, A> {

    // Lazy Reply supplier.
    private Supplier<Reply<I, A>> supplier;

    // Lazy-initialised Reply.
    private Reply<I, A> reply;

    Consumed(Supplier<Reply<I, A>> supplier) {
        this.supplier = supplier;
    }

    @Override
    public boolean isConsumed() {
        return true;
    }

    @Override
    public Reply<I, A> getReply() {
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
final class Empty<I, A> implements ConsumedT<I, A> {

    private final Reply<I, A> reply;

    Empty(Reply<I, A> reply) {
        this.reply = reply;
    }

    @Override
    public boolean isConsumed() {
        return false;
    }

    @Override
    public Reply<I, A> getReply() {
        return reply;
    }
}
