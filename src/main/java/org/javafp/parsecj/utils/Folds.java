package org.javafp.parsecj.utils;

import java.util.*;
import java.util.function.*;

/**
 * Fold operations.
 */
public abstract class Folds {
    /**
     * Left-fold a function over an {@link Iterable}.
     * @param f binary function to be applied for the fold
     * @param z starting value for the fold
     * @param iter Iterable to be folded over
     * @param <T> iterable element type
     * @param <R> result type of fold operation
     * @return the folded value
     */
    public static <T, R> R foldLeft(BiFunction<R, T, R> f, R z, Iterable<T> iter) {
        R acc = z;
        for (T t : iter) {
            acc = f.apply(acc, t);
        }
        return acc;
    }

    /**
     * Left-fold a function over a non-empty {@link Iterable}.
     * @param f binary operator to be applied for the fold
     * @param iter Iterable to be folded over
     * @param <T> iterable element type
     * @return the folded value
     */
    public static <T> T foldLeft1(BinaryOperator<T> f, Iterable<T> iter) {
        T acc = null;
        for (T t : iter) {
            if (acc == null) {
                acc = t;
            } else {
                acc = f.apply(acc, t);
            }
        }

        if (acc == null) {
            throw new IllegalArgumentException("Supplied Iterable argument is empty");
        } else {
            return acc;
        }
    }

    /**
     * Right-fold a function over an {@link List}}
     * @param f binary function to be applied for the fold
     * @param z starting value for the fold
     * @param l list to fold over
     * @param <T> list element type
     * @param <R> result type of fold operation
     * @return the folded value
     */
    public static <T, R> R foldRight(BiFunction<T, R, R> f, R z, List<T> l) {
        R acc = z;
        for (int i = l.size() - 1; i >= 0; --i) {
            acc = f.apply(l.get(i), acc);
        }
        return acc;
    }

    /**
     * Right-fold a function over a non-empty {@link List}.
     * @param f binary operator to be applied for the fold
     * @param l {@code List} to fold over
     * @param <T> list element type
     * @return the folded value
     */
    public static <T> T foldRight1(BinaryOperator<T> f, List<T> l) {
        final int i0 = l.size() - 1;
        T acc = null;
        for (int i = i0; i >= 0; --i) {
            if (i == i0) {
                acc = l.get(i);
            } else {
                acc = f.apply(l.get(i), acc);
            }
        }
        return acc;
    }

    /**
     * Right-fold a function over an {@link Set}}
     * @param f binary function to be applied for the fold
     * @param z starting value for the fold
     * @param s set to fold over
     * @param <T> set element type
     * @param <R> result type of fold operation
     * @return the folded value
     */
    public static <T, R> R foldRight(BiFunction<T, R, R> f, R z, Set<T> s) {
        return foldRight(f, z, (List<T>)new ArrayList<T>(s));
    }

    /**
     * Right-fold a function over a non-empty  {@link Set}}
     * @param f binary function to be applied for the fold
     * @param s set to fold over
     * @param <T> set element type
     * @return the folded value
     */
    public static <T> T foldRight1(BinaryOperator<T> f, Set<T> s) {
        return foldRight1(f, (List<T>)new ArrayList<T>(s));
    }
}
