package org.javafp.parsecj;

import org.javafp.data.IList;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * An message which represents a (potential) parse failure.
 * Note, the construction of a Message doesn't necessarily indicate an failure,
 * the message may be intended for use later on when a parse failure occurs.
 */
public interface Message<I> {

    final class Exception extends java.lang.RuntimeException {
        public final Message message;

        public Exception(Message<?> message) {
            this.message = message;
        }
    }

    static <I> Message<I> of(int pos, I sym, String expected) {
        return new MessageImpl<I>(pos, sym, IList.of(expected));
    }

    static <I> Message<I> of(int pos, String expected) {
        return new MessageImpl<I>(pos, null, IList.of(expected));
    }

    static <I> Message<I> of(int pos) {
        return new MessageImpl<I>(pos, null, IList.of());
    }

    static <I> Message<I> of(String msg, int pos) {
        return new ErrorMessage<I>(pos, msg);
    }

    static <I> Message<I> of() {
        return EmptyMessage.instance();
    }

    static <I> Message<I> endOfInput(int pos, String expected) {
        return new EndOfInput<I>(pos, IList.of(expected));
    }

    static <I> LazyMessage<I> lazy(Supplier<Message<I>> supplier) {
        return new LazyMessage(supplier);
    }

    // The position the error occurred at.
    int position();

    // The symbol that caused the error.
    I symbol();

    // The names of the productions that were expected.
    IList<String> expected();

    default LazyMessage<I> expect(String name) {
        return Message.lazy(() ->
            Message.of(position(), symbol(), name)
        );
    }

    default Message<I> merge(Message<I> rhs) {
        return Message.lazy(() ->
            new MessageImpl<I>(
                this.position(),
                this.symbol(),
                this.expected().add(rhs.expected())
            )
        );
    }
}

final class EmptyMessage<I> implements Message<I> {

    private static final EmptyMessage<?> instance = new EmptyMessage<>();

    static <I> EmptyMessage<I> instance() {
        return (EmptyMessage<I>) instance;
    }

    private EmptyMessage() {
    }

    @Override
    public int position() {
        return 0;
    }

    @Override
    public I symbol() {
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

final class MessageImpl<I> implements Message<I> {

    // The position the Error occurred at.
    public final int pos;

    // The symbol that caused the Error.
    public final I sym;

    // The names of the productions that were expected.
    public final IList<String> expected;

    public MessageImpl(int pos, I sym, IList<String> expected) {
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
        switch (expected.size()) {
            case 0:
                return
                    "Unexpected '" + sym +
                        "' at position " + (pos == -1 ? "EOF" : pos) +
                        ". Expecting nothing";
            case 1:
                return
                    "Unexpected '" + sym +
                        "' at position " + (pos == -1 ? "EOF" : pos) +
                        ". Expecting [" + expected.head() + ']';
            default:
                final String expectedStr = expected.foldr1((x, y) -> x + ',' + y);
                return
                    "Unexpected '" + sym +
                        "' at position " + (pos == -1 ? "EOF" : pos) +
                        ". Expecting one of [" + expectedStr + ']';
        }
    }

    @Override
    public int position() {
        return pos;
    }

    @Override
    public I symbol() {
        return sym;
    }

    @Override
    public IList<String> expected() {
        return expected;
    }
}

/**
 * Message containing an error.
 */
final class ErrorMessage<I> implements Message<I> {
    // The position the Error occurred at.
    public final int pos;

    // The error message.
    public final String error;

    ErrorMessage(int pos, String error) {
        this.pos = pos;
        this.error = error;
    }

    @Override
    public int position() {
        return pos;
    }

    @Override
    public I symbol() {
        return null;
    }

    @Override
    public IList<String> expected() {
        return IList.of();
    }

    @Override
    public String toString() {
        return error + " at position " + pos;
    }
}

/**
 * Message indicating the end of the input has been reached
 */
final class EndOfInput<I> implements Message<I> {

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
    public I symbol() {
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
final class LazyMessage<I> implements Message<I> {

    private Supplier<Message<I>> supplier;
    private Message<I> value;

    public LazyMessage(Supplier<Message<I>> supplier) {
        this.supplier = supplier;
    }

    private synchronized Message<I> get() {
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
    public I symbol() {
        return get().symbol();
    }

    @Override
    public IList<String> expected() {
        return get().expected();
    }
}

