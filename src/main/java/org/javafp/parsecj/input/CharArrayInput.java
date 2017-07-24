package org.javafp.parsecj.input;

import org.javafp.parsecj.utils.CharSequences;

public class CharArrayInput extends ArrayInput<Character> implements CharInput {

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
