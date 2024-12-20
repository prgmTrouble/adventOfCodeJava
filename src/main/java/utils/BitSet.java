package utils;

import java.util.Arrays;

import static java.lang.Math.max;
import static java.lang.Math.min;

/** Various bitset utility functions */
public final class BitSet
{
    private BitSet() {}
    
    /** @return The size of the backing array for a bitset of the specified minimum capacity */
    public static int arraySize(final long capacity)
    {
        assert (capacity >>> 5) < Integer.MAX_VALUE;
        return (int)((capacity >>> 5) + ((capacity & 0b11111) != 0? 1 : 0));
    }
    /** @return A bitset array with the specified minimum capacity */
    public static int[] create(final long capacity) {return new int[arraySize(capacity)];}
    /** @return {@code true} if the bit at the specified location is set */
    public static boolean test(final int[] set,final long bit) {return (set[(int)(bit >>> 5)] & (1 << (bit & 0b11111))) != 0;}
    /** Sets the specified bit */
    public static void set(final int[] set,final long bit) {set[(int)(bit >>> 5)] |= (1 << (bit & 0b11111));}
    /**
     * Sets all bits in the specified range
     *
     * @param start first bit, inclusive
     * @param end last bit, exclusive
     */
    public static void set(final int[] set,final long start,final long end)
    {
        assert end >= start;
        final int majorS = (int)(start >>> 5),
                  majorE = (int)(end >>> 5);
        assert majorE <= set.length;
        final byte minorS = (byte)(start & 0b11111),
                   minorE = (byte)(end & 0b11111);
        if(majorS == majorE)
            set[majorS] |= -(1 << minorS) & ((1 << minorE) - 1);
        else
        {
            if(majorS + 1 < majorE)
                Arrays.fill(set,majorS + 1,majorE,-1);
            set[majorS] |= -(1 << minorS);
            if(majorE < set.length)
                set[majorE] |= (1 << minorE) - 1;
        }
    }
    /** Clears the specified bit */
    public static void unset(final int[] set,final long bit) {set[(int)(bit >>> 5)] &= ~(1 << (bit & 0b11111));}
    /**
     * Clears all bits in the specified range
     *
     * @param start first bit, inclusive
     * @param end last bit, exclusive
     */
    public static void unset(final int[] set,final long start,final long end)
    {
        assert end > start;
        final int majorS = (int)(start >>> 5),
                  majorE = (int)(end >>> 5);
        assert majorE <= set.length;
        final byte minorS = (byte)(start & 0b11111),
                   minorE = (byte)(end & 0b11111);
        if(majorS == majorE)
            set[majorS] &= -(1 << minorE) | ((1 << minorS) - 1);
        else
        {
            if(majorS + 1 < majorE)
                Arrays.fill(set,majorS + 1,majorE,0);
            set[majorS] &= (1 << minorS) - 1;
            if(majorE < set.length)
                set[majorE] &= -(1 << minorE);
        }
    }
    /** Flips the specified bit */
    public static void toggle(final int[] set,final long bit) {set[(int)(bit >>> 5)] ^= (1 << (bit & 0b11111));}
    /**
     * Flips all bits in the specified range
     *
     * @param start first bit, inclusive
     * @param end last bit, exclusive
     */
    public static void toggle(final int[] set,final long start,final long end)
    {
        assert end > start;
        final int majorS = (int)(start >>> 5),
                  majorE = (int)(end >>> 5);
        assert majorE <= set.length;
        final byte minorS = (byte)(start & 0b11111),
                   minorE = (byte)(end & 0b11111);
        if(majorS == majorE)
            set[majorS] ^= -(1 << minorS) & ((1 << minorE) - 1);
        else
        {
            for(int i = majorS + 1;i < majorE;++i)
                set[i] ^= -1;
            set[majorS] ^= -(1 << minorS);
            if(majorE < set.length)
                set[majorE] ^= (1 << minorE) - 1;
        }
    }
    /** Flips every bit */
    public static void not(final int[] set) {for(int i = 0;i < set.length;++i) set[i] ^= -1;}
    /** @return {@code a & b} */
    public static int[] and(final int[] a,final int[] b)
    {
        final int[] c = new int[max(a.length,b.length)];
        int i;
        for(i = 0;i < min(a.length,b.length);++i)
            c[i] = a[i] & b[i];
        return c;
    }
    /** @return {@code a | b} */
    public static int[] or(final int[] a,final int[] b)
    {
        final int[] c = new int[max(a.length,b.length)];
        int i;
        for(i = 0;i < min(a.length,b.length);++i)
            c[i] = a[i] | b[i];
        for(;i < a.length;++i)
            c[i] = a[i];
        for(;i < b.length;++i)
            c[i] = b[i];
        return c;
    }
    /** @return {@code a ^ b} */
    public static int[] xor(final int[] a,final int[] b)
    {
        final int[] c = new int[max(a.length,b.length)];
        int i;
        for(i = 0;i < min(a.length,b.length);++i)
            c[i] = a[i] ^ b[i];
        for(;i < a.length;++i)
            c[i] = a[i];
        for(;i < b.length;++i)
            c[i] = b[i];
        return c;
    }
    
    /** @return The index of the first {@code 1} bit in the array, or {@code -1L} if not present */
    public static long firstSetBit(final int[] set)
    {
        for(int i = 0;i < set.length;++i)
            if(set[i] != 0)
                return Integer.numberOfTrailingZeros(set[i]) + ((long)i << 5);
        return -1L;
    }
    /**
     * @param start first bit, inclusive
     * @param end last bit, exclusive
     * @return The index of the first {@code 1} bit in the array in the specified range, or {@code -1L} if not present
     */
    public static long firstSetBit(final int[] set,final long start,final long end)
    {
        assert end >= start;
        final int majorS = (int)(start >>> 5),
                  majorE = (int)(end >>> 5);
        assert majorE <= set.length;
        assert majorS < set.length;
        final byte minorS = (byte)(start & 0b11111),
                   minorE = (byte)(end & 0b11111);
        final int s0 = set[majorS] & -(1 << minorS);
        if(majorS == majorE)
        {
            final int e0 = s0 & ((1 << minorE) - 1);
            return e0 == 0? -1L : (Integer.numberOfTrailingZeros(e0) + ((long)majorS << 5));
        }
        if(s0 != 0)
            return Integer.numberOfTrailingZeros(s0) + ((long)majorS << 5);
        for(int i = majorS + 1;i < majorE;++i)
            if(set[i] != 0)
                return Integer.numberOfTrailingZeros(set[i]) + ((long)i << 5);
        if(majorE < set.length)
        {
            final int e0 = set[majorE] & ((1 << minorE) - 1);
            if(e0 != 0)
                return Integer.numberOfTrailingZeros(e0) + ((long)majorE << 5);
        }
        return -1L;
    }
    /** @return The index of the first {@code 0} bit in the array, or {@code -1L} if not present */
    public static long firstUnsetBit(final int[] set)
    {
        for(int i = 0;i < set.length;++i)
            if(set[i] != -1)
                return Integer.numberOfTrailingZeros(~set[i]) + ((long)i << 5);
        return -1L;
    }
    /**
     * @param start first bit, inclusive
     * @param end last bit, exclusive
     * @return The index of the first {@code 0} bit in the array in the specified range, or {@code -1L} if not present
     */
    public static long firstUnsetBit(final int[] set,final long start,final long end)
    {
        assert end >= start;
        final int majorS = (int)(start >>> 5),
                  majorE = (int)(end >>> 5);
        assert majorE <= set.length;
        assert majorS < set.length;
        final byte minorS = (byte)(start & 0b11111),
                   minorE = (byte)(end & 0b11111);
        final int s0 = ~set[majorS] & -(1 << minorS);
        if(majorS == majorE)
        {
            final int e0 = s0 & ((1 << minorE) - 1);
            return e0 == 0? -1L : (Integer.numberOfTrailingZeros(e0) + ((long)majorS << 5));
        }
        if(s0 != 0)
            return Integer.numberOfTrailingZeros(s0) + ((long)majorS << 5);
        for(int i = majorS + 1;i < majorE;++i)
            if(set[i] != -1)
                return Integer.numberOfTrailingZeros(~set[i]) + ((long)i << 5);
        if(majorE < set.length)
        {
            final int e0 = ~set[majorE] & ((1 << minorE) - 1);
            if(e0 != 0)
                return Integer.numberOfTrailingZeros(e0) + ((long)majorE << 5);
        }
        return -1L;
    }
    /** @return The index of the last {@code 1} bit in the array, or {@code -1L} if not present */
    public static long lastSetBit(final int[] set)
    {
        for(int i = set.length;i-- > 0;)
            if(set[i] != 0)
                return ((i + 1L) << 5) - Integer.numberOfLeadingZeros(set[i]) - 1;
        return -1L;
    }
    /**
     * @param start first bit, inclusive
     * @param end last bit, exclusive
     * @return The index of the last {@code 1} bit in the array in the specified range, or {@code -1L} if not present
     */
    public static long lastSetBit(final int[] set,final long start,final long end)
    {
        assert end >= start;
        final int majorS = (int)(start >>> 5),
                  majorE = (int)(end >>> 5);
        assert majorE <= set.length;
        assert majorS < set.length;
        final byte minorS = (byte)(start & 0b11111),
                   minorE = (byte)(end & 0b11111);
        if(majorE < set.length)
        {
            final int e0 = set[majorE] & ((1 << minorE) - 1);
            if(majorS == majorE)
            {
                final int s0 = e0 & -(1 << minorS);
                return s0 == 0? -1L : (((majorS + 1L) << 5) - Integer.numberOfLeadingZeros(s0) - 1);
            }
            if(e0 != 0)
                return ((majorE + 1L) << 5) - Integer.numberOfLeadingZeros(e0) - 1;
        }
        for(int i = majorE;--i > majorS;)
            if(set[i] != 0)
                return ((i + 1L) << 5) - Integer.numberOfLeadingZeros(set[i]) - 1;
        final int s0 = set[majorS] & -(1 << minorS);
        if(s0 != 0)
            return ((majorS + 1L) << 5) - Integer.numberOfLeadingZeros(s0) - 1;
        return -1L;
    }
    /** @return The index of the last {@code 0} bit in the array, or {@code -1L} if not present */
    public static long lastUnsetBit(final int[] set)
    {
        for(int i = set.length;i-- > 0;)
            if(set[i] != -1)
                return ((i + 1L) << 5) - Integer.numberOfLeadingZeros(~set[i]) - 1;
        return -1L;
    }
    /**
     * @param start first bit, inclusive
     * @param end last bit, exclusive
     * @return The index of the last {@code 0} bit in the array in the specified range, or {@code -1L} if not present
     */
    public static long lastUnsetBit(final int[] set,final long start,final long end)
    {
        assert end >= start;
        final int majorS = (int)(start >>> 5),
                  majorE = (int)(end >>> 5);
        assert majorE <= set.length;
        assert majorS < set.length;
        final byte minorS = (byte)(start & 0b11111),
                   minorE = (byte)(end & 0b11111);
        if(majorE < set.length)
        {
            final int e0 = ~set[majorE] & ((1 << minorE) - 1);
            if(majorS == majorE)
            {
                final int s0 = e0 & -(1 << minorS);
                return s0 == 0? -1L : (((majorS + 1L) << 5) - Integer.numberOfLeadingZeros(s0) - 1);
            }
            if(e0 != 0)
                return ((majorE + 1L) << 5) - Integer.numberOfLeadingZeros(e0) - 1;
        }
        for(int i = majorE;--i > majorS;)
            if(set[i] != -1)
                return ((i + 1L) << 5) - Integer.numberOfLeadingZeros(~set[i]) - 1;
        final int s0 = ~set[majorS] & -(1 << minorS);
        if(s0 != 0)
            return ((majorS + 1L) << 5) - Integer.numberOfLeadingZeros(s0) - 1;
        return -1L;
    }
    
    /** Performs {@code a &= b} */
    public static void andAssign(final int[] a,final int[] b)
    {
        int i;
        for(i = 0;i < min(a.length,b.length);++i)
            a[i] &= b[i];
        for(;i < a.length;++i)
            a[i] = 0;
    }
    /** Performs {@code a |= b} */
    public static void orAssign(final int[] a,final int[] b)
    {
        assert a.length >= b.length;
        for(int i = 0;i < b.length;++i)
            a[i] |= b[i];
    }
    /** Performs {@code a ^= b} */
    public static void xorAssign(final int[] a,final int[] b)
    {
        assert a.length >= b.length;
        for(int i = 0;i < b.length;++i)
            a[i] ^= b[i];
    }
    
    /** @return {@code true} iff all elements of {@code b} are contained in {@code a}. */
    public static boolean superset(final int[] a,final int[] b)
    {
        int i;
        for(i = 0;i < min(a.length,b.length);++i)
            if((a[i] & b[i]) != b[i])
                return false;
        for(;i < b.length;++i)
            if(b[i] != 0)
                return false;
        return true;
    }
}
