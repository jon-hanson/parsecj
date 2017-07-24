package org.javafp.parsecj.input;

import java.util.*;

public class ArrayInput<I> implements Input<I> {

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
