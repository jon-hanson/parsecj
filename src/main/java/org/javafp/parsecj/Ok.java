package org.javafp.parsecj;

import java.util.function.Function;

/**
 * A successful parse result.
 */
public class Ok<S, A> extends Reply<S, A> {

    /**
     * The parsed result.
     */
    public final A result;

    /**
     * The remaining input stream state.
     */
    public final State<S> rest;

    Ok(A result, State<S> rest, Message.Ref<S> msg) {
        super(msg);
        this.result = result;
        this.rest = rest;
    }

    @Override
    public <U> U match(Function<Ok<S, A>, U> ok, Function<Error<S, A>, U> error) {
        return ok.apply(this);
    }

    @Override
    public A getResult() throws Exception {
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Ok ok = (Ok) o;

        if (!msg.equals(ok.msg)) return false;
        if (!rest.equals(ok.rest)) return false;
        if (result != null ? !result.equals(ok.result) : ok.result != null) return false;

        return true;
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
