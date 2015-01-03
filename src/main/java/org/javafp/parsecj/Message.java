package org.javafp.parsecj;

import org.javafp.data.List;

import java.util.function.Supplier;

/**
 * An error message which represents a parse failure.
 */
public class Message<S> {

    public static <S> Message<S> of(State<S> state, List<String> expected) {
        return new Message<S>(state.position(), state.current(), expected);
    }

    public static <S> Message<S> of(int pos, S sym, List<String> expected) {
        return new Message<S>(pos, sym, expected);
    }

    /**
     * A lazily-constructed error message.
     */
    public static class Ref<S> implements Supplier<Message<S>> {

        public static <S> Ref<S> of(Supplier<Message<S>> supplier) {
            return new Ref(supplier);
        }

        private Supplier<Message<S>> supplier;
        private Message<S> value;

        private Ref(Supplier<Message<S>> supplier) {
            this.supplier = supplier;
        }

        @Override
        public synchronized Message<S> get() {
            if (supplier != null) {
                value = supplier.get();
                supplier = null;
            }
            return value;
        }

        public Ref<S> merge(Ref<S> rhs) {
            return Ref.of(() ->
                Message.of(
                    this.get().pos,
                    this.get().sym,
                    this.get().expected.add(rhs.get().expected)
                )
            );
        }

        public Ref<S> expect(String name) {
            return Ref.of(() ->
                Message.of(this.get().pos, this.get().sym, List.of(name))
            );
        }

        @Override
        public String toString() {
            return "ref(" + get() + ")";
        }
    }

    // The position the error occurred at.
    public final int pos;

    // The symbol that caused the error.
    public final S sym;

    // The names of the productions that were expected.
    public final List<String> expected;

    public Message(int pos, S sym, List<String> expected) {
        this.pos = pos;
        this.sym = sym;
        this.expected = expected;
    }

    public String msg() {
        final String expectedStr = expected.isEmpty() ? "" : expected.foldr1((x, y) -> x + ',' + y);
        return
            "Unexpected '" + (sym == null ? "EOF" : sym) +
                "' at position " + (pos == -1 ? "EOF" : pos) +
                ". Expecting one of [" +
                expectedStr + ']';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Message message = (Message) o;

        if (pos != message.pos) return false;
        if (!expected.equals(message.expected)) return false;

        return sym.equals(message.sym);
    }

    @Override
    public int hashCode() {
        int result = pos;
        result = 31 * result + sym.hashCode();
        result = 31 * result + expected.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Message{" +
            "position=" + pos +
            ", sym=" + sym +
            ", expected=" + expected +
            '}';
    }
}
