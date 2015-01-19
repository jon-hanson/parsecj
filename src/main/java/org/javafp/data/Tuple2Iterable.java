package org.javafp.data;

import java.util.Iterator;

/**
 * An Iterable composed to two Iterables, which can be iterated over simultaneously.
 */
public class Tuple2Iterable<A, B> implements Iterable<Tuple2<A, B>> {

    private static class Tuple2Iterator<A, B> implements Iterator<Tuple2<A, B>> {

        final Iterator<A> iterA;
        final Iterator<B> iterB;

        public Tuple2Iterator(Iterator<A> iterA, Iterator<B> iterB) {
            this.iterA = iterA;
            this.iterB = iterB;
        }

        @Override
        public boolean hasNext() {
            return iterA.hasNext() && iterB.hasNext();
        }

        @Override
        public Tuple2<A, B> next() {
            return new Tuple2<A, B>(iterA.next(), iterB.next());
        }

        @Override
        public void remove() {
            iterA.remove();
            iterB.remove();
        }
    }

    public static <A, B> Tuple2Iterable<A, B> of(Iterable<A> iterA, Iterable<B> iterB) {
        return new Tuple2Iterable<A, B>(iterA, iterB);
    }

    final Iterable<A> iterA;
    final Iterable<B> iterB;

    public Tuple2Iterable(Iterable<A> iterA, Iterable<B> iterB) {
        this.iterA = iterA;
        this.iterB = iterB;
    }

    @Override
    public Iterator<Tuple2<A, B>> iterator() {
        return new Tuple2Iterator<A, B>(iterA.iterator(), iterB.iterator());
    }
}
