ParsecJ
============

(Translating Haskell into Java)

# Introduction

**ParsecJ** is a Java monadic parser combinator framework for constructing LL(1) parsers.
It is based on the
[Parsec paper](http://research.microsoft.com/en-us/um/people/daan/download/papers/parsec-paper.pdf),
which describes a monadic parsing framework implemented in Haskell.

## Parser Combinators

The standard approach to implementing parsers for special-purpose languages
is to use a parser generation tool,
such as Yacc/Bison or ANTLR.
With these tools the language is defined using a grammar language specific to the tool.
The parsing code is then generated from the language grammar.
An alternative approach is to implement a
[recursive descent parser](http://en.wikipedia.org/wiki/Recursive_descent_parser),
whereby the production rules comprising your language grammar
are translated into parse functions.
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

    intr.bind(x ->
        satisfy('+')
            .then(intr.bind(y ->
                retn(x+y))))

Here a parser is defined, which will parse and evaluate expressions of the form *a+b* where *a* and *b* are integers.
The parser is constructed by taking the `intr` parser for integers, the `satisfy` parser for single symbols,
and combining them using the `bind`, `then` and `retn` combinators.

This parser can be used as follows:

    int i =
        intr.bind(x ->
            satisfy('+')
                .then(intr.bind(y ->
                    retn(x+y)
                )
            )
        ).parse(State.of("1+2")).reply().getResult();

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

The `org.javafp.parsecj.Combinators` package provides the basic set of combinators:

|
