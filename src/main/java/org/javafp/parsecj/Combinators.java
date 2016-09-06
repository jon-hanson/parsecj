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
     * @param <I>       the input symbol type
     * @param <A>       the parser value type
     * @return          the Error object
     */
    public static <I, A> Reply<I, A> endOfInput(Input<I> input, String expected) {
        return Reply.<I, A>error(
            Message.lazy(() -> Message.endOfInput(input.position(), expected))
        );
    }

    /**
     * Monadic return function, i.e. a parser which returns the supplied value.
     * @param x         the value for the parser to return
     * @param <I>       the input symbol type
     * @param <A>       the parser value type
     * @return          the parser
     */
    public static <I, A> Parser<I, A> retn(A x) {
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
     * @param <I>       the input symbol type
     * @param <A>       the first parser value type
     * @param <B>       the second parser value type
     * @return          the parser
     */
    public static <I, A, B> Parser<I, B> bind(
            Parser<I, ? extends A> p,
            Function<A, Parser<I, B>> f) {
        return input -> {
            final ConsumedT<I, ? extends A> cons1 = p.apply(input);
            if (cons1.isConsumed()) {
                return consumed(() ->
                    cons1.getReply().<Reply<I, B>>match(
                        ok1 -> {
                            final ConsumedT<I, B> cons2 = f.apply(ok1.result).apply(ok1.rest);
                            return cons2.getReply();
                        },
                        error -> error.cast()
                    )
                );
            } else {
                return cons1.getReply().<ConsumedT<I, B>>match(
                    ok1 -> {
                        final ConsumedT<I, B> cons2 = f.apply(ok1.result).apply(ok1.rest);
                        if (cons2.isConsumed()) {
                            return cons2;
                        } else {
                            return cons2.getReply().match(
                                ok2 -> mergeOk(ok2.result, ok2.rest, ok1.msg, ok2.msg),
                                error -> Merge.<I, B>mergeError(ok1.msg, error.msg)
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
     * @param <I>       the input symbol type
     * @param <A>       the first parser value type
     * @param <B>       the second parser value type
     * @return          the parser
     */
    public static <I, A, B> Parser<I, B> then(Parser<I, ? extends A> p, Parser<I, B> q) {
        return input -> {
            final ConsumedT<I, ? extends A> cons1 = p.apply(input);
            if (cons1.isConsumed()) {
                return consumed(() ->
                        cons1.getReply().<Reply<I, B>>match(
                            ok1 -> {
                                final ConsumedT<I, B> cons2 = q.apply(ok1.rest);
                                return cons2.getReply();
                            },
                            error -> error.cast()
                        )
                );
            } else {
                return cons1.getReply().<ConsumedT<I, B>>match(
                    ok1 -> {
                        final ConsumedT<I, B> cons2 = q.apply(ok1.rest);
                        if (cons2.isConsumed()) {
                            return cons2;
                        } else {
                            return cons2.getReply().match(
                                ok2 -> mergeOk(ok2.result, ok2.rest, ok1.msg, ok2.msg),
                                error2 -> Merge.<I, B>mergeError(ok1.msg, error2.msg)
                            );
                        }
                    },
                    error -> cons1.cast()
                );
            }
        };
    }

    /**
     * Convert a parser which throws a Message.Exception into one
     * which converts the exception into an Consumed error.
     * @param p         the unsafe parser
     * @param <I>
     * @param <A>
     * @return          safe parser
     */
    public static <I, A> Parser<I, A> safe(Parser<I, A> p) {
        return input -> {
            try {
                return p.apply(input);
            } catch (Message.Exception ex) {
                return ConsumedT.empty(
                    Reply.error(ex.message)
                );
            }
        };
    }

    /**
     * Monadic return function, i.e. a parser which returns the supplied value.
     * @param supplier  the value for the parser to return
     * @param <I>       the input symbol type
     * @param <A>       the parser value type
     * @return          the parser
     */
    public static <I, A> Parser<I, A> safeRetn(Supplier<A> supplier, String expected) {
        return input -> {
            try {
                return empty(
                    Reply.ok(
                        supplier.get(),
                        input,
                        Message.lazy(() -> Message.of(input.position()))
                    )
                );
            } catch (Exception ex) {
                return ConsumedT.empty(
                    Reply.error(Message.of(input.position(), input.current(), expected))
                );
            }
        };
    }

    /**
     * A parser which always fails
     * @param <I>       the input symbol type
     * @param <A>       the parser value type
     * @return          the parser
     */
    public static <I, A> Parser<I, A> fail() {
        return input ->
            empty(
                Reply.error(
                    Message.lazy(() -> Message.of(input.position()))
                )
            );
    }

    /**
     * A parser which always fails
     * @param msg       error message
     * @param <I>       the input symbol type
     * @param <A>       the parser value type
     * @return          the parser
     */
    public static <I, A> Parser<I, A> fail(String msg) {
        return input ->
            empty(
                Reply.error(
                    Message.lazy(() -> Message.of(msg, input.position()))
                )
            );
    }

    private static final String eofName = "EOF";

    /**
     * A parser which succeeds if the end of the input is reached.
     * @param <I>       the input symbol type
     * @return          the parser
     */
    public static <I> Parser<I, Unit> eof() {
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
                    Reply.<I, Unit>error(
                        Message.lazy(() -> Message.of(input.position(), input.current(), eofName))
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
     * @param <I>       the input symbol type
     * @return          the parser
     */
    public static <I> Parser<I, I> satisfy(Predicate<I> test) {
        return input -> {
            if (!input.end()) {
                final I s = input.current();
                if (test.test(s)) {
                    final Input<I> newInput = input.next();
                    return consumed(
                        () -> Reply.ok(
                            s,
                            newInput,
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
     * @param <I>       the input symbol type
     * @return          the parser
     */
    public static <I> Parser<I, I> satisfy(I value) {
        return label(satisfy(value::equals), value.toString());
    }

    /**
     * A parser which succeeds if the next input symbol equals the supplied value.
     * @param value     the value to be compared against
     * @param result    the value the parser returns if it succeeds
     * @param <I>       the input symbol type
     * @param <A>       the parser value type
     * @return          the parser
     */
    public static <I, A> Parser<I, A> satisfy(I value, A result) {
        return satisfy(value).then(retn(result));
    }

    /**
     * A parser first tries parser <code>p</code>.
     * If it succeeds or consumes input then its result is returned.
     * Otherwise return the result of applying parser <code>q</code>.
     * @param p         first parser to try with
     * @param q         second parser to try with
     * @param <I>       the input symbol type
     * @param <A>       the parser value type
     * @return          the parser
     */
    public static <I, A> Parser<I, A> or(Parser<I, ? extends A> p, Parser<I, ? extends A> q) {
        return input -> {
            final ConsumedT<I, ? extends A> cons1 = p.apply(input);
            if (cons1.isConsumed()) {
                return cons1.cast();
            } else {
                return cons1.getReply().match(
                    ok1 -> {
                        final ConsumedT<I, ? extends A> cons2 = q.apply(input);
                        if (cons2.isConsumed()) {
                            return cons2.cast();
                        } else {
                            return mergeOk(ok1.result, ok1.rest, ok1.msg, cons2.getReply().msg).cast();
                        }
                    },
                    error1 -> {
                        final ConsumedT<I, ? extends A> cons2 = q.apply(input);
                        if (cons2.isConsumed()) {
                            return cons2.cast();
                        } else {
                            return cons2.getReply().match(
                                ok2 -> mergeOk(ok2.result, ok2.rest, error1.msg, ok2.msg),
                                error2 -> Merge.<I, A>mergeError(error1.msg, error2.msg)
                            ).cast();
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
     * @param <I>       the input symbol type
     * @param <A>       the parser value type
     * @return          the parser
     */
    public static <I, A> Parser<I, A> label(Parser<I, A> p, String name) {
        return input -> {
            final ConsumedT<I, A> cons = p.apply(input);
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
     * @param <I>       the input symbol type
     * @param <A>       the parser value type
     * @return          the parser
     */
    public static <I, A> Parser<I, A> attempt(Parser<I, A> p) {
        return input -> {
            final ConsumedT<I, A> cons = p.apply(input);
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
     * @param <I>       the input symbol type
     * @param <A>       the parser value type
     * @return          the parser
     */
    public static <I, A> Parser<I, A> choice(IList<Parser<I, A>> ps) {
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
     * @param p1        first parser
     * @param p2        second parser
     * @param <I>       the input symbol type
     * @param <A>       the parser value type
     * @return          the parser
     */
    public static <I, A> Parser<I, A> choice(
            Parser<I, ? extends A> p1,
            Parser<I, ? extends A> p2) {
        return or(p1, p2);
    }

    /**
     * choice tries to apply the parsers in the list <code>ps</code> in order,
     * until one of them succeeds. It returns the value of the succeeding
     * parser
     * @param p1        first parser
     * @param p2        second parser
     * @param p3        third parser
     * @param <I>       the input symbol type
     * @param <A>       the parser value type
     * @return          the parser
     */
    public static <I, A> Parser<I, A> choice(
            Parser<I, ? extends A> p1,
            Parser<I, ? extends A> p2,
            Parser<I, ? extends A> p3) {
        return or(p1, or(p2, p3));
    }

    /**
     * choice tries to apply the parsers in the list <code>ps</code> in order,
     * until one of them succeeds. It returns the value of the succeeding
     * parser
     * @param p1        first parser
     * @param p2        second parser
     * @param p3        third parser
     * @param p4        fourth parser
     * @param <I>       the input symbol type
     * @param <A>       the parser value type
     * @return          the parser
     */
    public static <I, A> Parser<I, A> choice(
            Parser<I, ? extends A> p1,
            Parser<I, ? extends A> p2,
            Parser<I, ? extends A> p3,
            Parser<I, ? extends A> p4) {
        return or(p1, or(p2, or(p3, p4)));
    }

    /**
     * choice tries to apply the parsers in the list <code>ps</code> in order,
     * until one of them succeeds. It returns the value of the succeeding
     * parser
     * @param p1        first parser
     * @param p2        second parser
     * @param p3        third parser
     * @param p4        fourth parser
     * @param p5        fifth parser
     * @param <I>       the input symbol type
     * @param <A>       the parser value type
     * @return          the parser
     */
    public static <I, A> Parser<I, A> choice(
            Parser<I, ? extends A> p1,
            Parser<I, ? extends A> p2,
            Parser<I, ? extends A> p3,
            Parser<I, ? extends A> p4,
            Parser<I, ? extends A> p5) {
        return or(p1, or(p2, or(p3, or(p4, p5))));
    }

    /**
     * choice tries to apply the parsers in the list <code>ps</code> in order,
     * until one of them succeeds. It returns the value of the succeeding
     * parser
     * @param p1        first parser
     * @param p2        second parser
     * @param p3        third parser
     * @param p4        fourth parser
     * @param p5        fifth parser
     * @param p6        sixth parser
     * @param <I>       the input symbol type
     * @param <A>       the parser value type
     * @return          the parser
     */
    public static <I, A> Parser<I, A> choice(
            Parser<I, ? extends A> p1,
            Parser<I, ? extends A> p2,
            Parser<I, ? extends A> p3,
            Parser<I, ? extends A> p4,
            Parser<I, ? extends A> p5,
            Parser<I, ? extends A> p6) {
        return or(p1, or(p2, or(p3, or(p4, or(p5, p6)))));
    }

    /**
     * choice tries to apply the parsers in the list <code>ps</code> in order,
     * until one of them succeeds. It returns the value of the succeeding
     * parser
     * @param p1        first parser
     * @param p2        second parser
     * @param p3        third parser
     * @param p4        fourth parser
     * @param p5        fifth parser
     * @param p6        sixth parser
     * @param p7        seventh parser
     * @param <I>       the input symbol type
     * @param <A>       the parser value type
     * @return          the parser
     */
    public static <I, A> Parser<I, A> choice(
            Parser<I, ? extends A> p1,
            Parser<I, ? extends A> p2,
            Parser<I, ? extends A> p3,
            Parser<I, ? extends A> p4,
            Parser<I, ? extends A> p5,
            Parser<I, ? extends A> p6,
            Parser<I, ? extends A> p7) {
        return or(p1, or(p2, or(p3, or(p4, or(p5, or(p6, p7))))));
    }

    /**
     * choice tries to apply the parsers in the list <code>ps</code> in order,
     * until one of them succeeds. It returns the value of the succeeding
     * parser.
     * @param ps        the list of parsers
     * @param <I>       the input symbol type
     * @param <A>       the parser value type
     * @return          the parser
     */
    public static <I, A> Parser<I, A> choice(Parser<I, A>... ps) {
        return choice(IList.of(ps));
    }

    /**
     * A parser which attempts parser <code>p</code> first and if it fails then return <code>x</code>.
     * @param p         the parser
     * @param x         value to be returned if <code>p</code> fails
     * @param <I>       the input symbol type
     * @param <A>       the parser value type
     * @return          the parser
     */
    public static <I, A> Parser<I, A> option(Parser<I, A> p, A x) {
        return or(p, retn(x));
    }

    /**
     * A parser for optional values.
     * Applies parser <code>p</code> and if it succeeds returns the result in in {@link Optional},
     * otherwise return <code>Optional.empty()</code>.
     * @param p         the parser
     * @param <I>       the input symbol type
     * @param <A>       the parser value type
     * @return          the parser
     */
    public static <I, A> Parser<I, Optional<A>> optionalOpt(Parser<I, A> p) {
        return option(
            bind(p, x -> retn(Optional.of(x))),
            Optional.empty()
        );
    }

    public static <I, A> Parser<I, Boolean> optBool(Parser<I, A> p) {
        return option(
            bind(p, x -> retn(Boolean.TRUE)),
            Boolean.FALSE
        );
    }

    /**
     * A parser for optional values, which throws the result away.
     * @param p         the parser
     * @param <I>       the input symbol type
     * @return          the parser
     */
    public static <I, A> Parser<I, Unit> optional(Parser<I, A> p) {
        return or(bind(p, x -> retn(Unit.unit)), retn(Unit.unit));
    }

    /**
     * A parser which parses an opening symbol, then applies parser <code>p</code>, then parses a closing symbol,
     * and returns the result of <code>p</code>.
     * @param open      parser for the open symbol
     * @param close     parser for the closing symbol
     * @param p         the parser for the content
     * @param <I>       the input symbol type
     * @param <A>       the parser value type
     * @param <OPEN>    the open parser value type
     * @param <CLOSE>   the close parser value type
     * @return          the parser
     */
    public static <I, A, OPEN, CLOSE> Parser<I, A> between(
            Parser<I, OPEN> open,
            Parser<I, CLOSE> close,
            Parser<I, A> p) {
        return then(open, bind(p, a -> then(close, retn(a))));
    }

    /**
     * A parser for a list of zero or more values of the same type.
     * @param p         the parser
     * @param <I>       the input symbol type
     * @param <A>       the parser value type
     * @return          the parser
     */
    public static <I, A> Parser<I, IList<A>> many(Parser<I, A> p) {
        return manyLoop(p, IList.of());
    }

    /**
     * A parser for a list of one or more values of the same type.
     * @param p         the parser
     * @param <I>       the input symbol type
     * @param <A>       the parser value type
     * @return          the parser
     */
    public static <I, A> Parser<I, IList<A>> many1(Parser<I, A> p) {
        return bind(p, x -> manyLoop(p, IList.of(x)));
    }

    private static <I, A> Parser<I, IList<A>> manyLoop(Parser<I, A> p, IList<A> acc) {
        return manyLoop(p, acc, -1);
    }

    private static <I, A> Parser<I, IList<A>> manyLoop(Parser<I, A> p, final IList<A> acc, int count) {
        return input -> {
            IList<A> acc2 = acc;
            boolean consumed = false;
            boolean done = false;
            int i = 0;
            for (; !done && i != count; ++i) {
                final ConsumedT<I, A> cons = p.apply(input);
                if (cons.isConsumed()) {
                    consumed = true;
                    final Reply<I, A> reply = cons.getReply();
                    if (reply.isOk()) {
                        final Reply.Ok<I, A> ok = (Reply.Ok<I, A>)reply;
                        acc2 = acc2.add(ok.result);
                        input = ok.rest;
                    } else {
                        return cons.cast();
                    }
                } else {
                    final Reply<I, A> reply = cons.getReply();
                    if (reply.isOk()) {
                        final Reply.Ok<I, A> ok = (Reply.Ok<I, A>)reply;
                        acc2 = acc2.add(ok.result);
                        input = ok.rest;
                    } else {
                        done = true;
                        --i;
                    }
                }
            }
            final IList<A> acc3 = acc2;
            final Input<I> input2 = input;
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
     * @param <I>       the input symbol type
     * @param <A>       the parser value type
     * @return          the parser
     */
    public static <I, A> Parser<I, Unit> skipMany(Parser<I, A> p) {
        return input -> {
            boolean consumed = false;
            while (true) {
                final ConsumedT<I, A> cons = p.apply(input);
                if (cons.isConsumed()) {
                    consumed = true;
                    final Reply<I, A> reply = cons.getReply();
                    if (reply.isOk()) {
                        final Reply.Ok<I, A> ok = (Reply.Ok<I, A>)reply;
                        input = ok.rest;
                    } else {
                        return cons.cast();
                    }
                } else {
                    final Reply<I, A> reply = cons.getReply();
                    if (reply.isOk()) {
                        final Reply.Ok<I, A> ok = (Reply.Ok<I, A>)reply;
                        input = ok.rest;
                    } else {
                        final Input<I> input2 = input;
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
     * @param <I>       the input symbol type
     * @return          the parser
     */
    public static <I, A> Parser<I, Unit> skipMany1(Parser<I, A> p) {
        return then(p, skipMany(p));
    }

    /**
     * A parser which parses zero or more occurrences of <code>p</code>, separated by <code>sep</code>,
     * and returns a list of values returned by <code>p</code>.
     * @param p         parser
     * @param sep       parser for the separator
     * @param <I>       the input symbol type
     * @param <A>       the parser value type
     * @return          the parser
     */
    public static <I, A, SEP> Parser<I, IList<A>> sepBy(
            Parser<I, A> p,
            Parser<I, SEP> sep) {
        return or(sepBy1(p, sep), retn(IList.of()));
    }

    /**
     * A parser which parses one or more occurrences of <code>p</code>, separated by <code>sep</code>,
     * and returns a list of the values returned by <code>p</code>.
     * @param p         parser
     * @param sep       parser for the separator
     * @param <I>       the input symbol type
     * @param <A>       the parser value type
     * @return          the parser
     */
    public static <I, A, SEP> Parser<I, IList<A>> sepBy1(
            Parser<I, A> p,
            Parser<I, SEP> sep) {
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
     * @param <I>       the input symbol type
     * @param <A>       the parser value type
     * @return          the parser
     */
    public static <I, A, SEP> Parser<I, IList<A>> sepEndBy(
            Parser<I, A> p,
            Parser<I, SEP> sep) {
        return or(sepEndBy1(p, sep), retn(IList.of()));
    }

    /**
     * A parser which parses one or more occurrences of <code>p</code>, separated by <code>sep</code>,
     * and optionally ended by sep,
     * and returns a list of the values returned by <code>p</code>.
     * @param p         parser
     * @param sep       parser for the separator
     * @param <I>       the input symbol type
     * @param <A>       the parser value type
     * @return          the parser
     */
    public static <I, A, SEP> Parser<I, IList<A>> sepEndBy1(
            Parser<I, A> p,
            Parser<I, SEP> sep) {
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
     * @param <I>       the input symbol type
     * @param <A>       the parser value type
     * @return          the parser
     */
    public static <I, A, SEP> Parser<I, IList<A>> endBy(
            Parser<I, A> p,
            Parser<I, SEP> sep) {
        return many(bind(p, x -> then(sep, retn(x))));
    }

    /**
     * A parser which parses one or more occurrences of <code>p</code>, separated by <code>sep</code>,
     * and ended by sep,
     * and returns a list of values returned by p.
     * @param p         parser
     * @param sep       parser for the separator
     * @param <I>       the input symbol type
     * @param <A>       the parser value type
     * @return          the parser
     */
    public static <I, A, SEP> Parser<I, IList<A>> endBy1(
            Parser<I, A> p,
            Parser<I, SEP> sep) {
        return many1(bind(p, x -> then(sep, retn(x))));
    }

    /**
     * A parser which applies parser <code>p</code> <code>n</code> times.
     * @param p         parser
     * @param n         number of times to apply <code>p</code>
     * @param <I>       the input symbol type
     * @param <A>       the parser value type
     * @return          the parser
     */
    public static <I, A> Parser<I, IList<A>> count(
            Parser<I, A> p,
            int n) {
        return manyLoop(p, IList.of(), n);
    }

    /**
     * A parser for an operand followed by zero or more operands (<code>p</code>)
     * separated by right-associative operators (<code>op</code>).
     * If there are zero operands then <code>x</code> is returned.
     * @param p         parser
     * @param op        parser for the operand
     * @param x         value to return if there are no operands
     * @param <I>       the input symbol type
     * @param <A>       the parser value type
     * @return          the parser
     */
    public static <I, A> Parser<I, A> chainr(
            Parser<I, A> p,
            Parser<I, BinaryOperator<A>> op,
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
     * @param <I>       the input symbol type
     * @param <A>       the parser value type
     * @return          the parser
     */
    public static <I, A> Parser<I, A> chainl(
            Parser<I, A> p,
            Parser<I, BinaryOperator<A>> op,
            A x) {
        return or(chainl1(p, op), retn(x));
    }

    /**
     * A parser for an operand followed by one or more operands (<code>p</code>)
     * separated by right-associative operators (<code>op</code>).
     * @param p         parser
     * @param op        parser for the operand
     * @param <I>       the input symbol type
     * @param <A>       the parser value type
     * @return          the parser
     */
    public static <I, A> Parser<I, A> chainr1(
            Parser<I, A> p,
            Parser<I, BinaryOperator<A>> op) {
        return scanr1(p, op);
    }

    private static <I, A> Parser<I, A> scanr1(
            Parser<I, A> p,
            Parser<I, BinaryOperator<A>> op) {
        return bind(p, x -> restr1(p, op, x));
    }

    private static <I, A> Parser<I, A> restr1(
            Parser<I, A> p,
            Parser<I, BinaryOperator<A>> op,
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
     * @param <I>       the input symbol type
     * @param <A>       the parser value type
     * @return          the parser
     */
    public static <I, A> Parser<I, A> chainl1(
            Parser<I, A> p,
            Parser<I, BinaryOperator<A>> op) {
        return bind(p, x -> restl1(p, op, x));
    }

    private static <I, A> Parser<I, A> restl1(
            Parser<I, A> p,
            Parser<I, BinaryOperator<A>> op,
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
