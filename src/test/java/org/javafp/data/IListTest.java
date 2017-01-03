package org.javafp.data;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class IListTest {

    private static final IList<Integer> empty = IList.of();
    private static final IList<Integer> l = IList.of(1, 2, 3, 4);

    @Test
    public void testEquals() {
        assertEquals("equals for an empty list", IList.of(), empty);
        assertEquals("[equals for a non-empty list", IList.of().add(4).add(3).add(2).add(1), l);

        final IList<Integer> l3 = IList.of(1, 2, 3);
        final IList<Integer> l5 = IList.of(1, 2, 3, 4, 5);
        assertFalse("equals for an different length lists", l3.equals(l));
        assertFalse("equals for an different length lists", l5.equals(l));
    }

    @Test
    public void testIsEmpty() {
        assertTrue("isEmpty for an empty list", empty.isEmpty());
        assertFalse("isEmpty() for a non-empty list", l.isEmpty());
    }

    @Test
    public void testLength() {
        assertEquals("size for an empty list", 0, empty.size());
        assertEquals("[1,2,3,4].size for a non-empty list", 4, l.size());
    }

    @Test
    public void testToString() {
        assertEquals("toString for an empty list", "[]", empty.toString());
        assertEquals("toString for a non-empty list", "[1,2,3,4]", l.toString());
    }

    @Test
    public void testFoldl() {
        assertEquals("foldl for an empty list", 0, empty.foldl((x, y) -> x - y, 0).intValue());
        assertEquals("foldl for a non-empty list", (((10-1)-2)-3)-4, l.foldl((x, y) -> x - y, 10).intValue());
    }

    @Test
    public void testFoldr() {
        assertEquals("foldr for an empty list", 0, empty.foldr((x, y) -> x - y, 0).intValue());
        assertEquals("foldr for a non-empty list", 1-(2-(3-(4-10))), l.foldr((x, y) -> x - y, 10).intValue());
    }

    @Test
    public void testMap() {
        assertEquals("map for an empty list", IList.of(), empty.map(x -> null));
        assertEquals("map for a non-empty list", IList.of(-1, -2, -3, -4), l.map(i -> -i));
    }

    @Test
    public void testMatch() {
        assertTrue("match for an empty list", empty.match(n -> false, e -> true));
        assertTrue("match for a non-empty list", l.match(n -> true, e -> false));
    }

    @Test
    public void testToList() {
        final IList<Integer> il = IList.of(0, 1, 2, 3, 4, 5);
        final List<Integer> l = il.toList();
        int i = 0;
        for (int el : l) {
            assertEquals("", i++, el);
        }
    }

    @Test
    public void testGet() {
        final IList<Integer> l = IList.of(0, 1, 2, 3);
        assertEquals("get for a non-empty list", 0, l.get(0).intValue());
        assertEquals("get for a non-empty list", 1, l.get(1).intValue());
        assertEquals("get for a non-empty list", 3, l.get(3).intValue());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGet2() {
        IList.of(0, 1, 2, 3).get(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGet3() {
        IList.of(0, 1, 2, 3).get(4);
    }
}
