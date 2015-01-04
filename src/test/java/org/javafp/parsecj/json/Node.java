package org.javafp.parsecj.json;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

public interface Node {
    public static final class NullNode implements Node {
        @Override
        public String toString() {
            return "null";
        }
    }

    public static final class BooleanNode implements Node {
        public final boolean value;

        public BooleanNode(boolean value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return Boolean.toString(value);
        }
    }

    public static final class NumberNode implements Node {
        public final double value;

        public NumberNode(double value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return Double.toString(value);
        }
    }

    public static final class TextNode implements Node {
        public final String value;

        public TextNode(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "\"" + value + '"';
        }
    }

    public static final class ArrayNode implements Node {
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

    public static final class ObjectNode implements Node {
        public final LinkedHashMap<String, Node> value;

        public ObjectNode(LinkedHashMap<String, Node> value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return
                value.entrySet().stream()
                    .map(entry -> entry.getKey() + ':' + entry.getValue())
                    .collect(Collectors.joining(";", "{", "}"));
        }
    }
}
