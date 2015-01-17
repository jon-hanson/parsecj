package org.javafp.data;

/**
 * Simple class for a pair of values.
 */
public class Tuple2<A, B> implements Comparable<Tuple2<A, B>> {

    public final A first;
    public final B second;

    public static <A, B> Tuple2<A, B> of(A first, B second) {
        return new Tuple2<A, B>(first, second);
    }

    public Tuple2(A first, B second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public String toString() {
        return "(" + first + "," + second + ")";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((first == null) ? 0 : first.hashCode());
        result = prime * result + ((second == null) ? 0 : second.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj == null)
            return false;

        if (getClass() != obj.getClass())
            return false;

        final Tuple2<?, ?> rhs = (Tuple2<?, ?>)obj;

        if (first == null) {
            if (rhs.first != null)
                return false;
        } else if (!first.equals(rhs.first))
            return false;

        if (second == null) {
            if (rhs.second != null)
                return false;
        } else if (!second.equals(rhs.second))
            return false;

        return true;
    }

    @Override
    public int compareTo(Tuple2<A, B> rhs) {
        int result = compare(first, rhs.first);
        if (result != 0) {
            return result;
        }

        return compare(second, rhs.second);
    }

    private static <T> int compare(T lhs, T rhs) {
        if (lhs instanceof Comparable<?>) {
            @SuppressWarnings("unchecked")
            final Comparable<T> lhsComp = (Comparable<T>)lhs;
            return lhsComp.compareTo(rhs);
        } else {
            throw new RuntimeException("Tuple type is not Comparable");
        }
    }
}
