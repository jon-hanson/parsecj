package org.javafp.parsecj.json;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A model for JSON nodes.
 */
public interface Node {

    static Node nul() {
        return NullNode.instance;
    }

    static Node bool(boolean value) {
        return new BooleanNode(value);
    }

    static Node number(double value) {
        return new NumberNode(value);
    }

    static Node text(String value) {
        return new TextNode(value);
    }

    static Node array(List<Node> value) {
        return new ArrayNode(value);
    }

    static Node object(LinkedHashMap<String, Node> value) {
        return new ObjectNode(value);
    }

    final class NullNode implements Node {
        public static final NullNode instance = new NullNode();

        private NullNode() {
        }

        @Override
        public String toString() {
            return "null";
        }
    }

    final class BooleanNode implements Node {
        public final boolean value;

        public BooleanNode(boolean value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return Boolean.toString(value);
        }
    }

    final class NumberNode implements Node {
        public final double value;

        public NumberNode(double value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return Double.toString(value);
        }
    }

    final class TextNode implements Node {
        private static String escape(String s) {
            final StringBuilder sb = new StringBuilder(s.length());
            final int len = s.length();
            for (int i = 0; i < len; ++i) {
                final char c = s.charAt(i);
                switch(c) {
                    case '\"':
                        sb.append("\\\"");
                        break;
                    case '\\':
                        sb.append("\\\\");
                        break;
                    case '/':
                        sb.append("\\/");
                        break;
                    case '\b':
                        sb.append("\\b");
                        break;
                    case '\f':
                        sb.append("\\f");
                        break;
                    case '\n':
                        sb.append("\\n");
                        break;
                    case '\r':
                        sb.append("\\r");
                        break;
                    case '\t':
                        sb.append("\\t");
                        break;
                    default:
                        if ((c >= '\u0000' && c <= '\u001F') ||
                            (c >= '\u007F' && c <= '\u009F') ||
                            (c >= '\u2000' && c <= '\u20FF')) {
                            sb.append("\\u" + Integer.toHexString(c | 0x10000).substring(1));
                        } else {
                            sb.append(c);
                        }
                }
            }

            return sb.toString();
        }

        public final String value;

        public TextNode(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "\"" + escape(value) + '"';
        }
    }

    final class ArrayNode implements Node {
        public final List<Node> value;

        public ArrayNode(List<Node> value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return
                value.stream()
                    .map(Node::toString)
                    .collect(Collectors.joining(",", "[", "]"));
        }
    }

    final class ObjectNode implements Node {
        public final LinkedHashMap<String, Node> value;

        public ObjectNode(LinkedHashMap<String, Node> value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return
                value.entrySet().stream()
                    .map(entry -> "\"" + entry.getKey() + "\":" + entry.getValue())
                    .collect(Collectors.joining(",", "{", "}"));
        }
    }
}
