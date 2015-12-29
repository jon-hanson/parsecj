package org.javafp.parsecj.json;

import org.javafp.data.*;
import org.javafp.parsecj.*;

import java.util.*;

import static org.javafp.parsecj.Combinators.*;
import static org.javafp.parsecj.Text.*;

/**
 * A grammar for JSON.
 * Adapted from the Haskell Parsec-based JSON parser:
 * https://hackage.haskell.org/package/json
 */
public class Grammar {
    private static <T> Parser<Character, T> tok(Parser<Character, T> p) {
        return p.bind(x -> wspaces.then(retn(x)));
    }

    private static final Parser.Ref<Character, Node> jvalue = Parser.ref();

    private static final Parser<Character, Node> jnull = tok(string("null")).then(retn(Node.nul())).label("null");

    private static final Parser<Character, Boolean> jtrue = tok(string("true").then(retn(Boolean.TRUE)));
    private static final Parser<Character, Boolean> jfalse = tok(string("false").then(retn(Boolean.FALSE)));

    private static final Parser<Character, Node> jbool = tok(jtrue.or(jfalse).bind(b -> retn(Node.bool(b)))).label("boolean");

    private static final Parser<Character, Node> jnumber = tok(dble.bind(d -> retn(Node.number(d)))).label("number");

    private static final Parser<Character, Byte> hexDigit =
        satisfy((Character c) -> Character.digit(c, 16) != -1)
            .bind(c -> retn((byte) Character.digit(c, 16))).label("hex digit");

    private static final Parser<Character, Character> uni =
        hexDigit.bind(d0 ->
            hexDigit.bind(d1 ->
                hexDigit.bind(d2 ->
                    hexDigit.bind(d3 ->
                        retn((d0<<0x3) & (d1<<0x2) & (d2<<0x1) & d0)
                    )
                )
            )
        ).bind(i -> retn((char) i.intValue()));

    private static final Parser<Character, Character> esc =
        choice(
            chr('"'),
            chr('\\'),
            chr('/'),
            chr('b').then(retn('\b')),
            chr('f').then(retn('\f')),
            chr('n').then(retn('\n')),
            chr('r').then(retn('\r')),
            chr('t').then(retn('\t')),
            chr('u').then(uni)
            ).label("escape character");

    private static final Parser<Character, Character> stringChar =
        (
            chr('\\').then(esc)
        ).or(
            satisfy(c -> c != '"' && c != '\\')
        );

    private static final Parser<Character, String> jstring =
        tok(between(
            chr('"'),
            chr('"'),
            many(stringChar).bind(l -> retn(IList.listToString(l)))
        )).label("string");

    private static final Parser<Character, Node> jtext =
        jstring.bind(s ->
            retn(Node.text(s))
        ).label("text");

    private static final Parser<Character, Node> jarray =
        between(
            tok(chr('[')),
            tok(chr(']')),
            sepBy(
                jvalue,
                tok(chr(','))
            )
        ).bind(l ->
            retn(Node.array(IList.toList(l)))
        ).label("array");

    private static LinkedHashMap<String, Node> toMap(IList<Map.Entry<String, Node>> fields) {
        final LinkedHashMap<String, Node> map = new LinkedHashMap<String, Node>();
        fields.forEach(field -> map.put(field.getKey(), field.getValue()));
        return map;
    }

    private static final Parser<Character, Map.Entry<String, Node>> jfield =
        jstring.bind(name ->
            tok(chr(':'))
                .then(jvalue)
                .bind(value ->
                    retn(new AbstractMap.SimpleEntry<>(name, value))
                )
        );

    private static final Parser<Character, Node> jobject =
        between(
            tok(chr('{')),
            tok(chr('}')),
            sepBy(
                jfield,
                tok(chr(','))
            ).bind(lf -> retn(Node.object(toMap(lf))))
        ).label("object");

    static {
        jvalue.set(
            choice(
                jnull,
                jbool,
                jnumber,
                jtext,
                jarray,
                jobject
            ).label("JSON value")
        );
    }

    public static final Parser<Character, Node> parser = wspaces.then(jvalue);

    public static Reply<Character, Node> parse(String str) {
        return parser.parse(Input.of(str));
    }
}
