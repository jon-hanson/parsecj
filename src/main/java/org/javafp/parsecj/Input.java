package org.javafp.parsecj;

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

interface CharInput extends Input<Character> {
    CharSequence getCharSequence();
    CharSequence getCharSequence(int length);
}

class StringInput implements CharInput {

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

class ArrayInput<I> implements Input<I> {

    protected final I[] symbols;

    protected final int pos;

    ArrayInput(I[] symbols, int pos) {
        this.symbols = symbols;
        this.pos = pos;
    }

    ArrayInput(I[] symbols) {
        this(symbols, 0);
    }

    @Override
    public int position() {
        return pos;
    }

    @Override
    public boolean end() {
        return pos >= symbols.length;
    }

    @Override
    public I current() {
        return pos < symbols.length ? symbols[pos] : null;
    }

    @Override
    public List<I> current(int n) {
        return Arrays.asList(symbols).subList(pos, pos + n);
    }

    @Override
    public Input<I> next() {
        return new ArrayInput<I>(symbols, pos + 1);
    }

    @Override
    public Input<I> next(int n) {
        return new ArrayInput<I>(symbols, pos + n);
    }
}

class CharArrayInput extends ArrayInput<Character> implements CharInput {

    CharArrayInput(Character[] symbols, int i) {
        super(symbols, i);
    }

    CharArrayInput(Character[] symbols) {
        super(symbols);
    }

    @Override
    public CharSequence getCharSequence() {
        return CharSequences.of(symbols, pos, symbols.length - pos);
    }

    @Override
    public CharSequence getCharSequence(final int maxLength) {
        final int length = Math.min(symbols.length - pos, maxLength);
        return CharSequences.of(symbols, pos, length);
    }
}
