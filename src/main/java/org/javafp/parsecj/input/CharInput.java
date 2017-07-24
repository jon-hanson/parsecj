package org.javafp.parsecj.input;

public interface CharInput extends Input<Character> {
    CharSequence getCharSequence();
    CharSequence getCharSequence(int length);
}
