package org.javafp.parsecj;

/**
 * Utility function for merging parse messages.
 */
public abstract class Merge {

    /**
     * Merge the two supplied messages into an Ok reply.
     * @return an Empty wrapping the Ok Reply.
     */
    public static <I, A> ConsumedT<I, A> mergeOk(A x, Input<I> input, Message<I> msg1, Message<I> msg2) {
        return ConsumedT.empty(Reply.ok(x, input, msg1.merge(msg2)));
    }

    /**
     * Merge the two supplied messages into an Error reply.
     * @return an Empty wrapping the Error Reply.
     */
    public static <I, A> ConsumedT<I, A> mergeError(Message<I> msg1, Message<I> msg2) {
        return ConsumedT.empty(Reply.error(msg1.merge(msg2)));
    }
}
