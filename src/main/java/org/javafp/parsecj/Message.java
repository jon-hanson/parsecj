package org.javafp.parsecj;

import org.javafp.data.IList;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * An message which represents a (potential) parse failure.
 * Note, the construction of a Message doesn't necessarily indicate an failure,
 * the message may be intended for use later on when a parse failure occurs.
 */
public interface Message<S> {

    public static <S> Message<S> of(int pos, S sym, String expected) {
        return new MessageImpl<S>(pos, sym, IList.of(expected));
    }

    public static <S> Message<S> of(int pos, String expected) {
        return new MessageImpl<S>(pos, null, IList.of(expected));
    }

    public static <S> Message<S> of(int pos) {
        return new MessageImpl<S>(pos, null, IList.of());
    }

    public static <S> Message<S> of() {
        return EmptyMessage.instance();
    }

    public static <S> Message<S> endOfInput(int pos, String expected) {
        return new EndOfInput<S>(pos, IList.of(expected));
    }

    public static <S> LazyMessage<S> lazy(Supplier<Message<S>> supplier) {
        return new LazyMessage(supplier);
    }

    // The position the error occurred at.
    public int position();

    // The symbol that caused the error.
    public S symbol();

    // The names of the productions that were expected.
    public IList<String> expected();

    public default LazyMessage<S> expect(String name) {
        return Message.lazy(() ->
            Message.of(position(), symbol(), name)
        );
    }

    public default Message<S> merge(Message<S> rhs) {
        return Message.lazy(() ->
            new MessageImpl<S>(
                this.position(),
                this.symbol(),
                this.expected().add(rhs.expected())
            )
        );
    }
}

final class EmptyMessage<S> implements Message<S> {

    private static final EmptyMessage<?> instance = new EmptyMessage<Object>();

    static <S> EmptyMessage<S> instance() {
        return (EmptyMessage<S>) instance;
    }

    private EmptyMessage() {
    }

    @Override
    public int position() {
        return 0;
    }

    @Override
    public S symbol() {
        return null;
    }

    @Override
    public IList<String> expected() {
        return IList.of();
    }

    @Override
    public String toString() {
        return "";
    }

    @Override
    public boolean equals(Object rhs) {
        return rhs == this;
    }

    @Override
    public int hashCode() {
        return 0;
    }
}

final class MessageImpl<S> implements Message<S> {

    // The position the Error occurred at.
    public final int pos;

    // The symbol that caused the Error.
    public final S sym;

    // The names of the productions that were expected.
    public final IList<String> expected;

    public MessageImpl(int pos, S sym, IList<String> expected) {
        this.pos = pos;
        this.sym = sym;
        this.expected = Objects.requireNonNull(expected);
    }

    @Override
    public boolean equals(Object rhs) {
        if (this == rhs) return true;
        if (rhs == null) return false;
        if (!(rhs instanceof Message)) {
            return false;
        }

        final Message message = (Message)rhs;
        if (message instanceof EndOfInput || message instanceof EmptyMessage) {
            return false;
        }

        if (pos != message.position()) return false;
        if (!expected.equals(message.expected())) return false;

        return sym.equals(message.symbol());
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
        final String expectedStr = expected.isEmpty() ? "" : expected.foldr1((x, y) -> x + ',' + y);
        return
            "Unexpected '" + sym +
                "' at position " + (pos == -1 ? "EOF" : pos) +
                ". Expecting one of [" +
                expectedStr + ']';
    }

    @Override
    public int position() {
        return pos;
    }

    @Override
    public S symbol() {
        return sym;
    }

    @Override
    public IList<String> expected() {
        return expected;
    }
}

/**
 * Message indicating the
 */
final class EndOfInput<S> implements Message<S> {

    // The position the Error occurred at.
    public final int pos;

    // The names of the productions that were expected.
    public final IList<String> expected;

    EndOfInput(int pos, IList<String> expected) {
        this.pos = pos;
        this.expected = expected;
    }

    @Override
    public int position() {
        return pos;
    }

    @Override
    public S symbol() {
        return null;
    }

    @Override
    public IList<String> expected() {
        return expected;
    }

    @Override
    public boolean equals(Object rhs) {
        if (this == rhs) return true;
        if (rhs == null) return false;
        if (!(rhs instanceof EndOfInput)) {
            return false;
        }

        final EndOfInput message = (EndOfInput)rhs;

        if (pos != message.pos) return false;

        return expected.equals(message.expected);
    }

    @Override
    public int hashCode() {
        int result = pos;
        result = 31 * result + expected.hashCode();
        return result;
    }

    @Override
    public String toString() {
        final String expectedStr = expected.isEmpty() ? "" : expected.foldr1((x, y) -> x + ',' + y);
        return
            "\"Unexpected EOF at position " + pos +
                ". Expecting one of [" +
                expectedStr + ']';
    }
}

/**
 * A lazily-constructed message.
 */
final class LazyMessage<S> implements Message<S> {

    private Supplier<Message<S>> supplier;
    private Message<S> value;

    public LazyMessage(Supplier<Message<S>> supplier) {
        this.supplier = supplier;
    }

    private synchronized Message<S> get() {
        if (supplier != null) {
            value = supplier.get();
            supplier = null;
        }
        return value;
    }

    @Override
    public boolean equals(Object rhs) {
        if (this == rhs) return true;
        if (rhs == null) return false;
        if (!(rhs instanceof Message)) {
            return false;
        }

        final Message message = (Message)rhs;

        if (get().position() != message.position()) return false;
        if (!get().expected().equals(message.expected())) return false;

        return get().symbol().equals(message.symbol());
    }

    @Override
    public int hashCode() {
        return get().hashCode();
    }

    @Override
    public String toString() {
        return get().toString();
    }

    @Override
    public int position() {
        return get().position();
    }

    @Override
    public S symbol() {
        return get().symbol();
    }

    @Override
    public IList<String> expected() {
        return get().expected();
    }
}

