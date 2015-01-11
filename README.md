ParsecJ
============

# Introduction

**ParsecJ** is a Java monadic parser combinator framework for constructing LL(1) parsers.
It is based on the
[Parsec paper](http://research.microsoft.com/en-us/um/people/daan/download/papers/parsec-paper.pdf),
which describes a monadic parsing framework implemented in Haskell.

## Parser Combinators

The standard approach to implementing parsers for special-purpose languages
is to use a parser generation tool,
such as Yacc/Bison and ANTLR.
With these tools the language is described using a grammar language specific to the tool.
The parsing code is then generated from the language grammar.

An alternative approach is to implement a
[recursive descent parser](http://en.wikipedia.org/wiki/Recursive_descent_parser),
whereby the production rules comprising the grammar
are translated by hand into parse functions.
One limitation of this approach
is that the extra plumbing required to implement error-handling and backtracking
obscures the correspondence between the parsing functions and the language rules.

[Monadic parser combinators](http://www.artima.com/pins1ed/combinator-parsing.html)
are an extension of recursive descent parsing,
which use a monad to encapsulate the plumbing.
The framework provides the basic building blocks -
parsers for constituent language elements such as characters, words and numbers.
It also provides combinators which construct more complex parsers by composing existing parsers.
The framework effectively provides a Domain Specific Language for expressing language grammars,
whereby each grammar instance implements an executable parser.

## Example

As a quick illustration of how a simple parser looks when implemented using ParsecJ,
consider the following example.

```java
intr.bind(x ->
    satisfy('+')
        .then(intr.bind(y ->
            retn(x+y))))
```

Here a parser is defined which will parse and evaluate expressions of the form *a+b* where *a* and *b* are integers.
The parser is constructed by taking the `intr` parser for integers, the `satisfy` parser for single symbols,
and combining them using the `bind`, `then` and `retn` combinators.

This parser can be used as follows:

```java
int i =
    intr.bind(x ->
        satisfy('+')
            .then(intr.bind(y ->
                retn(x+y)
            )
        )
    ).parse(State.of("1+2")).getResult();
// i is now 3.
```

# Usage

Typically parsers are defined by composing the predefined combinators provided by the library.
In rare cases a parser combinator may need to be implemented by operating directly on the input state.

## Types

### Parser<S, A>

All parsers implement the `org.javafp.parsecj.Parser` interface,
which has an `apply` method :

```java
@FunctionalInterface
public interface Parser<S, A> {
    ConsumedT<S, A> apply(State<S> state);

    default Reply<S, A> parse(State<S> state) {
        return apply(state).getReply();
    }
    // ...
}
```

I.e. a `Parser<S, A>` is essentially a function from a `State<S>` to a `ConsumedT<S, A>`,
where `S` is the input stream symbol type (usually `Character`),
and `A` is the type of the value being parsed.
For example, a parser which operates on character input and parses an integer would have type `Parser<Character, Integer>`.

The `apply` method contains the main machinery of the parser,
and combinators use this method to compose parsers.
However, since the `ConsumedT` type returned by `apply` is an intermediate type,
the `parse` method is provided to apply and the parser and extract the `Reply` parse result.

### State<S>

The `State<S>` interface is an abstraction representing an immutable input state.
It provides several static `of` methods for constructing `State` instances from sequences of symbols:

```java
public interface State<S> {
    static <S> State<S> of(S[] symbols) {
        return new ArrayState<S>(symbols);
    }

    static State<Character> of(Character[] symbols) {
        return new CharArrayState(symbols);
    }

    static State<Character> of(String symbols) {
        return new StringState(symbols);
    }

    // ...
}
```

### Reply<S, A>

The `ConsumedT<S, A>` object returned by `Parser.apply` is an intermediate result wrapper,
which is typically only of interest to combinator implementations.
The `ConsumedT.getReply` method returns the parser result wrapper,
or the `Parser.parse` method can be used to bypass `ConsumedT` entirely.

A `Reply` can be either a successful parse result (represented by the `Ok` subtype)
or an error (represented by the `Error` subtype).
Use the `match` method to handle both cases:

```java
public abstract class Reply<S, A> {
    public abstract <B> B match(Function<Ok<S, A>, B> ok, Function<Error<S, A>, B> error);
    // ...
}
```

E.g.:

```java
String msg =
    parser.parse("abcd")
        .match(
            ok -> "Result : " + ok.getResult(),
            error -> "Error : " + error.getMsg()
        );
```

## Defining Parsers

A parser for a language is constructed by translating the production rules comprising the language grammar into parsers,
using the combinators provided by the library.

### Combinators

The `org.javafp.parsecj.Combinators` package provides the following basic combinator parsers:

Name | Description | Returns
-----|-------------|--------
`retn(value)` | A parser which always succeeds | The supplied value.
`bind(p, f)` | A parser which first applies the parser `p`. If it succeeds it then applies the function `f` to the result to `fail()` | A parser which always fails. | An Error
`satisfy(test)` | Applies a test to the next input symbol. | The symbol.
`satisfy(value)` | A parser which succeeds if the next input symbol equals `value`. | The symbol
`eof()` | A parser which succeeds if the end of the input is reached. | UNIT.
yield another parser which is then applied. | Result of `q`
`then(p, q)` | A parser which first applies the parser `p`. If it succeeds it then applies parser `q`. | Result of `q`.
`or(p, q)` | A parser which first applies the parser `p`. If it succeeds the result is returned otherwise it applies parser `q`. | Result of succeeding parser.
... |

Combinators which take a `Parser` as a first parameter, such as `bind` and `or`,
also exist as methods on the `Parser` interface, to allow parsers to be constructed in a fluent style.
E.g. `p.bind(f)` is equivalent to `bind(p, f)`.

### Text

The `org.javafp.parsecj.Text` package provides in addition to the parsers in `Combinators`,
the following parsers specialised for parsing text input:

Name | Description | Returns
-----|-------------|--------
`alpha` | A parser which succeeds if the next character is alphabetic. | The character.
`digit` | A parser which succeeds if the next character is a digit. | The character.
`intr` | A parser which parses an integer. | The integer.
`dble` | A parser which parses an double. | The double.
`string(s)` | A parser which parses the supplied string. | The string.
`alphaNum` | A parser which parses an alphanumeric string. | The string.
`regex(regex)` | A parser which parses a string matching the supplied regex. | The string matching the regex.

### Parser Monad

The `retn` and `bind` combinators are slightly special as they are what make `Parser` a monad.
The key point is that they observe the [3 monad laws](https://www.haskell.org/haskellwiki/Monad_laws):

1. **Left Identity** : `retn(a).bind(f)` = `f.apply(a)`
1. **Right Identity** : `p.bind(x -> retn(x)` = `p`
1. **Associativity** : `p.bind(f).bind(g)` = `p.bind(x -> f.apply(x).bind(g))`

where `p` and `q` are parsers, `a` is a parse result, and `f` a function from a parse result to a parser.

The first two laws tell us that `retn` is the identity the `bind` operation.
The third law tells us that when we have three parser expressions being combined with `bind`,
the order in which the expressions are evaluated has no effect on the result.
This becomes relevant when using the fluent chaining,
as it means we do not to worry too much about bracketing when chaining parsers.
The intent becomes clearer if we add some redundant brackets to the law:

`(p.bind(f)).bind(g)` = `p.bind(x -> (f.apply(x).bind(g)))`

It is analgous to associativity of addition over numbers,
where *a+b+c* yields the same result regardless of whether we evaluate it as *(a+b)+c* or *a+(b+c)*.

Also of note is the `fail` parser, which is a monadic zero,
since if combined with any other parser the result is always a parser that fails.

# Example

The `test/org.javafp.parsecj.expr.Grammar` class provides a simple illustration of how this library can be used,
by implementing a parser for simple mathematical expressions.

The grammar for this language is as follows:

```
expr      ::= number | binOpExpr
binOpExpr ::= '(' expr binOp expr ')'
binOp     ::= '+' | '-' | '*' | '/'
```

Valid expressions conforming to this language include:

```
1
(1.2+3.4)
((1.2*3.4)+5.6)
```

Typically parsers will construct values using a set of model classes corresponding to the language elements.
To jeek the example simple the parsers for this language will simply compute the evaluated result of each expression.
I.e. numbers will be parsed into their values,
operators will be parsed into binary functions,
and binary operator expressions will be parsed into the evaluated rresult of the expression.

The above grammar can be translated into the following Java implementation:

```java
// Forward declare expr to allow for circular references.
final Parser.Ref<Character, Double> expr = Parser.Ref.of();

// binOp ::= '+' | '-' | '*' | '/'
final Parser<Character, BinaryOperator<Double>> binOp =
    choice(
        chr('+').then(Combinators.<Character, BinaryOperator<Double>>retn((l, r) -> l + r)),
        chr('-').then(Combinators.<Character, BinaryOperator<Double>>retn((l, r) -> l - r)),
        chr('*').then(Combinators.<Character, BinaryOperator<Double>>retn((l, r) -> l * r)),
        chr('/').then(Combinators.<Character, BinaryOperator<Double>>retn((l, r) -> l / r))
    );

// binOpExpr ::= '(' expr binOp expr ')'
final Parser<Character, Double> binOpExpr =
    chr('(')
        .then(expr.bind(
            l -> binOp.bind(
                op -> expr.bind(
                    r -> chr(')')
                        .then(retn(op.apply(l, r)))))));

// expr ::= dble | binOpExpr
expr.set(choice(dble, binOpExpr));

final Parser<Character, Void> end = eof();
final Parser<Character, Double> parser = expr.bind(d -> end.then(retn(d)));
```

**Notes**
* The expression language is recursive - `expr` refers to `binOpExpr` which refers to `expr`. Since Java doesn't allow us to define a mutually recursive set of variables, we have to break the circularity by making the `expr` a `Parser.Ref`, which gets declared at the beginning and initalised at the end. `Ref` implements the `Parser` interface, hence it can be used as a parser.
* In some cases Java's type inference can't infer the types of expressions - the four operator parsers comprising `binOp` for instance. Here we have to provide the compiler with a type hint to allow the expression to compile - `Combinators.<Character, BinaryOperator<Double>>retn`).
* We add the `eof` parser, which succeeds if it encounters the end of the input, to bookend the `expr` parser. This ensures the parser does not incorrectly parse malformed inputs which begin with a valid expression, such as `(1+2)Z`.

# Translating Haskell into Java

This section describes how the Haskell code from the [Parsec paper](http://research.microsoft.com/en-us/um/people/daan/download/papers/parsec-paper.pdf)
paper has been translated into Java.

## Section 3

Section 3 of the paper begins to describe the implementation of Parsec, with these three types:

```Haskell
type Parser a = String -> Consumed a
data Consumed a = Consumed (Reply a)
                | Empty (Reply a)
data Reply a = Ok a String | Error
```

Start with `Reply`, this is a discriminated union between an `Ok` and an `Error`.
We can model this in Java with a `Reply` base class (or interface),
with two sub-classes:

```Java
public abstract class Reply<A> {
    public static <A> Ok<A> ok(A result, String rest) {
        return new Ok<A>(result, rest);
    }
        
    public static <A> Error<A> error() {
        return new Error<A>();
    }
    public abstract <B> B match(Function<Ok<A>, B> ok, Function<Error<A>, B> error);

    public static final class Ok<A> extends Reply<A> {

        public final A result;

        public final String rest;

        Ok(A result, String rest) {
            this.result = result;
            this.rest = rest;
        }

        @Override
        public <U> U match(Function<Ok<A>, U> ok, Function<Error<A>, U> error) {
            return ok.apply(this);
        }
        
        // Usual toString, equals etc.
    }

    public static final class Error<A> extends Reply<A> {

        Error() {}

        @Override
        public <B> B match(Function<Ok<A>, B> ok, Function<Error<A>, B> error) {
            return error.apply(this);
        }
        
        // Usual toString, equals etc.
    }
}
```

The `match` method provides a means to simulate Haskell's pattern-matching.
We can use it here to extract the result from a `Reply`:

```Java
<A> A getResult(Reply<A> reply) {
    return reply.match(
        ok -> ok.result,
        error -> {throw new RuntimeException("Error");}
    );
}
```

The `Consumed` type could in theory be handled in a similar fashion,
however there are two subtleties to be dealt with.
1. The Haskell code uses the name `Consumed` for both the type and the type constructor -
in Java we chose to call the former `ConsumedT` to distinguish it from the latter.
1. We learn further on in the document that Parsec relies on the `Consumed` type constructor being lazy (as is standard in Haskell). In order to simulate this in Java we need to make the `Consumed` class lazily constructed, using a `Supplier` instance:

```Java
public interface ConsumedT<S, A> {

    public static <S, A> ConsumedT<S, A> consumed(Supplier<Reply<S, A>> supplier) {
        return new Consumed<S, A>(supplier);
    }

    public static <S, A> ConsumedT<S, A> empty(Reply<S, A> reply) {
        return new Empty<S, A>(reply);
    }

    public boolean isConsumed();

    public Reply<S, A> getReply();

    public default <B> ConsumedT<S, B> cast() {
        return (ConsumedT<S, B>)this;
    }
}

final class Consumed<S, A> implements ConsumedT<S, A> {

    // Lazy Reply supplier.
    private Supplier<Reply<S, A>> supplier;

    // Lazy-initialised Reply.
    private Reply<S, A> reply;

    Consumed(Supplier<Reply<S, A>> supplier) {
        this.supplier = supplier;
    }

    @Override
    public boolean isConsumed() {
        return true;
    }

    @Override
    public Reply<S, A> getReply() {
        if (supplier != null) {
            reply = supplier.get();
            supplier = null;
        }

        return reply;
    }
}

final class Empty<S, A> implements ConsumedT<S, A> {

    private final Reply<S, A> reply;

    Empty(Reply<S, A> reply) {
        this.reply = reply;
    }

    @Override
    public boolean isConsumed() {
        return false;
    }

    @Override
    public Reply<S, A> getReply() {
        return reply;
    }
}
```
The final of the three Haskell types is `Parser a`,
which is a function from `String` to `Consumed a`.
We can model this as a functional interface in Java (Java 8 that is):

```Java
@FunctionalInterface
public interface Parser<A> {
    ConsumedT<A> apply(String state);
}
```

Since `Parser` is a functional interface we can construct `Parser` instances using the concise lambda syntax:

```Java
    Parser<Integer> p = s -> { ... };
```

## Section 3.1 - Basic combinators

Section 3.1 of the paper outlines the implementation of some basic combinators.

The `return` combinator:

```Haskell
return x
= \input -> Empty (Ok x input)
```

has to be renamed in Java as `return` is a reserved word, however the definition otherwise maps fairly easily:

```Java
public static <A> Parser<A> retn(A x) {
    return s -> ConsumedT.empty(Reply.ok(x, s));
}


```
# Related Work
