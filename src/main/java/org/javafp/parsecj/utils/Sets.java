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
            final HashSet<T> ts = new HashSet<T>(lhs.size() + rhs.size());
            ts.addAll(lhs);
            ts.addAll(rhs);
            return ts;
        }
    }
}
