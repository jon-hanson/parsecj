package org.javafp.parsecj.input;

import org.javafp.parsecj.utils.CharSequences;

import java.util.*;

/**
 * An interface for parseable, immutable symbol streams.
 * @param <I> Input stream symbol type.
 */
public interface Input<I> {
    static <I> Input<I> of(I[] symbols) {
        return new ArrayInput<I>(symbols);
    }

    static Input<Character> of(Character[] symbols) {
        return new CharArrayInput(symbols);
    }

    static Input<Character> of(String symbols) {
        return new StringInput(symbols);
    }

    int position();

    boolean end();

    I current();

    List<I> current(int n);

    default Input<I> next() {
        return next(1);
    }

    Input<I> next(int n);
}

