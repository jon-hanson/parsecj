package org.javafp.parsecj;

import org.javafp.data.*;

import java.util.Optional;
import java.util.function.*;

import static org.javafp.parsecj.ConsumedT.*;
import static org.javafp.parsecj.Merge.*;

/**
 * A collection of parser combinator functions.
 * The {@link Parser} type along with <code>retn</code> &amp; <code>bind</code> constitute a monad.
 */
public abstract class Combinators {

    /**
     * Construct an {@link Error} {@link Reply} indicating the end of the input has been reached.
     * @param input     the current input state
     * @param expected  the expected rule
     * @param <S>       the input symbol type
     * @param <A>       the parser value type
     * @return          the Error object
     */
    public static <S, A> Reply<S, A> endOfInput(State<S> input, String expected) {
        return Reply.<S, A>error(
            Message.lazy(() -> Message.endOfInput(input.position(), expected))
        );
    }

    /**
     * Monadic return function, i.e. a parser which returns the supplied value.
     * @param x         the value for the parser to return
     * @param <S>       the input symbol type
     * @param <A>       the parser value type
     * @return          the parser
     */
    public static <S, A> Parser<S, A> retn(A x) {
        return input ->
            empty(
                Reply.ok(
                    x,
                    input,
                    Message.lazy(() -> Message.of(input.position()))
                )
            );
    }

    /**
     * Monadic bind function.
     * Bind chains two parsers by creating a parser which calls the first,
     * and if that parser succeeds the resultant value is passed to the
     * function argument to obtain a second parser, which is then invoked.
     * @param p         a parser which is called first
     * @param f         a function which is passed the result of the first parser if successful,
     *                  and which returns a second parser
     * @param <S>       the input symbol type
     * @param <A>       the first parser value type
     * @param <B>       the second parser value type
     * @return          the parser
     */
    public static <S, A, B> Parser<S, B> bind(
            Parser<S, ? extends A> p,
            Function<A, Parser<S, B>> f) {
        return input -> {
            final ConsumedT<S, ? extends A> cons1 = p.apply(input);
            if (cons1.isConsumed()) {
                return consumed(() ->
                    cons1.getReply().<Reply<S, B>>match(
                        ok1 -> {
                            final ConsumedT<S, B> cons2 = f.apply(ok1.result).apply(ok1.rest);
                            return cons2.getReply();
                        },
                        error -> error.cast()
                    )
                );
            } else {
                return cons1.getReply().<ConsumedT<S, B>>match(
                    ok1 -> {
                        final ConsumedT<S, B> cons2 = f.apply(ok1.result).apply(ok1.rest);
                        if (cons2.isConsumed()) {
                            return cons2;
                        } else {
                            return cons2.getReply().match(
                                ok2 -> mergeOk(ok2.result, ok2.rest, ok1.msg, ok2.msg),
                                error -> Merge.<S, B>mergeError(ok1.msg, error.msg)
                            );
                        }
                    },
                    error -> empty(error.cast())
                );
            }
        };
    }

    /**
     * Apply the first parser, then apply the second parser and return the result.
     * This is an optimisation for <code>bind(p, x -&gt; q)</code> - i.e. a parser which discards <code>x</code>,
     * the result of the first parser <code>p</code>.
     * @param p         the first parser
     * @param q         the second parser
     * @param <S>       the input symbol type
     * @param <A>       the first parser value type
     * @param <B>       the second parser value type
     * @return          the parser
     */
    public static <S, A, B> Parser<S, B> then(Parser<S, ? extends A> p, Parser<S, B> q) {
        return input -> {
            final ConsumedT<S, ? extends A> cons1 = p.apply(input);
            if (cons1.isConsumed()) {
                return consumed(() ->
                        cons1.getReply().<Reply<S, B>>match(
                            ok1 -> {
                                final ConsumedT<S, B> cons2 = q.apply(ok1.rest);
                                return cons2.getReply();
                            },
                            error -> error.cast()
                        )
                );
            } else {
                return cons1.getReply().<ConsumedT<S, B>>match(
                    ok1 -> {
                        final ConsumedT<S, B> cons2 = q.apply(ok1.rest);
                        if (cons2.isConsumed()) {
                            return cons2;
                        } else {
                            return cons2.getReply().match(
                                ok2 -> mergeOk(ok2.result, ok2.rest, ok1.msg, ok2.msg),
                                error2 -> Merge.<S, B>mergeError(ok1.msg, error2.msg)
                            );
                        }
                    },
                    error -> cons1.cast()
                );
            }
        };
    }

    /**
     * A parser which always fails
     * @param <S>       the input symbol type
     * @param <A>       the parser value type
     * @return          the parser
     */
    public static <S, A> Parser<S, A> fail() {
        return input ->
            empty(
                Reply.error(
                    Message.lazy(() -> Message.of(input.position()))
                )
            );
    }

    private static final String eofName = "EOF";

    /**
     * A parser which succeeds if the end of the input is reached.
     * @param <S>       the input symbol type
     * @return          the parser
     */
    public static <S> Parser<S, Void> eof() {
        return input -> {
            if (input.end()) {
                return empty(
                    Reply.ok(
                        input,
                        Message.lazy(() -> Message.of(input.position(), eofName))
                    )
                );
            } else {
                return empty(
                    Reply.<S, Void>error(
                        Message.lazy(() -> Message.of(input.position(), eofName))
                    )
                );
            }
        };
    }

    private static final String testName = "<test>";

    /**
     * A parser which succeeds if the next symbol passes the predicate <code>test</code>.
     * The parser returns the symbol.
     * @param test      predicate to be applied to the next symbol
     * @param <S>       the input symbol type
     * @return          the parser
     */
    public static <S> Parser<S, S> satisfy(Predicate<S> test) {
        return input -> {
            if (!input.end()) {
                final S s = input.current();
                if (test.test(s)) {
                    final State<S> newState = input.next();
                    return consumed(() -> Reply.ok(
                            s,
                            newState,
                            Message.lazy(() -> Message.of(input.position()))
                        )
                    );
                } else {
                    return empty(
                        Reply.error(
                            Message.lazy(() -> Message.of(input.position(), input.current(), testName))
                        )
                    );
                }
            } else {
                return empty(endOfInput(input, testName));
            }
        };
    }

    /**
     * A parser which succeeds if the next input symbol equals the supplied value.
     * The parser returns the symbol.
     * @param value     the value to be compared against
     * @param <S>       the input symbol type
     * @return          the parser
     */
    public static <S> Parser<S, S> satisfy(S value) {
        return label(satisfy(v -> value.equals(v)), value.toString());
    }

    /**
     * A parser which succeeds if the next input symbol equals the supplied value.
     * @param value     the value to be compared against
     * @param result    the value the parser returns if it succeeds
     * @param <S>       the input symbol type
     * @param <A>       the parser value type
     * @return          the parser
     */
    public static <S, A> Parser<S, A> satisfy(S value, A result) {
        return satisfy(value).then(retn(result));
    }

    /**
     * A parser first tries parser <code>p</code>.
     * If it succeeds or consumes input then its result is returned.
     * Otherwise return the result of applying parser <code>q</code>.
     * @param p         first parser to try with
     * @param q         second parser to try with
     * @param <S>       the input symbol type
     * @param <A>       the parser value type
     * @return          the parser
     */
    public static <S, A> Parser<S, A> or(Parser<S, A> p, Parser<S, A> q) {
        return input -> {
            final ConsumedT<S, A> cons1 = p.apply(input);
            if (cons1.isConsumed()) {
                return cons1;
            } else {
                return cons1.getReply().match(
                    ok1 -> {
                        final ConsumedT<S, A> cons2 = q.apply(input);
                        if (cons2.isConsumed()) {
                            return cons2;
                        } else {
                            return mergeOk(ok1.result, ok1.rest, ok1.msg, cons2.getReply().msg);
                        }
                    },
                    error1 -> {
                        final ConsumedT<S, A> cons2 = q.apply(input);
                        if (cons2.isConsumed()) {
                            return cons2;
                        } else {
                            return cons2.getReply().match(
                                ok2 -> mergeOk(ok2.result, ok2.rest, error1.msg, ok2.msg),
                                error2 -> Merge.<S, A>mergeError(error1.msg, error2.msg)
                            );
                        }
                    }
                );
            }
        };
    }

    /**
     * Label a parser with a readable name for more meaningful error messages.
     * @param p         the parser to be labelled
     * @param name      the label (this will appear in the list of expected rules in the event of a failure)
     * @param <S>       the input symbol type
     * @param <A>       the parser value type
     * @return          the parser
     */
    public static <S, A> Parser<S, A> label(Parser<S, A> p, String name) {
        return input -> {
            final ConsumedT<S, A> cons = p.apply(input);
            if (cons.isConsumed()) {
                return cons;
            } else {
                return cons.getReply().match(
                    ok -> empty(Reply.ok(ok.result, ok.rest, ok.msg.expect(name))),
                    error -> empty(Reply.error((error.msg.expect(name))))
                );
            }
        };
    }

    /**
     * A combinator which turns a parser which consumes input
     * into one which doesn't consume input if it fails.
     * This allows the implementation of LL(∞) grammars.
     * @param p         the parser
     * @param <S>       the input symbol type
     * @param <A>       the parser value type
     * @return          the parser
     */
    public static <S, A> Parser<S, A> attempt(Parser<S, A> p) {
        return input -> {
            final ConsumedT<S, A> cons = p.apply(input);
            if (cons.isConsumed()) {
                return cons.getReply().match(
                    ok -> cons,
                    error -> empty(error)
                );
            } else {
                return cons;
            }
        };
    }

    /**
     * choice tries to apply the parsers in the list <code>ps</code> in order,
     * until one of them succeeds. Returns the value of the succeeding
     * parser
     * @param ps        the list of parsers
     * @param <S>       the input symbol type
     * @param <A>       the parser value type
     * @return          the parser
     */
    public static <S, A> Parser<S, A> choice(IList<Parser<S, A>> ps) {
        if (ps.tail().isEmpty()) {
            return ps.head();
        } else {
            return or(ps.head(), choice(ps.tail()));
        }
    }

    /**
     * choice tries to apply the parsers in the list <code>ps</code> in order,
     * until one of them succeeds. It returns the value of the succeeding
     * parser
     * @param ps        the list of parsers
     * @param <S>       the input symbol type
     * @param <A>       the parser value type
     * @return          the parser
     */
    public static <S, A> Parser<S, A> choice(Parser<S, A>... ps) {
        return choice(IList.of(ps));
    }

    /**
     * A parser which attempts parser <code>p</code> first and if it fails then return <code>x</code>.
     * @param p         the parser
     * @param x         value to be returned if <code>p</code> fails
     * @param <S>       the input symbol type
     * @param <A>       the parser value type
     * @return          the parser
     */
    public static <S, A> Parser<S, A> option(Parser<S, A> p, A x) {
        return or(p, retn(x));
    }

    /**
     * A parser for optional values.
     * Applies parser <code>p</code> and if it succeeds returns the result in in {@link Optional},
     * otherwise return <code>Optional.empty()</code>.
     * @param p         the parser
     * @param <S>       the input symbol type
     * @param <A>       the parser value type
     * @return          the parser
     */
    public static <S, A> Parser<S, Optional<A>> optionalOpt(Parser<S, A> p) {
        return option(
            p.bind(x -> retn(Optional.of(x))),
            Optional.empty()
        );
    }

    /**
     * A parser for optional values, which throws the result away.
     * @param p         the parser
     * @param <S>       the input symbol type
     * @return          the parser
     */
    public static <S, A> Parser<S, Unit> optional(Parser<S, A> p) {
        return or(bind(p, x -> retn(Unit.unit)), retn(Unit.unit));
    }

    /**
     * A parser which parses an opening symbol, then applies parser <code>p</code>, then parses a closing symbol,
     * and returns the result of <code>p</code>.
     * @param open      parser for the open symbol
     * @param close     parser for the closing symbol
     * @param p         the parser for the content
     * @param <S>       the input symbol type
     * @param <A>       the parser value type
     * @param <OPEN>    the open parser value type
     * @param <CLOSE>   the close parser value type
     * @return          the parser
     */
    public static <S, A, OPEN, CLOSE> Parser<S, A> between(
            Parser<S, OPEN> open,
            Parser<S, CLOSE> close,
            Parser<S, A> p) {
        return then(open, bind(p, a -> then(close, retn(a))));
    }

    /**
     * A parser for a list of zero or more values of the same type.
     * @param p         the parser
     * @param <S>       the input symbol type
     * @param <A>       the parser value type
     * @return          the parser
     */
    public static <S, A> Parser<S, IList<A>> many(Parser<S, A> p) {
        return manyLoop(p, IList.of());
    }

    /**
     * A parser for a list of one or more values of the same type.
     * @param p         the parser
     * @param <S>       the input symbol type
     * @param <A>       the parser value type
     * @return          the parser
     */
    public static <S, A> Parser<S, IList<A>> many1(Parser<S, A> p) {
        return bind(p, x -> manyLoop(p, IList.of(x)));
    }

//    private static <S, A> Parser<S, IList<A>> manyAccOld(Parser<S, A> p, IList<A> acc) {
//        return or(bind(p, x -> manyAccOld(p, acc.add(x))), retn(acc.reverse()));
//    }

    private static <S, A> Parser<S, IList<A>> manyLoop(Parser<S, A> p, IList<A> acc) {
        return manyLoop(p, acc, -1);
    }

    private static <S, A> Parser<S, IList<A>> manyLoop(Parser<S, A> p, final IList<A> acc, int count) {
        return input -> {
            IList<A> acc2 = acc;
            boolean consumed = false;
            boolean done = false;
            int i = 0;
            for (; !done && i != count; ++i) {
                final ConsumedT<S, A> cons = p.apply(input);
                if (cons.isConsumed()) {
                    consumed = true;
                    final Reply<S, A> reply = cons.getReply();
                    if (reply.isOk()) {
                        final Reply.Ok<S, A> ok = (Reply.Ok<S, A>)reply;
                        acc2 = acc2.add(ok.result);
                        input = ok.rest;
                    } else {
                        return cons.cast();
                    }
                } else {
                    final Reply<S, A> reply = cons.getReply();
                    if (reply.isOk()) {
                        final Reply.Ok<S, A> ok = (Reply.Ok<S, A>)reply;
                        acc2 = acc2.add(ok.result);
                        input = ok.rest;
                    } else {
                        done = true;
                        --i;
                    }
                }
            }
            final IList<A> acc3 = acc2;
            final State<S> input2 = input;
            if (count == -1 || i == count) {
                return ConsumedT.of(
                    consumed,
                    () -> Reply.ok(
                        acc3.reverse(),
                        input2,
                        Message.lazy(() -> Message.of(input2.position()))
                    )
                );
            } else {
                return ConsumedT.of(
                    consumed,
                    () -> Reply.error(
                        Message.lazy(() -> Message.of(input2.position()))
                    )
                );
            }
        };
    }

    /**
     * A parser which applies the parser <code>p</code> zero or more times, throwing away the result.
     * @param p         the parser
     * @param <S>       the input symbol type
     * @param <A>       the parser value type
     * @return          the parser
     */
    public static <S, A> Parser<S, Unit> skipMany(Parser<S, A> p) {
        return input -> {
            boolean consumed = false;
            while (true) {
                final ConsumedT<S, A> cons = p.apply(input);
                if (cons.isConsumed()) {
                    consumed = true;
                    final Reply<S, A> reply = cons.getReply();
                    if (reply.isOk()) {
                        final Reply.Ok<S, A> ok = (Reply.Ok<S, A>)reply;
                        input = ok.rest;
                    } else {
                        return cons.cast();
                    }
                } else {
                    final Reply<S, A> reply = cons.getReply();
                    if (reply.isOk()) {
                        final Reply.Ok<S, A> ok = (Reply.Ok<S, A>)reply;
                        input = ok.rest;
                    } else {
                        final State<S> input2 = input;
                        return ConsumedT.of(
                            consumed,
                            () -> Reply.ok(
                                Unit.unit,
                                input2,
                                Message.lazy(() -> Message.of(input2.position()))
                            )
                        );
                    }
                }
            }
        };
    }

    /**
     * A parser which applies the parser <code>p</code> one or more times, throwing away the result.
     * @param p         the parser
     * @param <S>       the input symbol type
     * @return          the parser
     */
    public static <S, A> Parser<S, Unit> skipMany1(Parser<S, A> p) {
        return then(p, skipMany(p));
    }

    /**
     * A parser which parses zero or more occurrences of <code>p</code>, separated by <code>sep</code>,
     * and returns a list of values returned by <code>p</code>.
     * @param p         parser
     * @param sep       parser for the separator
     * @param <S>       the input symbol type
     * @param <A>       the parser value type
     * @return          the parser
     */
    public static <S, A, SEP> Parser<S, IList<A>> sepBy(
            Parser<S, A> p,
            Parser<S, SEP> sep) {
        return or(sepBy1(p, sep), retn(IList.of()));
    }

    /**
     * A parser which parses one or more occurrences of <code>p</code>, separated by <code>sep</code>,
     * and returns a list of the values returned by <code>p</code>.
     * @param p         parser
     * @param sep       parser for the separator
     * @param <S>       the input symbol type
     * @param <A>       the parser value type
     * @return          the parser
     */
    public static <S, A, SEP> Parser<S, IList<A>> sepBy1(
            Parser<S, A> p,
            Parser<S, SEP> sep) {
        return bind(
            p,
            x -> bind(
                many(then(sep, p)),
                xs -> retn(xs.add(x))
            )
        );
    }

    /**
     * A parser which parses zero or more occurrences of <code>p</code>, separated by <code>sep</code>,
     * and optionally ended by sep,
     * and returns a list of the values returned by <code>p</code>.
     * @param p         parser
     * @param sep       parser for the separator
     * @param <S>       the input symbol type
     * @param <A>       the parser value type
     * @return          the parser
     */
    public static <S, A, SEP> Parser<S, IList<A>> sepEndBy(
            Parser<S, A> p,
            Parser<S, SEP> sep) {
        return or(sepEndBy1(p, sep), retn(IList.of()));
    }

    /**
     * A parser which parses one or more occurrences of <code>p</code>, separated by <code>sep</code>,
     * and optionally ended by sep,
     * and returns a list of the values returned by <code>p</code>.
     * @param p         parser
     * @param sep       parser for the separator
     * @param <S>       the input symbol type
     * @param <A>       the parser value type
     * @return          the parser
     */
    public static <S, A, SEP> Parser<S, IList<A>> sepEndBy1(
            Parser<S, A> p,
            Parser<S, SEP> sep) {
        return bind(
            p,
            x -> or(
                then(
                    sep,
                    bind(
                        sepEndBy(p, sep),
                        xs -> retn(xs.add(x))
                    )
                ),
                retn(IList.of(x))
            )
        );
    }

    /**
     * A parser which parses zero or more occurrences of <code>p</code>, separated and ended by <code>sep</code>,
     * and ended by sep,
     * and returns a list of the values returned by <code>p</code>.
     * @param p         parser
     * @param sep       parser for the separator
     * @param <S>       the input symbol type
     * @param <A>       the parser value type
     * @return          the parser
     */
    public static <S, A, SEP> Parser<S, IList<A>> endBy(
            Parser<S, A> p,
            Parser<S, SEP> sep) {
        return many(bind(p, x -> then(sep, retn(x))));
    }

    /**
     * A parser which parses one or more occurrences of <code>p</code>, separated by <code>sep</code>,
     * and ended by sep,
     * and returns a list of values returned by p.
     * @param p         parser
     * @param sep       parser for the separator
     * @param <S>       the input symbol type
     * @param <A>       the parser value type
     * @return          the parser
     */
    public static <S, A, SEP> Parser<S, IList<A>> endBy1(
            Parser<S, A> p,
            Parser<S, SEP> sep) {
        return many1(bind(p, x -> then(sep, retn(x))));
    }

    /**
     * A parser which applies parser <code>p</code> <code>n</code> times.
     * @param p         parser
     * @param n         number of times to apply <code>p</code>
     * @param <S>       the input symbol type
     * @param <A>       the parser value type
     * @return          the parser
     */
    public static <S, A> Parser<S, IList<A>> count(
            Parser<S, A> p,
            int n) {
        return manyLoop(p, IList.of(), n);
    }

    private static <S, A> Parser<S, IList<A>> countAcc(
            Parser<S, A> p,
            int n,
            IList<A> acc) {
        if (n == 0) {
            return retn(acc.reverse());
        } else {
            return bind(p, x -> countAcc(p, n - 1, acc.add(x)));
        }
    }

    /**
     * A parser for an operand followed by zero or more operands (<code>p</code>)
     * separated by right-associative operators (<code>op</code>).
     * If there are zero operands then <code>x</code> is returned.
     * @param p         parser
     * @param op        parser for the operand
     * @param x         value to return if there are no operands
     * @param <S>       the input symbol type
     * @param <A>       the parser value type
     * @return          the parser
     */
    public static <S, A> Parser<S, A> chainr(
            Parser<S, A> p,
            Parser<S, BinaryOperator<A>> op,
            A x) {
        return or(chainr1(p, op), retn(x));
    }

    /**
     * A parser for an operand followed by zero or more operands (<code>p</code>)
     * separated by left-associative operators (<code>op</code>).
     * If there are zero operands then <code>x</code> is returned.
     * @param p         parser
     * @param op        parser for the operand
     * @param x         value to return if there are no operands
     * @param <S>       the input symbol type
     * @param <A>       the parser value type
     * @return          the parser
     */
    public static <S, A> Parser<S, A> chainl(
            Parser<S, A> p,
            Parser<S, BinaryOperator<A>> op,
            A x) {
        return or(chainl1(p, op), retn(x));
    }

    /**
     * A parser for an operand followed by one or more operands (<code>p</code>)
     * separated by right-associative operators (<code>op</code>).
     * @param p         parser
     * @param op        parser for the operand
     * @param <S>       the input symbol type
     * @param <A>       the parser value type
     * @return          the parser
     */
    public static <S, A> Parser<S, A> chainr1(
            Parser<S, A> p,
            Parser<S, BinaryOperator<A>> op) {
        return scanr1(p, op);
    }

    private static <S, A> Parser<S, A> scanr1(
            Parser<S, A> p,
            Parser<S, BinaryOperator<A>> op) {
        return bind(p, x -> restr1(p, op, x));
    }

    private static <S, A> Parser<S, A> restr1(
            Parser<S, A> p,
            Parser<S, BinaryOperator<A>> op,
            A x) {
        return or(
            bind(
                op,
                f -> bind(
                    scanr1(p, op),
                    y -> retn(f.apply(x, y))
                )
            ),
            retn(x)
        );
    }

    /**
     * A parser for an operand followed by one or more operands (<code>p</code>)
     * separated by operators (<code>op</code>).
     * This parser can for example be used to eliminate left recursion which typically occurs in expression grammars.
     * @param p         parser
     * @param op        parser for the operand
     * @param <S>       the input symbol type
     * @param <A>       the parser value type
     * @return          the parser
     */
    public static <S, A> Parser<S, A> chainl1(
            Parser<S, A> p,
            Parser<S, BinaryOperator<A>> op) {
        return bind(p, x -> restl1(p, op, x));
    }

    private static <S, A> Parser<S, A> restl1(
            Parser<S, A> p,
            Parser<S, BinaryOperator<A>> op,
            A x) {
        return or(
            bind(
                op,
                f -> bind(
                    p,
                    y -> restl1(
                        p,
                        op,
                        f.apply(x, y)
                    )
                )
            ),
            retn(x)
        );
    }
}
