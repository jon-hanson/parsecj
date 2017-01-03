package org.javafp.data;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/**
 * Simple recursive, immutable linked list.
 * This list type allows tails to be shared between lists.
 * @param <T>   element type
 */
public abstract class IList<T> implements Iterable<T> {

    public static <T> IList<T> of() {
        return Empty.EMPTY;
    }

    public static <T> IList<T> of(T elem) {
        return Empty.EMPTY.add(elem);
    }

    public static <T> IList<T> of(T... elems) {
        IList<T> list = Empty.EMPTY;
        for (int i = elems.length - 1; i >= 0; --i) {
            list = list.add(elems[i]);
        }
        return list;
    }

    public static <T> IList<T> add(T head, IList<T> tail) {
        return new Node<T>(head, tail);
    }

    public static <T> List<T> toList(IList<T> list) {
        final List<T> result = new LinkedList<T>();
        for (; !list.isEmpty(); list = list.tail()) {
            result.add(list.head());
        }
        return result;
    }

    public static String listToString(IList<Character> list) {
        final StringBuilder sb = new StringBuilder();
        for (; !list.isEmpty(); list = list.tail()) {
            sb.append(list.head());
        }
        return sb.toString();
    }

    public static IList<Character> listToString(String s) {
        IList<Character> list = Empty.EMPTY;
        for (int i = s.length() - 1; i >= 0; --i) {
            list = list.add(s.charAt(i));
        }
        return list;
    }

    public IList<T> add(T head) {
        return new Node<T>(head, this);
    }

    public abstract boolean isEmpty();

    public abstract T head();

    public abstract IList<T> tail();

    protected abstract StringBuilder asString(StringBuilder sb);

    @Override
    public boolean equals(Object rhs) {
        if (this == rhs) return true;
        if (rhs == null || getClass() != rhs.getClass()) return false;
        return equals((IList<T>)rhs);
    }

    public abstract boolean equals(IList<T> rhs);

    public abstract <S> S match(Function<Node<T>, S> node, Function<Empty<T>, S> empty);

    public abstract IList<T> add(IList<T> l);

    public abstract int size();

    public abstract IList<T> reverse();

    public abstract <U> IList<U> map(Function<T, U> f);

    public abstract <U> U foldr(BiFunction<T, U, U> f, U z);
    public abstract <U> U foldl(BiFunction<U, T, U> f, U z);

    public abstract T foldr1(BinaryOperator<T> f);
    public abstract T foldl1(BinaryOperator<T> f);

    public abstract Spliterator<T> spliterator();

    public Stream<T> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    public Stream<T> parallelStream() {
        return StreamSupport.stream(spliterator(), true);
    }

    public abstract Iterator<T> iterator();

    public static class Empty<T> extends IList<T> {
        static final Empty EMPTY = new Empty();

        private Empty() {
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public T head() {
            throw new UnsupportedOperationException("Cannot take the head of an empty list");
        }

        @Override
        public IList<T> tail() {
            throw new UnsupportedOperationException("Cannot take the tail of an empty list");
        }

        @Override
        public String toString() {
            return "[]";
        }

        @Override
        public boolean equals(IList<T> rhs) {
            return rhs.isEmpty();
        }

        @Override
        protected StringBuilder asString(StringBuilder sb) {
            return sb;
        }

        @Override
        public <S> S match(Function<Node<T>, S> node, Function<Empty<T>, S> empty) {
            return empty.apply(this);
        }

        @Override
        public IList<T> add(IList<T> l) {
            return l;
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public IList<T> reverse() {
            return EMPTY;
        }

        @Override
        public <U> IList<U> map(Function<T, U> f) {
            return EMPTY;
        }

        @Override
        public <U> U foldr(BiFunction<T, U, U> f, U z) {
            return z;
        }

        @Override
        public <U> U foldl(BiFunction<U, T, U> f, U z) {
            return z;
        }

        @Override
        public T foldr1(BinaryOperator<T> f) {
            throw new UnsupportedOperationException("Cannot call foldr1 on an empty list");
        }

        @Override
        public T foldl1(BinaryOperator<T> f) {
            throw new UnsupportedOperationException("Cannot call foldl1 on an empty list");
        }

        @Override
        public Spliterator<T> spliterator() {
            return new Spliterator<T>() {
                @Override
                public boolean tryAdvance(Consumer<? super T> action) {
                    return false;
                }

                @Override
                public Spliterator<T> trySplit() {
                    return null;
                }

                @Override
                public long estimateSize() {
                    return 0;
                }

                @Override
                public int characteristics() {
                    return Spliterator.IMMUTABLE + Spliterator.SIZED;
                }
            };
        }

        @Override
        public Iterator<T> iterator() {

            return new Iterator<T>(){

                @Override
                public boolean hasNext() {
                    return false;
                }

                @Override
                public T next() {
                    throw new NoSuchElementException();
                }
            };
        }
    }

    public static class Node<T> extends IList<T> {

        public final T head;
        public final IList<T> tail;

        Node(T head, IList<T> tail) {
            this.head = head;
            this.tail = tail;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public T head() {
            return head;
        }

        @Override
        public IList<T> tail() {
            return tail;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("[");
            asString(sb).setCharAt(sb.length() - 1, ']');
            return sb.toString();
        }

        @Override
        protected StringBuilder asString(StringBuilder sb) {
            return tail.asString(sb.append(head).append(','));
        }

        @Override
        public boolean equals(IList<T> rhs) {
            IList<T> lhs = this;
            while (!lhs.isEmpty()) {
                if (rhs.isEmpty()) {
                    return false;
                } else {
                    if (!lhs.head().equals(rhs.head())) {
                        return false;
                    }
                }
                lhs = lhs.tail();
                rhs = rhs.tail();
            }

            return rhs.isEmpty();
        }

        @Override
        public <S> S match(Function<Node<T>, S> node, Function<Empty<T>, S> empty) {
            return node.apply(this);
        }

        @Override
        public IList<T> add(IList<T> l) {
            return new Node<T>(head, tail.add(l));
        }

        @Override
        public int size() {
            IList<T> pos = this;
            int length = 0;
            while (!pos.isEmpty()) {
                ++length;
                pos = pos.tail();
            }

            return length;
        }

        @Override
        public IList<T> reverse() {
            IList<T> rev = IList.of();
            IList<T> next = this;
            while (!next.isEmpty()) {
                rev = rev.add(next.head());
                next = next.tail();
            }
            return rev;
        }

        @Override
        public <U> IList<U> map(Function<T, U> f) {
            return new Node<U>(f.apply(head), tail.map(f));
        }

        @Override
        public <U> U foldr(BiFunction<T, U, U> f, U z) {
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/**
 * Simple recursive, immutable linked list.
 * This list type allows tails to be shared between lists.
 * It does not allow null values to be held in the list.
 * @param <T> element type
 */
public abstract class IList<T> implements Iterable<T> {

    /**
     * Construct an empty list.
     */
    public static <T> IList<T> empty() {
        return Empty.EMPTY;
    }

    /**
     * Construct an empty list.
     */
    public static <T> IList<T> of() {
        return empty();
    }

    /**
     * Construct a list with on element.
     */
    public static <T> NonEmpty<T> of(T elem) {
        return IList.<T>empty().add(Objects.requireNonNull(elem));
    }

    /**
     * Construct a list with multiple elements.
     */
    public static <T> NonEmpty<T> of(T... elems) {
        IList<T> list = empty();
        for (int i = elems.length - 1; i >= 0; --i) {
            list = list.add(Objects.requireNonNull(elems[i]));
        }
        return (NonEmpty<T>)list;
    }

    /**
     * Construct a list from an Iterable.
     */
    public static <T> IList<T> of(Iterable<T> elems) {
        IList<T> list = empty();
        for (T elem : elems) {
            list = list.add(Objects.requireNonNull(elem));
        }
        return list.reverse();
    }

    /**
     * Concatenate two lists.
     */
    public static <T> IList<T> concat(IList<? extends T> listA, IList<? extends T>  listB) {
        IList<T> list = (IList<T>)listB;
        for (T elem : listA.reverse()) {
            list = list.add(elem);
        }
        return list;
    }

    /**
     * Convert a list of Characters into a String.
     */
    public static String listToString(IList<Character> list) {
        final StringBuilder sb = new StringBuilder();
        for (; !list.isEmpty(); list = list.tail()) {
            sb.append(list.head());
        }
        return sb.toString();
    }

    /**
     * Convert a String into a list of Characters.
     */
    public static IList<Character> listToString(String s) {
        IList<Character> list = empty();
        for (int i = s.length() - 1; i >= 0; --i) {
            list = list.add(s.charAt(i));
        }
        return list;
    }

    /**
     * Create a new list by adding an element to the head of this list.
     */
    public NonEmpty<T> add(T head) {
        return new NonEmpty<T>(head, this);
    }

    /**
     * Create a new list by adding multiple elements to the head of this list.
     */
    public IList<T> addAll(IList<T> head) {

        IList<T> l = head.reverse();
        IList<T> res = this;

        while(!l.isEmpty()) {
            res = res.add(l.head());
            l = l.tail();
        }

        return res;
    }

    /**
     * Is this list empty?.
     */
    public abstract boolean isEmpty();

    /**
     * Returns Optional.empty() if this list is empty,
     * otherwise it returns an Optional which wraps the non-empty list.
     */
    public abstract Optional<NonEmpty<T>> nonEmpty();

    /**
     * @return the head of this list.
     * @throws UnsupportedOperationException if the list is empty.
     */
    public abstract T head();

    /**
     * @return the tail of this list.
     * @throws UnsupportedOperationException if the list is empty.
     */
    public abstract IList<T> tail();

    /**
     * @return the indexed element of this list.
     * @throws IndexOutOfBoundsException if the index is out of bounds.
     */
    public abstract T get(int index);

    /**
     * Internal helper method.
     */
    protected abstract StringBuilder asString(StringBuilder sb);

    /**
     * List equality.
     * @return true if this list and rhs are equal in terms of their size and elements.
     */
    @Override
    public boolean equals(Object rhs) {
        if (this == rhs) return true;
        if (rhs == null || getClass() != rhs.getClass()) return false;
        return equals((IList<T>)rhs);
    }

    /**
     * Type-safe list equality.
     * @return true if this list and rhs are equal in terms of their elements.
     */
    public abstract boolean equals(IList<T> rhs);

    /**
     * Apply one of two functions depending on whether this list is empty or not.
     * @return the result of applying the appropriate function.
     */
    public abstract <S> S match(Function<NonEmpty<T>, S> nonEmpty, Function<Empty<T>, S> empty);

    /**
     * Create a new list by appending an element to the end of this list.
     */
    public abstract IList<T> append(IList<T> l);

    /**
     * @return the length of this list.
     */
    public abstract int size();

    /**
     * @return this list in reverse.
     */
    public abstract IList<T> reverse();

    /**
     * Map a function over this list.
     */
    public abstract <U> IList<U> map(Function<? super T, ? extends U> f);

    /**
     * Left-fold a function over this list.
     */
    public abstract <U> U foldr(BiFunction<T, U, U> f, U z);

    /**
     * Right-fold a function over this list.
     */
    public abstract <U> U foldl(BiFunction<U, T, U> f, U z);

    /**
     * Right-fold a function over this non-empty list.
     */
    public abstract T foldr1(BinaryOperator<T> f);

    /**
     * Left-fold a function over this non-empty list.
     */
    public abstract T foldl1(BinaryOperator<T> f);

    /**
     * Create a spliterator.
     */
    public abstract Spliterator<T> spliterator();

    /**
     * Create a Stream onto this list.
     */
    public Stream<T> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    /**
     * Create a parallel Stream onto this list.
     */
    public Stream<T> parallelStream() {
        return StreamSupport.stream(spliterator(), true);
    }

    /**
     * Create an iterator over this list.
     */
    public abstract Iterator<T> iterator();

    /**
     * Convert to a Java List implementation, albeit an immutable one.
     * @return Java List.
     */
    public abstract List<T> toList();

    public static final class Empty<T> extends IList<T> {
        static final Empty EMPTY = new Empty();

        private Empty() {
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public Optional<NonEmpty<T>> nonEmpty() {
            return Optional.empty();
        }

        @Override
        public T head() {
            throw new UnsupportedOperationException("Cannot take the head of an empty list");
        }

        @Override
        public IList<T> tail() {
            throw new UnsupportedOperationException("Cannot take the tail of an empty list");
        }

        @Override
        public T get(int index) {
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds for an " + size() + " element list");
        }

        @Override
        public String toString() {
            return "[]";
        }

        @Override
        public boolean equals(IList<T> rhs) {
            return rhs.isEmpty();
        }

        @Override
        protected StringBuilder asString(StringBuilder sb) {
            return sb;
        }

        @Override
        public <S> S match(Function<NonEmpty<T>, S> nonEmpty, Function<Empty<T>, S> empty) {
            return empty.apply(this);
        }

        @Override
        public IList<T> append(IList<T> l) {
            return l;
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public IList<T> reverse() {
            return EMPTY;
        }

        @Override
        public <U> IList<U> map(Function<? super T, ? extends U> f) {
            return EMPTY;
        }

        @Override
        public <U> U foldr(BiFunction<T, U, U> f, U z) {
            return z;
        }

        @Override
        public <U> U foldl(BiFunction<U, T, U> f, U z) {
            return z;
        }

        @Override
        public T foldr1(BinaryOperator<T> f) {
            throw new UnsupportedOperationException("Cannot call foldr1 on an empty list");
        }

        @Override
        public T foldl1(BinaryOperator<T> f) {
            throw new UnsupportedOperationException("Cannot call foldl1 on an empty list");
        }

        @Override
        public Spliterator<T> spliterator() {
            return new Spliterator<T>() {
                @Override
                public boolean tryAdvance(Consumer<? super T> action) {
                    return false;
                }

                @Override
                public Spliterator<T> trySplit() {
                    return null;
                }

                @Override
                public long estimateSize() {
                    return size();
                }

                @Override
                public int characteristics() {
                    return Spliterator.IMMUTABLE + Spliterator.SIZED;
                }
            };
        }

        @Override
        public Iterator<T> iterator() {

            return new Iterator<T>(){

                @Override
                public boolean hasNext() {
                    return false;
                }

                @Override
                public T next() {
                    throw new NoSuchElementException();
                }
            };
        }

        @Override
        public List<T> toList() {
            return Collections.emptyList();
        }
    }

    public static final class NonEmpty<T> extends IList<T> {

        public final T head;
        public final IList<T> tail;

        NonEmpty(T head, IList<T> tail) {
            this.head = Objects.requireNonNull(head);
            this.tail = Objects.requireNonNull(tail);
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public Optional<NonEmpty<T>> nonEmpty() {
            return Optional.of(this);
        }

        @Override
        public T head() {
            return head;
        }

        @Override
        public IList<T> tail() {
            return tail;
        }

        @Override
        public T get(int index) {
            final Supplier<RuntimeException> raiseError = () -> new IndexOutOfBoundsException("Index " + index + " out of bounds");

            if (index < 0) {
                throw raiseError.get();
            } else if (index == 0) {
                return head;
            } else {
                IList<T> next = tail;
                for (int i = 1; i < index; ++i) {
                    if (next.isEmpty()) {
                        throw raiseError.get();
                    } else {
                        next = ((NonEmpty<T>)next).tail;
                    }
                }
                if (next.isEmpty()) {
                    throw raiseError.get();
                } else {
                    return ((NonEmpty<T>)next).head;
                }
            }
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("[");
            asString(sb).setCharAt(sb.length() - 1, ']');
            return sb.toString();
        }

        @Override
        protected StringBuilder asString(StringBuilder sb) {
            return tail.asString(sb.append(head).append(','));
        }

        @Override
        public boolean equals(IList<T> rhs) {
            if (rhs.isEmpty()) {
                return false;
            } else {
                for (T lhs : this) {
                    if (rhs.isEmpty() || !lhs.equals(rhs.head())) {
                        return false;
                    }

                    rhs = rhs.tail();
                }

                return rhs.isEmpty();
            }
        }

        @Override
        public <S> S match(Function<NonEmpty<T>, S> nonEmpty, Function<Empty<T>, S> empty) {
            return nonEmpty.apply(this);
        }

        @Override
        public IList<T> append(IList<T> l) {
            return new NonEmpty<T>(head, tail.append(l));
        }

        @Override
        public int size() {
            IList<T> pos = this;
            int length = 0;
            while (!pos.isEmpty()) {
                ++length;
                pos = pos.tail();
            }

            return length;
        }

        @Override
        public IList<T> reverse() {
            IList<T> result = IList.of();
            IList<T> next = this;
            for (;!next.isEmpty(); next = next.tail()) {
                result = result.add(next.head());
            }
            return result;
        }

        @Override
        public <U> IList<U> map(Function<? super T, ? extends U> f) {
            IList<U> result = IList.empty();
            IList<T> next = this;
            for (;!next.isEmpty(); next = next.tail()) {
                result = result.add(f.apply(next.head()));
            }
            return result.reverse();
        }

        @Override
        public <U> U foldr(BiFunction<T, U, U> f, U z) {
            return f.apply(head, tail.foldr(f, z));
        }

        @Override
        public <U> U foldl(BiFunction<U, T, U> f, U z) {
            U r = z;

            for (IList<T> l = this; !l.isEmpty(); l = l.tail()) {
                r = f.apply(r, l.head());
            }

            return r;
        }

        @Override
        public T foldr1(BinaryOperator<T> f) {
            return tail.nonEmpty()
                .map(tl -> f.apply(head, tl.foldr1(f)))
                .orElse(head);
        }

        @Override
        public T foldl1(BinaryOperator<T> f) {
            T r = null;

            for (IList<T> l = this; !l.isEmpty(); l = l.tail()) {
                if (r == null) {
                    r = l.head();
                } else {
                    r = f.apply(r, l.head());
                }
            }

            return r;
        }

        @Override
        public Spliterator<T> spliterator() {
            return Spliterators.spliterator(this.iterator(), size(), Spliterator.IMMUTABLE + Spliterator.SIZED);
        }

        @Override
        public Iterator<T> iterator() {
            return new Iterator<T>(){

                IList<T> pos = NonEmpty.this;

                @Override
                public boolean hasNext() {
                    return !pos.isEmpty();
                }

                @Override
                public T next() {
                    final T head = pos.head();
                    pos = pos.tail();
                    return head;
                }
            };
        }

        @Override
        public List<T> toList() {
            return new ListAdaptor<T>(this);
        }
    }
}

class ListAdaptor<T> extends AbstractSequentialList<T> {

    private final IList<T> impl;
    private final int size;

    ListAdaptor(IList<T> impl) {
        this.impl = impl;
        size = impl.size();
    }

    @Override
    public ListIterator<T> listIterator(int index) {

        return new ListIterator<T>() {

            private IList<T> move(IList<T> node, int count) {
                for (int i = 0; i < count; ++i) {
                    node = node.tail();
                }
                return node;
            }

            private int pos = index;
            private IList<T> node = move(impl, index);

            @Override
            public boolean hasNext() {
                return pos < size;
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                } else {
                    final T ret = node.head();
                    node = node.tail();
                    ++pos;
                    return ret;
                }
            }

            @Override
            public boolean hasPrevious() {
                return pos >= 0;
            }

            @Override
            public T previous() {
                if (!hasPrevious()) {
                    throw new NoSuchElementException();
                } else {
                    --pos;
                    node = move(impl, pos);
                    ++pos;
                    return node.head();
                }
            }

            @Override
            public int nextIndex() {
                return pos;
            }

            @Override
            public int previousIndex() {
                return pos - 1;
            }

            @Override
            public void remove() {
                throw modError();
            }

            @Override
            public void set(T t) {
                throw modError();
            }

            @Override
            public void add(T t) {
                throw modError();
            }

            private UnsupportedOperationException modError() {
                return new UnsupportedOperationException("IList can not be modified");
            }
        };
    }

    @Override
    public int size() {
        return size;
    }
}

