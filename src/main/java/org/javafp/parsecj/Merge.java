package org.javafp.parsecj;

public abstract class Merge {

    public static <S, A> ConsumedT<S, A> mergeOk(A x, State<S> state, Message.Ref<S> msg1, Message.Ref<S> msg2) {
        return ConsumedT.Empty(Reply.Ok(x, state, msg1.merge(msg2)));
    }

    public static <S, A> ConsumedT<S, A> mergeError(Message.Ref<S> msg1, Message.Ref<S> msg2) {
        return ConsumedT.Empty(Reply.Error(msg1.merge(msg2)));
    }
}
