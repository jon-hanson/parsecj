package org.javafp.parsecj;

import org.javafp.parsecj.utils.CharSequences;

import java.util.*;

/**
 * An interface for parseable, immutable symbol streams.
 * @param <S> Input stream symbol type.
 */
public interface State<S> {
    static <S> State<S> of(S[] symbols) {
        return new ArrayState<S>(symbols);
    }

    static State<Character> of(Character[] symbols) {
        return new CharArrayState(symbols);
    }

    static State<Character> of(String symbols) {
        return new StringState(symbols);
    }

    int position();

    boolean end();

    S current();

    List<S> current(int n);

    default State<S> next() {
        return next(1);
    }

    State<S> next(int n);
}

interface CharState extends State<Character> {
    CharSequence getCharSequence();
    CharSequence getCharSequence(int length);
}

class StringState implements CharState {

    protected final String symbols;

    protected final int pos;

    StringState(String symbols, int pos) {
        this.symbols = symbols;
        this.pos = pos;
    }

    StringState(String symbols) {
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
    public StringState next() {
        return new StringState(symbols, pos + 1);
    }

    @Override
    public StringState next(int n) {
        return new StringState(symbols, pos + n);
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

class ArrayState<S> implements State<S> {

    protected final S[] symbols;

    protected final int pos;

    ArrayState(S[] symbols, int pos) {
        this.symbols = symbols;
        this.pos = pos;
    }

    ArrayState(S[] symbols) {
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
    public S current() {
        return pos < symbols.length ? symbols[pos] : null;
    }

    @Override
    public List<S> current(int n) {
        return Arrays.asList(symbols).subList(pos, pos + n);
    }

    @Override
    public State<S> next() {
        return new ArrayState<S>(symbols, pos + 1);
    }

    @Override
    public State<S> next(int n) {
        return new ArrayState<S>(symbols, pos + n);
    }
}

class CharArrayState extends ArrayState<Character> implements CharState {

    CharArrayState(Character[] symbols, int i) {
        super(symbols, i);
    }

    CharArrayState(Character[] symbols) {
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
