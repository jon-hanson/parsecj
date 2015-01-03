package org.javafp.parsecj;

import org.javafp.data.List;

import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;

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

    public static <S, A> Reply<S, A> parse(Parser<S, A> parser, State<S> state) {
        return parser.parse(state).getReply();
    }

    /**
     * A lazily initialised reference to a Parser.
     */
    public static class Ref<S, A> implements Supplier<Parser<S, A>>, Parser<S, A> {

        public static <S, A> Ref<S, A> of() {
            return new Ref();
        }

        public static <S, A> Ref<S, A> of(Supplier<Parser<S, A>> supplier) {
            return new Ref(supplier);
        }

        private Supplier<Parser<S, A>> supplier;

        private Parser<S, A> value;

        private Ref(Supplier<Parser<S, A>> supplier) {
            this.supplier = supplier;
        }

        private Ref() {
            this.supplier = null;
        }

        public Parser<S, A> set(Supplier<Parser<S, A>> supplier) {
            this.supplier = supplier;
            return this;
        }

        @Override
        public synchronized Parser<S, A> get() {
            if (supplier != null) {
                value = supplier.get();
                supplier = null;
            } else {
                if (value == null) {
                    throw new RuntimeException("Null Parser Reference");
                }
            }
            return value;
        }

        @Override
        public ConsumedT<S, A> parse(State<S> input) {
            return get().parse(input);
        }
    }

    /**
     * Parse the input state
     * @return a ConsumedT result parse result
     */
    ConsumedT<S, A> parse(State<S> state);

    // Helper functions to call combinators in a fluent style.

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

    default Parser<S, A> tryP() {
        return Combinators.tryP(this);
    }

    default Parser<S, A> chainl1(Parser<S, BinaryOperator<A>> op) {
        return Combinators.chainl1(this, op);
    }

    default Parser<S, List<A>> many() {
        return Combinators.many(this);
    }

    default Parser<S, List<A>> many1() {
        return Combinators.many1(this);
    }

    default <SEP> Parser<S, List<A>> sepBy(Parser<S, SEP> sep) {
        return Combinators.sepBy(this, sep);
    }

    default <SEP> Parser<S, List<A>> sepBy1 (Parser<S, SEP> sep) {
        return Combinators.sepBy1(this, sep);
    }
}
