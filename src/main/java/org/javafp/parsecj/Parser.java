package org.javafp.parsecj;

import org.javafp.data.*;

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

    public static <S, A> Parser<S, A> parser(Function<State<S>, ConsumedT<S, A>> parser) {
        return parser::apply;
    }

    static <S, A> Ref<S, A> ref() {
        return new Ref<S, A>();
    }

    static <S, A> Ref<S, A> ref(Parser<S, A> parser) {
        return new Ref<S, A>(parser);
    }

    /**
     * A lazily initialised reference to a Parser.
     */
    public static class Ref<S, A> implements Supplier<Parser<S, A>>, Parser<S, A> {

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
        return
            apply(state)
                .getReply()
                .match(
                    // Strip off the message if the parse was successful.
                    ok -> Reply.ok(ok.result, ok.rest, Message.of()),
                    error -> error
                );
    }

    // Helper functions to allow combinators to be chained in a fluent style.

    /**
     * @see Combinators#bind
     */
    default <B> Parser<S, B> bind(Function<A, Parser<S, B>> f) {
        return Combinators.bind(this, f);
    }

    /**
     * @see Combinators#then
     */
    default <B> Parser<S, B> then(Parser<S, B> p) {
        return Combinators.then(this, p);
    }

    /**
     * @see Combinators#or
     */
    default Parser<S, A> or(Parser<S, A> q) {
        return Combinators.or(this, q);
    }

    /**
     * @see Combinators#label
     */
    default Parser<S, A> label(String name) {
        return Combinators.label(this, name);
    }

    /**
     * @see Combinators#attempt
     */
    default Parser<S, A> attempt() {
        return Combinators.attempt(this);
    }

    /**
     * @see Combinators#option
     */
    default Parser<S, A> option(A x) {
        return Combinators.option(this, x);
    }

    /**
     * @see Combinators#optionalOpt
     */
    default Parser<S, Optional<A>> optionalOpt() {
        return Combinators.optionalOpt(this);
    }

    /**
     * @see Combinators#optional
     */
    default Parser<S, Unit> optional() {
        return Combinators.optional(this);
    }

    /**
     * @see Combinators#between
     */
    default <OPEN, CLOSE> Parser<S, A> between(Parser<S, OPEN> open, Parser<S, CLOSE> close) {
        return Combinators.between(open, close, this);
    }

    /**
     * @see Combinators#many
     */
    default Parser<S, IList<A>> many() {
        return Combinators.many(this);
    }

    /**
     * @see Combinators#many1
     */
    default Parser<S, IList<A>> many1() {
        return Combinators.many1(this);
    }

    /**
     * @see Combinators#skipMany
     */
    default Parser<S, Unit> skipMany() {
        return Combinators.skipMany(this);
    }

    /**
     * @see Combinators#skipMany1
     */
    default Parser<S, Unit> skipMany1() {
        return Combinators.skipMany1(this);
    }

    /**
     * @see Combinators#sepBy
     */
    default <SEP> Parser<S, IList<A>> sepBy(Parser<S, SEP> sep) {
        return Combinators.sepBy(this, sep);
    }

    /**
     * @see Combinators#sepBy1
     */
    default <SEP> Parser<S, IList<A>> sepBy1(Parser<S, SEP> sep) {
        return Combinators.sepBy1(this, sep);
    }

    /**
     * @see Combinators#sepEndBy
     */
    default <SEP> Parser<S, IList<A>> sepEndBy(Parser<S, SEP> sep) {
        return Combinators.sepEndBy(this, sep);
    }

    /**
     * @see Combinators#sepEndBy1
     */
    default <SEP> Parser<S, IList<A>> sepEndBy1(Parser<S, SEP> sep) {
        return Combinators.sepEndBy1(this, sep);
    }

    /**
     * @see Combinators#endBy
     */
    default <SEP> Parser<S, IList<A>> endBy(Parser<S, SEP> sep) {
        return Combinators.endBy(this, sep);
    }

    /**
     * @see Combinators#endBy1
     */
    default <SEP> Parser<S, IList<A>> endBy1(Parser<S, SEP> sep) {
        return Combinators.endBy1(this, sep);
    }

    /**
     * @see Combinators#count
     */
    default Parser<S, IList<A>> count(int n) {
        return Combinators.count(this, n);
    }

    /**
     * @see Combinators#chainr
     */
    default Parser<S, A> chainr(Parser<S, BinaryOperator<A>> op, A x) {
        return Combinators.chainr(this, op, x);
    }

    /**
     * @see Combinators#chainr1
     */
    default Parser<S, A> chainr1(Parser<S, BinaryOperator<A>> op) {
        return Combinators.chainr1(this, op);
    }

    /**
     * @see Combinators#chainl
     */
    default Parser<S, A> chainl(Parser<S, BinaryOperator<A>> op, A x) {
        return Combinators.chainl(this, op, x);
    }

    /**
     * @see Combinators#chainl1
     */
    default Parser<S, A> chainl1(Parser<S, BinaryOperator<A>> op) {
        return Combinators.chainl1(this, op);
    }
}
