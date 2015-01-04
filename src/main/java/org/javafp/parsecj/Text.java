package org.javafp.parsecj;

import org.javafp.data.List;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.javafp.parsecj.Combinators.*;
import static org.javafp.parsecj.Combinators.satisfy;

/**
 * Parser combinators to be used with Character streams.
 */
public abstract class Text {

    /**
     * Helper method to create a ConsumedT response.
     */
    public static <S, A> ConsumedT<S, A> createConsError(boolean consumed, State<S> state, String expected) {
        final List<String> expList = List.of(expected);
        return consumed ?
            ConsumedT.consumed(() ->
                Reply.error(
                    Message.Ref.of(() -> Message.of(state, expList))
                )
            ) : ConsumedT.empty(
                Reply.error(
                    Message.Ref.of(() -> Message.of(state, expList))
                )
        );
    }

    private static <S, A> ConsumedT<S, A> endOfInputError(boolean consumed, State<S> state) {
        return consumed ?
            ConsumedT.consumed(() -> endOfInput(state)) :
            ConsumedT.empty(endOfInput(state));
    }

    /**
     * A parser which parses an alphabetic character.
     */
    public static Parser<Character, Character> alpha = satisfy((Character c) -> Character.isAlphabetic(c)).label("alpha");

    /**
     * A parser which parses a numeric character, i.e. a digit.
     */
    public static Parser<Character, Character> digit = satisfy((Character c) -> Character.isDigit(c)).label("digit");

    /**
     * A parser which parses space.
     */
    public static Parser<Character, Character> space = satisfy((Character c) -> Character.isSpaceChar(c)).label("space");

    /**
     * A parser which parses whitespace.
     */
    public static Parser<Character, Character> wspace = satisfy((Character c) -> Character.isWhitespace(c)).label("wspace");

    /**
     * A parser which parses whitespace.
     */
    public static Parser<Character, Void> wspaces = skipMany(satisfy(c -> Character.isWhitespace(c)));

    /**
     * A parser which parses the specified char.
     */
    public static Parser<Character, Character> chr(char c) {
        return satisfy(ic -> ic == c);
    }

    /**
     * A parser which parses a signed integer.
     */
    public static final Parser<Character, Integer> intr =
        state -> {
            if (state.end()) {
                return ConsumedT.empty(endOfInput(state));
            }

            boolean consumed = false;

            boolean signPos = true;
            char c = state.current();
            switch (c) {
                case '-':
                    signPos = false;
                    state = state.inc();
                    consumed = true;
                    break;
                case '+':
                    state = state.inc();
                    consumed = true;
                    break;
            }

            if (state.end()) {
                return endOfInputError(consumed, state);
            }

            int acc = 0;
            c = state.current();
            if (!Character.isDigit(c)) {
                return createConsError(consumed, state, "integer");
            }

            consumed = true;

            do {
                acc = acc * 10 + (c - '0');
                state = state.inc();
                if (state.end()) {
                    break;
                }
                c = state.current();

            } while (Character.isDigit(c));

            final int res = signPos ? acc : -acc;
            final State<Character> tail = state;
            return ConsumedT.consumed(
                () -> Reply.ok(
                    res,
                    tail,
                    Message.Ref.of(() -> Message.of(tail, List.empty()))
                )
            );
        };

    /**
     * A parser which parses a signed double.
     */
    public static final Parser<Character, Double> dble =
        regex("-?(\\d+(\\.\\d*)?|\\d*\\.\\d+)([eE][+-]?\\d+)?[fFdD]?")
            .label("double")
            .bind(dblStr -> retn(Double.valueOf(dblStr)));

    /**
     * A parser which parses the specified string.
     */
    public static Parser<Character, String> string(String value) {
        return state -> {
            if (state.end()) {
                return ConsumedT.empty(endOfInput(state));
            }

            boolean consumed = false;
            char c = state.current();
            int i = 0;
            while (c == value.charAt(i)) {
                consumed = true;
                state = state.inc();
                c = state.current();
                ++i;
                if (i == value.length()) {
                    final State<Character> tail = state;
                    return ConsumedT.consumed(
                        () -> Reply.ok(
                            value,
                            tail,
                            Message.Ref.of(() -> Message.of(tail, List.empty()))
                        )
                    );
                } else if (state.end()) {
                    return endOfInputError(consumed, state);
                }
            }

            return createConsError(consumed, state, "string");
        };
    }

    /**
     * A parser which parses an alphanumeric string.
     */
    public static final Parser<Character, String> alphaNum =
        state -> {
            if (state.end()) {
                return ConsumedT.empty(endOfInput(state));
            }

            char c = state.current();
            if (!Character.isAlphabetic(c) && !Character.isDigit(c)) {
                final State<Character> tail = state;
                return ConsumedT.empty(
                    Reply.error(
                        Message.Ref.of(() -> Message.of(tail, List.empty()))
                    )
                );
            }

            final StringBuilder sb = new StringBuilder();
            do {
                sb.append(c);
                state = state.inc();
                if (state.end()) {
                    break;
                }
                c = state.current();

            } while (Character.isAlphabetic(c) || Character.isDigit(c));

            final State<Character> tail = state;
            return ConsumedT.consumed(
                () -> Reply.ok(
                    sb.toString(),
                    tail,
                    Message.Ref.of(() -> Message.of(tail, List.empty()))
                )
            );
        };

    /**
     * A parser which parses a string which matches the supplied regex.
     */
    public static Parser<Character, String> regex(String regex) {
        final Pattern pattern = Pattern.compile(regex);
        return state -> {
            final CharSequence cs;
            if (state instanceof CharState) {
                final CharState charState = (CharState) state;
                cs = charState.getCharSequence();
            } else {
                throw new RuntimeException("regex only supported on CharState");
            }

            final Matcher matcher = pattern.matcher(cs);

            final Message.Ref<Character> msg = Message.Ref.of(
                () -> Message.of(state, List.of("Regex '" + regex + "'"))
            );

            if (matcher.lookingAt()) {
                final int end = matcher.end();
                final String str = cs.subSequence(0, end).toString();
                return ConsumedT.consumed(() -> Reply.ok(str, state.inc(end), msg));
            } else {
                return ConsumedT.empty(Reply.error(msg));
            }
        };
    }
}
