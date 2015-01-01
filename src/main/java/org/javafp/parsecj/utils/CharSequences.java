package org.javafp.parsecj.utils;

import java.util.List;

public abstract class CharSequences {
    public static CharSequence of(List<Character> symbols, int start, int end) {
        return new CharListCharSequence(symbols, start, end);
    }

    public static CharSequence of(List<Character> symbols) {
        return of(symbols, 0, symbols.size());
    }

    public static CharSequence of(Character[] symbols, int start, int end) {
        return new CharArrayCharSequence(symbols, start, end);
    }

    public static CharSequence of(Character[] symbols) {
        return of(symbols, 0, symbols.length);
    }
}
