package org.javafp.parsecj.utils;

import java.util.List;

public class CharListCharSequence implements CharSequence {
    protected final List<Character> symbols;
    protected final int start;
    protected final int end;

    CharListCharSequence(List<Character> symbols, int start, int end) {
        this.symbols = symbols;
        this.start = start;
        this.end = end;
    }

    @Override
    public int length() {
        return end - start;
    }

    @Override
    public char charAt(int index) {
        return symbols.get(start + index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        if (start < 0) {
            throw new IndexOutOfBoundsException("Start index " + start + " is out of bounds");
        }
        if (end > length()) {
            throw new IndexOutOfBoundsException("End index " + end + " is out of bounds");
        }
        return new CharListCharSequence(symbols, this.start + start, this.start + end);
    }
}
