package org.javafp.parsecj;

public abstract class Merge {

    public static <S, A> ConsumedT<S, A> mergeOk(A x, State<S> state, Message.Ref<S> msg1, Message.Ref<S> msg2) {
        return ConsumedT.empty(Reply.ok(x, state, msg1.merge(msg2)));
    }

    public static <S, A> ConsumedT<S, A> mergeError(Message.Ref<S> msg1, Message.Ref<S> msg2) {
        return ConsumedT.empty(Reply.error(msg1.merge(msg2)));
    }
}
