package org.javafp.parsecj.utils;

import java.util.*;

public abstract class Sets {
    public static <T> Set<T> empty() {
        return Collections.emptySet();
    }

    public static <T> Set<T> singleton(T value) {
        return Collections.singleton(value);
    }

    public static <T> Set<T> union(Set<? extends T> lhs, Set<? extends T> rhs, Set<T> out) {
        out.addAll(lhs);
        out.addAll(rhs);
        return out;
    }

    public static <T> Set<T> union (Set<T> lhs, Set<T> rhs) {
        if (SortedSet.class.isAssignableFrom(lhs.getClass()) && SortedSet.class.isAssignableFrom(rhs.getClass())) {
            final SortedSet<? extends T> lhsSS = (SortedSet)lhs;
            final SortedSet<? extends T> rhsSS = (SortedSet)rhs;
            final TreeSet<T> ts;
            if (lhsSS.comparator().equals(rhsSS.comparator())) {
                ts = new TreeSet<T>((Comparator)lhsSS.comparator());
            } else {
                ts = new TreeSet<T>();
            }
            return union(lhsSS, rhsSS, ts);
        } else {
            final HashSet<T> ts = new HashSet<T>(capacity(lhs.size() + rhs.size()));
            ts.addAll(lhs);
            ts.addAll(rhs);
            return ts;
        }
    }

    private static final int MAX_POWER_OF_TWO = 1 << (Integer.SIZE - 2);

    static int capacity(int expectedSize) {
        if (expectedSize < 3) {
            return expectedSize + 1;
        } else if (expectedSize < MAX_POWER_OF_TWO) {
            return (int) ((float) expectedSize / 0.75F + 1.0F);
        } else {
            return Integer.MAX_VALUE;
        }
    }
}
