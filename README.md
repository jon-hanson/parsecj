ParsecJ
============

# Introduction

**ParsecJ** is a Java monadic parser combinator framework for constructing LL(1) parsers.
It is based on the
[Parsec paper](http://research.microsoft.com/en-us/um/people/daan/download/papers/parsec-paper.pdf),
which describes a monadic parsing framework implemented in Haskell.
Note, the implementation of the Haskell Parsec library has changed considerably since the paper.

## Parser Combinators

The standard approach to implementing parsers for special-purpose languages
is to use a parser generation tool,
such as Yacc/Bison and ANTLR.
With these tools the language is defined using a grammar language specific to the tool.
The parsing code is then generated from the language grammar.

An alternative approach is to implement a
[recursive descent parser](http://en.wikipedia.org/wiki/Recursive_descent_parser),
whereby the production rules comprising your language grammar
are translated by hand into parse functions.
One limitation of this approach
is that the extra plumbing required to implement error-handling and backtracking
obscures the correspondence between the parsing functions and the language rules.

[Monadic parser combinators](http://www.artima.com/pins1ed/combinator-parsing.html)
are a extension of recursive descent parsing,
which use a monad to encapsulate the plumbing.
The framework provides the basic building blocks -
parsers for constituent language elements such as characters, words and numbers.
It also provides combinators which construct more complex parsers by composing existing parsers.
The framework effectively provides a Domain Specific Language for expressing language grammars,
whereby each grammar instance implements an executable parser.

## Example

As a quick illustration of how this looks using ParsecJ, consider the following example.

```java
intr.bind(x ->
    satisfy('+')
        .then(intr.bind(y ->
            retn(x+y))))
```

Here a parser is defined, which will parse and evaluate expressions of the form *a+b* where *a* and *b* are integers.
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
    ).parse(State.of("1+2")).reply().getResult();
```

# Usage

`org.javafp.parsecj.Parser` is the primary interface. In essence it is as follows:

```java
/**
 * @param <S> Input stream symbol type.
 * @param <A> Parse result type
 */
@FunctionalInterface
public interface Parser<S, A> {
    ConsumedT<S, A> parse(State<S> state);
}
```

I.e. a `Parser<S, A>` is essentially a function from a `State<S>` to a `ConsumedT<T, A>`.
The `State<S>` interface is an abstraction representing an immutable input state,
where `S` is the symbol type (typically `Character`).
`ConsumedT<T, A>` is an intermediate result wrapper which has a `getReply()` method to obtain the parse result:

```java
/**
 * A Parser result, essentially a discriminated union between a Success and an Error.
 * @param <S> Input stream symbol type.
 * @param <A> Parse result type
 */
public abstract class Reply<S, A> {

    public abstract <B> B match(Function<Ok<S, A>, B> ok, Function<Error<S, A>, B> error);

    public abstract A getResult() throws Exception;
}
```

Since the Reply can be either a successful parse result (represented by the `Ok` type)
or an error (represented by the `Error` type),
use the `match` method to handle both cases:

```java
String msg = parser.parse("abcd").getReply().match(
            ok -> "Result : " + ok.getResult(),
            error -> "Error : " + error.getMsg()
        );
```

## Defining a Parser

The `org.javafp.parsecj.Combinators` package provides the following basic parsers:

Name | Description
-----|------------
`Combinators.satisfy(test)` | A parser which applies a test to the next input symbol. |
`Combinators.satisfy(value)` | A parser which succeeds if the next input symbol equals `value`. |
`Combinators.eof()` | A parser which succeeds if the end of the input is reached. |
`Combinators.fail()` | A parser which always fails. |
`Text.alpha` | A parser which succeeds if the next character is alphabetic. |
`Text.digit` | A parser which succeeds if the next character is a digit. |
`Text.intr` | A parser which parses an integer. |
`Text.dble` | A parser which parses an double. |
`Text.satisfy(s)` | A parser which parses the supplied string. |
`Text.alphaNum` | A parser which parses an alphanumeric string. |
`Text.regex(regex)` | A parser which parses a string matching the supplied regex. |

and the following combinator parsers:

Name | Description
-----|------------
`Combinators.retn(value)` | A parser which simply returns the supplied value
`Combinators.bind(p, f)` | A parser which first applies the parser `p`. If it succeeds it then applies the function `f` to the result to yield another parser which is then applied.
`Combinators.then(p, q)` | A parser which first applies the parser `p`. If it succeeds it then applies parser `q`.
`Combinators.or(p, q)` | A parser which first applies the parser `p`. If it succeeds the result is returned otherwise it applies parser `q`.
...

The `org.javafp.parsecj.Text` package provides in addition to the above, the following parsers specialised for parsing text input:

Name | Description
-----|------------
`Text.alpha` | A parser which succeeds if the next character is alphabetic. |
`Text.digit` | A parser which succeeds if the next character is a digit. |
`Text.intr` | A parser which parses an integer. |
`Text.dble` | A parser which parses an double. |
`Text.satisfy(s)` | A parser which parses the supplied string. |
`Text.alphaNum` | A parser which parses an alphanumeric string. |
`Text.regex(regex)` | A parser which parses a string matching the supplied regex. |
