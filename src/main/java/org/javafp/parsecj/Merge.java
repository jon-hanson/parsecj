package org.javafp.parsecj;

/**
 * Utility function for merging parse messages.
 */
public abstract class Merge {

    /**
     * Merge the two supplied messages into an Ok reply.
     * @return an Empty wrapping the Ok Reply.
     */
    public static <S, A> ConsumedT<S, A> mergeOk(A x, State<S> state, Message.Ref<S> msg1, Message.Ref<S> msg2) {
        return ConsumedT.Empty(Reply.Ok(x, state, msg1.merge(msg2)));
    }

    /**
     * Merge the two supplied messages into an Error reply.
     * @return an Empty wrapping the Error Reply.
     */
    public static <S, A> ConsumedT<S, A> mergeError(Message.Ref<S> msg1, Message.Ref<S> msg2) {
        return ConsumedT.Empty(Reply.Error(msg1.merge(msg2)));
    }
}
