package org.javafp.parsecj.json;

import org.javafp.data.IList;
import org.javafp.parsecj.*;

import java.util.LinkedHashMap;

import static org.javafp.parsecj.Combinators.*;
import static org.javafp.parsecj.Text.*;

/**
 * A grammar for JSON.
 */
public class Grammar {
    private static <T> Parser<Character, T> tok(Parser<Character, T> p) {
        return p.bind(x -> wspaces.then(retn(x)));
    }

    private static Parser.Ref<Character, Node> jvalue = Parser.Ref.of();

    private static Parser<Character, Node> jnull = tok(string("null")).then(retn(Node.nul())).label("null");

    private static Parser<Character, Boolean> jtrue = tok(string("true").then(retn(Boolean.TRUE)));
    private static Parser<Character, Boolean> jfalse = tok(string("false").then(retn(Boolean.FALSE)));

    private static Parser<Character, Node> jbool = tok(jtrue.or(jfalse).bind(b -> retn(Node.bool(b)))).label("boolean");

    private static Parser<Character, Node> jnumber = tok(dble.bind(d -> retn(Node.number(d)))).label("number");

    private static Parser<Character, Byte> hexDigit =
        satisfy((Character c) -> Character.digit(c, 16) != -1)
            .bind(c -> retn((byte) Character.digit(c, 16))).label("hex digit");

    private static Parser<Character, Character> uni =
        hexDigit.bind(
            d0 -> hexDigit.bind(
                d1 -> hexDigit.bind(
                    d2 -> hexDigit.bind(
                        d3 -> retn((d0<<0x3) & (d1<<0x2) & (d2<<0x1) & d0)))))
            .bind(i -> retn((char) i.intValue()));

    private static Parser<Character, Character> esc =
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

    private static Parser<Character, Character> stringChar =
        (
            chr('\\').then(esc)
        ).or(
            satisfy(c -> c != '"' && c != '\\')
        );

    private static Parser<Character, String> jstring =
        tok(between(
            chr('"'),
            chr('"'),
            many(stringChar).bind(l -> retn(IList.listToString(l)))
        ));

    private static Parser<Character, Node> jtext =
        jstring.bind(s -> retn(Node.text(s))).label("text");

    private static Parser<Character, Node> jarray =
        between(
            tok(chr('[')),
            tok(chr(']')),
            sepBy(
                jvalue,
                tok(chr(','))
            )
        ).bind(l -> retn(Node.array(IList.toJList(l))))
            .label("array");

    private static class Field {
        static Field of(String name, Node value) {
            return new Field(name, value);
        }

        static LinkedHashMap<String, Node> toMap(IList<Field> fields) {
            final LinkedHashMap<String, Node> map = new LinkedHashMap<String, Node>();
            fields.forEach(field -> map.put(field.name, field.value));
            return map;
        }

        final String name;
        final Node value;

        private Field(String name, Node value) {
            this.name = name;
            this.value = value;
        }
    }

    private static Parser<Character, Field> jfield =
        jstring.bind(
            name -> tok(chr(':'))
                .then(jvalue)
                .bind(value -> retn(Field.of(name, value)))
        );

    private static Parser<Character, Node> jobject =
        between(
            tok(chr('{')),
            tok(chr('}')),
            sepBy(
                jfield,
                tok(chr(','))
            ).bind(lf -> retn(Node.object(Field.toMap(lf))))
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

    public static Reply<Character, Node> parse(String str) {
        return wspaces.then(jvalue).parse(State.of(str)).getReply();
    }
}
