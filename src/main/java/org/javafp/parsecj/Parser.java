package org.javafp.parsecj;

import org.javafp.data.IList;

import java.util.Optional;
import java.util.function.*;

/**
 * A parser is essentially a function taking the input stream and returning a ConsumedT.
 * The Parser type along with the retn and bind functions constitute a monad.
 * @param <S> Input stream symbol type.
 * @param <A> Parse result type
 */
@FunctionalInterface
public interface Parser<S, A> {

    public static <S, A> Parser<S, A> of(Function<State<S>, ConsumedT<S, A>> parser) {
        return parser::apply;
    }

    /**
     * A lazily initialised reference to a Parser.
     */
    public static class Ref<S, A> implements Supplier<Parser<S, A>>, Parser<S, A> {

        public static <S, A> Ref<S, A> of() {
            return new Ref();
        }

        public static <S, A> Ref<S, A> of(Parser<S, A> parser) {
            return new Ref(parser);
        }

        private Parser<S, A> parser;

        private Ref(Parser<S, A> parser) {
            this.parser = parser;
        }

        private Ref() {
            this.parser = null;
        }

        public Parser<S, A> set(Parser<S, A> parser) {
            this.parser = parser;
            return this;
        }

        @Override
        public synchronized Parser<S, A> get() {
            if (parser == null) {
                throw new RuntimeException("Null Parser Reference");
            }
            return parser;
        }

        @Override
        public ConsumedT<S, A> apply(State<S> input) {
            return get().apply(input);
        }
    }

    /**
     * Parse the input state
     * @return a ConsumedT result
     */
    ConsumedT<S, A> apply(State<S> state);

    /**
     * Parse the input state, extract the result and apply one of the supplied functions.
     * @return a parse result
     */
    default Reply<S, A> parse(State<S> state) {
        return apply(state).getReply();
    }

    // Helper functions to allow combinators to be chained in a fluent style.

    default <B> Parser<S, B> bind(Function<A, Parser<S, B>> f) {
        return Combinators.bind(this, f);
    }

    default <B> Parser<S, B> then(Parser<S, B> p) {
        return Combinators.then(this, p);
    }

    default Parser<S, A> or(Parser<S, A> q) {
        return Combinators.or(this, q);
    }

    default Parser<S, A> label(String name) {
        return Combinators.label(this, name);
    }

    default Parser<S, A> attempt() {
        return Combinators.attempt(this);
    }

    default Parser<S, A> option(A x) {
        return Combinators.option(this, x);
    }

    default Parser<S, Optional<A>> optionalOpt() {
        return Combinators.optionalOpt(this);
    }

    default Parser<S, Void> optional() {
        return Combinators.optional(this);
    }

    default <OPEN, CLOSE> Parser<S, A> between(Parser<S, OPEN> open, Parser<S, CLOSE> close) {
        return Combinators.between(open, close, this);
    }

    default Parser<S, IList<A>> many() {
        return Combinators.many(this);
    }

    default Parser<S, IList<A>> many1() {
        return Combinators.many1(this);
    }

    default Parser<S, Void> skipMany() {
        return Combinators.skipMany(this);
    }

    default Parser<S, Void> skipMany1() {
        return Combinators.skipMany1(this);
    }

    default <SEP> Parser<S, IList<A>> sepBy(Parser<S, SEP> sep) {
        return Combinators.sepBy(this, sep);
    }

    default <SEP> Parser<S, IList<A>> sepBy1 (Parser<S, SEP> sep) {
        return Combinators.sepBy1(this, sep);
    }

    default <SEP> Parser<S, IList<A>> sepEndBy(Parser<S, SEP> sep) {
        return Combinators.sepEndBy(this, sep);
    }

    default <SEP> Parser<S, IList<A>> sepEndBy1 (Parser<S, SEP> sep) {
        return Combinators.sepEndBy1(this, sep);
    }

    default Parser<S, A> chainl1(Parser<S, BinaryOperator<A>> op) {
        return Combinators.chainl1(this, op);
    }
}
