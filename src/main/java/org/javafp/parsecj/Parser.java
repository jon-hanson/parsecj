package org.javafp.parsecj;

import org.javafp.data.*;

import java.util.Optional;
import java.util.function.*;

/**
 * A parser is essentially a function taking the input stream and returning a ConsumedT.
 * The Parser type along with the retn and bind functions constitute a monad.
 * @param <I> Input stream symbol type.
 * @param <A> Parse result type
 */
@FunctionalInterface
public interface Parser<I, A> {

    static <I, A> Parser<I, A> parser(Function<Input<I>, ConsumedT<I, A>> parser) {
        return parser::apply;
    }

    static <I, A> Ref<I, A> ref() {
        return new Ref<I, A>();
    }

    static <I, A> Ref<I, A> ref(Parser<I, A> parser) {
        return new Ref<I, A>(parser);
    }

    /**
     * A lazily initialised reference to a Parser.
     */
    class Ref<I, A> implements Supplier<Parser<I, A>>, Parser<I, A> {

        private Parser<I, A> parser;

        private Ref(Parser<I, A> parser) {
            this.parser = parser;
        }

        private Ref() {
            this.parser = null;
        }

        public Parser<I, A> set(Parser<I, A> parser) {
            this.parser = parser;
            return this;
        }

        @Override
        public synchronized Parser<I, A> get() {
            if (parser == null) {
                throw new RuntimeException("Null Parser Reference");
            }
            return parser;
        }

        @Override
        public ConsumedT<I, A> apply(Input<I> input) {
            return get().apply(input);
        }
    }

    /**
     * Parse the input state
     * @return a ConsumedT result
     */
    ConsumedT<I, A> apply(Input<I> input);

    /**
     * Parse the input state, extract the result and apply one of the supplied functions.
     * @return a parse result
     */
    default Reply<I, A> parse(Input<I> input) {
        return
            apply(input)
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
    default <B> Parser<I, B> bind(Function<A, Parser<I, B>> f) {
        return Combinators.bind(this, f);
    }

    /**
     * @see Combinators#then
     */
    default <B> Parser<I, B> then(Parser<I, B> p) {
        return Combinators.then(this, p);
    }

    /**
     * @see Combinators#map
     */
    default <B> Parser<I, B> map(Function<A, B> f) {
        return Combinators.map(this, f);
    }

    /**
     * @see Combinators#or
     */
    default Parser<I, A> or(Parser<I, ? extends A> q) {
        return Combinators.or(this, q);
    }

    /**
     * @see Combinators#label
     */
    default Parser<I, A> label(String name) {
        return Combinators.label(this, name);
    }

    /**
     * @see Combinators#attempt
     */
    default Parser<I, A> attempt() {
        return Combinators.attempt(this);
    }

    /**
     * @see Combinators#option
     */
    default Parser<I, A> option(A x) {
        return Combinators.option(this, x);
    }

    /**
     * @see Combinators#optionalOpt
     */
    default Parser<I, Optional<A>> optionalOpt() {
        return Combinators.optionalOpt(this);
    }

    /**
     * @see Combinators#optional
     */
    default Parser<I, Unit> optional() {
        return Combinators.optional(this);
    }

    /**
     * @see Combinators#between
     */
    default <OPEN, CLOSE> Parser<I, A> between(Parser<I, OPEN> open, Parser<I, CLOSE> close) {
        return Combinators.between(open, close, this);
    }

    /**
     * @see Combinators#many
     */
    default Parser<I, IList<A>> many() {
        return Combinators.many(this);
    }

    /**
     * @see Combinators#many1
     */
    default Parser<I, IList<A>> many1() {
        return Combinators.many1(this);
    }

    /**
     * @see Combinators#skipMany
     */
    default Parser<I, Unit> skipMany() {
        return Combinators.skipMany(this);
    }

    /**
     * @see Combinators#skipMany1
     */
    default Parser<I, Unit> skipMany1() {
        return Combinators.skipMany1(this);
    }

    /**
     * @see Combinators#sepBy
     */
    default <SEP> Parser<I, IList<A>> sepBy(Parser<I, SEP> sep) {
        return Combinators.sepBy(this, sep);
    }

    /**
     * @see Combinators#sepBy1
     */
    default <SEP> Parser<I, IList<A>> sepBy1(Parser<I, SEP> sep) {
        return Combinators.sepBy1(this, sep);
    }

    /**
     * @see Combinators#sepEndBy
     */
    default <SEP> Parser<I, IList<A>> sepEndBy(Parser<I, SEP> sep) {
        return Combinators.sepEndBy(this, sep);
    }

    /**
     * @see Combinators#sepEndBy1
     */
    default <SEP> Parser<I, IList<A>> sepEndBy1(Parser<I, SEP> sep) {
        return Combinators.sepEndBy1(this, sep);
    }

    /**
     * @see Combinators#endBy
     */
    default <SEP> Parser<I, IList<A>> endBy(Parser<I, SEP> sep) {
        return Combinators.endBy(this, sep);
    }

    /**
     * @see Combinators#endBy1
     */
    default <SEP> Parser<I, IList<A>> endBy1(Parser<I, SEP> sep) {
        return Combinators.endBy1(this, sep);
    }

    /**
     * @see Combinators#count
     */
    default Parser<I, IList<A>> count(int n) {
        return Combinators.count(this, n);
    }

    /**
     * @see Combinators#chainr
     */
    default Parser<I, A> chainr(Parser<I, BinaryOperator<A>> op, A x) {
        return Combinators.chainr(this, op, x);
    }

    /**
     * @see Combinators#chainr1
     */
    default Parser<I, A> chainr1(Parser<I, BinaryOperator<A>> op) {
        return Combinators.chainr1(this, op);
    }

    /**
     * @see Combinators#chainl
     */
    default Parser<I, A> chainl(Parser<I, BinaryOperator<A>> op, A x) {
        return Combinators.chainl(this, op, x);
    }

    /**
     * @see Combinators#chainl1
     */
    default Parser<I, A> chainl1(Parser<I, BinaryOperator<A>> op) {
        return Combinators.chainl1(this, op);
    }
}
