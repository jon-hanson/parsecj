package org.javafp.parsecj.json;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A model for JSON nodes.
 */
public interface Node {
    public static Node nul() {
        return NullNode.instance;
    }

    public static Node bool(boolean value) {
        return new BooleanNode(value);
    }

    public static Node number(double value) {
        return new NumberNode(value);
    }

    public static Node text(String value) {
        return new TextNode(value);
    }

    public static Node array(List<Node> value) {
        return new ArrayNode(value);
    }

    public static Node object(LinkedHashMap<String, Node> value) {
        return new ObjectNode(value);
    }

    public static final class NullNode implements Node {
        public static final NullNode instance = new NullNode();

        private NullNode() {
        }

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
                    .map(entry -> "\"" + entry.getKey() + "\":" + entry.getValue())
                    .collect(Collectors.joining(",", "{", "}"));
        }
    }
}
