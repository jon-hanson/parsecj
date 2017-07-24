package org.javafp.parsecj.input;

import java.util.*;

public class StringInput implements CharInput {

    protected final String symbols;

    protected final int pos;

    StringInput(String symbols, int pos) {
        this.symbols = symbols;
        this.pos = pos;
    }

    StringInput(String symbols) {
        this(symbols, 0);
    }

    @Override
    public int position() {
        return pos;
    }

    @Override
    public boolean end() {
        return pos >= symbols.length();
    }

    @Override
    public Character current() {
        return pos < symbols.length() ? symbols.charAt(pos) : null;
    }

    @Override
    public List<Character> current(int n) {
        final List<Character> chars = new ArrayList<>(n);
        for (int i = pos; i < pos + n; ++i) {
            chars.add(symbols.charAt(i));
        }
        return chars;
    }

    @Override
    public StringInput next() {
        return new StringInput(symbols, pos + 1);
    }

    @Override
    public StringInput next(int n) {
        return new StringInput(symbols, pos + n);
    }

    @Override
    public CharSequence getCharSequence() {
        return symbols.substring(pos);
    }

    @Override
    public CharSequence getCharSequence(int length) {
        return symbols.substring(pos, pos + length);
    }
}
