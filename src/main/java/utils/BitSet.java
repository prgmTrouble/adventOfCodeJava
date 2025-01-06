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
        assert (capacity >>> 5) <= Integer.MAX_VALUE;
        return (int)((capacity >>> 5) + ((capacity & 0b11111) != 0? 1 : 0));
    }
    /** @return A bitset array with the specified minimum capacity */
    public static int[] create(final long capacity) {return new int[arraySize(capacity)];}
    /** @return The specified bit. */
    public static byte get(final int[] set,final long bit) {return (byte)((set[(int)(bit >>> 5)] >>> bit) & 1);}
    /** @return {@code true} if the bit at the specified location is set */
    public static boolean test(final int[] set,final long bit) {return get(set,bit) != 0;}
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
            set[majorS] |= (-1 << minorS) & ((1 << minorE) - 1);
        else
        {
            if(majorS + 1 < majorE)
                Arrays.fill(set,majorS + 1,majorE,-1);
            set[majorS] |= (-1 << minorS);
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
        assert end >= start;
        final int majorS = (int)(start >>> 5),
                  majorE = (int)(end >>> 5);
        assert majorE <= set.length;
        final byte minorS = (byte)(start & 0b11111),
                   minorE = (byte)(end & 0b11111);
        if(majorS == majorE)
            set[majorS] &= (-1 << minorE) | ((1 << minorS) - 1);
        else
        {
            if(majorS + 1 < majorE)
                Arrays.fill(set,majorS + 1,majorE,0);
            set[majorS] &= (1 << minorS) - 1;
            if(majorE < set.length)
                set[majorE] &= (-1 << minorE);
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
        assert end >= start;
        final int majorS = (int)(start >>> 5),
                  majorE = (int)(end >>> 5);
        assert majorE <= set.length;
        final byte minorS = (byte)(start & 0b11111),
                   minorE = (byte)(end & 0b11111);
        if(majorS == majorE)
            set[majorS] ^= (-1 << minorS) & ((1 << minorE) - 1);
        else
        {
            for(int i = majorS + 1;i < majorE;++i)
                set[i] ^= -1;
            set[majorS] ^= (-1 << minorS);
            if(majorE < set.length)
                set[majorE] ^= (1 << minorE) - 1;
        }
    }
    /** Performs {@code a = b & (1 << pos)} */
    public static void select(final int[] a,final int[] b,final long pos)
    {
        assert 0 <= pos && pos < ((long)a.length << 5) && pos < ((long)b.length << 5);
        Arrays.fill(a,0);
        final int major = (int)(pos >>> 5);
        a[major] = b[major] & (1 << pos);
    }
    /** Performs {@code set &= 1 << pos} */
    public static void select(final int[] set,final long pos)
    {
        assert 0 <= pos && pos < ((long)set.length << 5);
        final int major = (int)(pos >>> 5);
        Arrays.fill(set,0,major,0);
        set[major] &= 1 << pos;
        Arrays.fill(set,major + 1,set.length,0);
    }
    /** Performs {@code a = b & ((1 << end) - 1) & (-1 << start)} */
    public static void mask(final int[] a,final int[] b,final long start,final long end)
    {
        assert 0 <= start &&
               start < ((long)a.length << 5) &&
               start < ((long)b.length << 5) &&
               start <= end &&
               end <= ((long)a.length << 5) &&
               end <= ((long)b.length << 5);
        final int majorS = (int)(start >>> 5),
                  majorE = (int)(end >>> 5);
        final byte boundE = majorE < a.length? (byte)1 : 0;
        Arrays.fill(a,0,majorS,0);
        Arrays.fill(a,majorE + boundE,a.length,0);
        System.arraycopy(b,majorS,a,majorS,majorE - majorS + boundE);
        a[majorS] &= -1 << start;
        if(majorE < a.length)
            a[majorE] &= (1 << end) - 1;
    }
    /** Performs {@code set &= ((1 << end) - 1) & (-1 << start)} */
    public static void mask(final int[] set,final long start,final long end)
    {
        assert 0 <= start && start < ((long)set.length << 5) && start <= end && end <= ((long)set.length << 5);
        final int majorS = (int)(start >>> 5),
                  majorE = (int)(end >>> 5);
        final byte boundE = majorE < set.length? (byte)1 : 0;
        Arrays.fill(set,0,majorS,0);
        Arrays.fill(set,majorE + boundE,set.length,0);
        set[majorS] &= -1 << start;
        if(majorE < set.length)
            set[majorE] &= (1 << end) - 1;
    }
    /** Performs {@code a = b & ((1 << start) - 1) & (-1 << end)} */
    public static void inverseMask(final int[] a,final int[] b,final long start,final long end)
    {
        assert 0 <= start &&
               start < ((long)a.length << 5) &&
               start < ((long)b.length << 5) &&
               start <= end &&
               end <= ((long)a.length << 5) &&
               end <= ((long)b.length << 5);
        final int majorS = (int)(start >>> 5),
                  majorE = (int)(end >>> 5);
        System.arraycopy(b,0,a,0,majorS + 1);
        System.arraycopy(b,majorE,a,majorE,a.length - majorE);
        if(majorS == majorE)
            a[majorS] &= ((1 << start) - 1) | (-1 << end);
        else
        {
            a[majorS] &= (1 << start) - 1;
            if(majorS + 1 < majorE)
                Arrays.fill(a,majorS + 1,majorE,0);
            if(majorE < a.length)
                a[majorE] &= -1 << end;
        }
    }
    /** Performs {@code set &= ((1 << start) - 1) | (-1 << end)} */
    public static void inverseMask(final int[] set,final long start,final long end)
    {
        assert 0 <= start && start < ((long)set.length << 5) && start <= end && end <= ((long)set.length << 5);
        final int majorS = (int)(start >>> 5),
                  majorE = (int)(end >>> 5);
        if(majorS == majorE)
            set[majorS] &= ((1 << start) - 1) | (-1 << end);
        else
        {
            set[majorS] &= (1 << start) - 1;
            if(majorS + 1 < majorE)
                Arrays.fill(set,majorS + 1,majorE,0);
            if(majorE < set.length)
                set[majorE] &= -1 << end;
        }
    }
    /** Performs {@code a = ~b} */
    public static void not(final int[] a,final int[] b)
    {
        assert a.length >= b.length;
        int i;
        for(i = 0;i < b.length;++i)
            a[i] = ~b[i];
        Arrays.fill(a,i,a.length,-1);
    }
    /** Performs {@code set = ~set} */
    public static void not(final int[] set) {for(int i = 0;i < set.length;++i) set[i] ^= -1;}
    
    /** Performs {@code a = b & c} */
    public static void and(final int[] a,final int[] b,final int[] c)
    {
        assert a.length >= min(b.length,c.length);
        int i;
        for(i = 0;i < min(b.length,c.length);++i)
            a[i] = b[i] & c[i];
        Arrays.fill(a,i,a.length,0);
    }
    /** Performs {@code a = b & ~c} */
    public static void andNot(final int[] a,final int[] b,final int[] c)
    {
        assert a.length >= min(b.length,c.length);
        int i;
        for(i = 0;i < min(b.length,c.length);++i)
            a[i] = b[i] & ~c[i];
        if(a.length >= b.length)
        {
            System.arraycopy(b,i,a,i,b.length - i);
            Arrays.fill(a,b.length,a.length,0);
        }
    }
    /** Performs {@code a = b | c} */
    public static void or(final int[] a,final int[] b,final int[] c)
    {
        assert a.length >= max(b.length,c.length);
        int i;
        for(i = 0;i < min(a.length,b.length);++i)
            a[i] = b[i] | c[i];
        if(a.length > b.length)
            System.arraycopy(a,i,c,i,a.length - i);
        else
            System.arraycopy(b,i,c,i,b.length - i);
    }
    /** Performs {@code a = b ^ c} */
    public static void xor(final int[] a,final int[] b,final int[] c)
    {
        assert a.length >= max(b.length,c.length);
        int i;
        for(i = 0;i < min(a.length,b.length);++i)
            a[i] = b[i] ^ c[i];
        if(b.length > c.length)
            System.arraycopy(b,i,a,i,b.length - i);
        else
            System.arraycopy(c,i,a,i,c.length - i);
    }
    
    /** Performs {@code a &= b} */
    public static void and(final int[] a,final int[] b)
    {
        int i;
        for(i = 0;i < min(a.length,b.length);++i)
            a[i] &= b[i];
        Arrays.fill(a,i,a.length,0);
    }
    /** Performs {@code a &= ~b}, assuming {@code a.length >= b.length} */
    public static void andNot(final int[] a,final int[] b)
    {
        for(int i = 0;i < b.length;++i)
            a[i] &= ~b[i];
    }
    /** Performs {@code a |= b}, assuming {@code a.length >= b.length} */
    public static void or(final int[] a,final int[] b)
    {
        for(int i = 0;i < b.length;++i)
            a[i] |= b[i];
    }
    /** Performs {@code a ^= b}, assuming {@code a.length >= b.length} */
    public static void xor(final int[] a,final int[] b)
    {
        for(int i = 0;i < b.length;++i)
            a[i] ^= b[i];
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
        final int s0 = set[majorS] & (-1 << minorS);
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
        final int s0 = ~set[majorS] & (-1 << minorS);
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
                final int s0 = e0 & (-1 << minorS);
                return s0 == 0? -1L : (((majorS + 1L) << 5) - Integer.numberOfLeadingZeros(s0) - 1);
            }
            if(e0 != 0)
                return ((majorE + 1L) << 5) - Integer.numberOfLeadingZeros(e0) - 1;
        }
        for(int i = majorE;--i > majorS;)
            if(set[i] != 0)
                return ((i + 1L) << 5) - Integer.numberOfLeadingZeros(set[i]) - 1;
        final int s0 = set[majorS] & (-1 << minorS);
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
                final int s0 = e0 & (-1 << minorS);
                return s0 == 0? -1L : (((majorS + 1L) << 5) - Integer.numberOfLeadingZeros(s0) - 1);
            }
            if(e0 != 0)
                return ((majorE + 1L) << 5) - Integer.numberOfLeadingZeros(e0) - 1;
        }
        for(int i = majorE;--i > majorS;)
            if(set[i] != -1)
                return ((i + 1L) << 5) - Integer.numberOfLeadingZeros(~set[i]) - 1;
        final int s0 = ~set[majorS] & (-1 << minorS);
        if(s0 != 0)
            return ((majorS + 1L) << 5) - Integer.numberOfLeadingZeros(s0) - 1;
        return -1L;
    }
    
    /** Performs a logical left shift */
    public static void lsh(final int[] set,final long amount)
    {
        if(amount == 0 | set.length == 0)
            return;
        if(amount < 0)
        {
            rsh(set,-amount);
            return;
        }
        final int major = (int)(amount >>> 5);
        assert major >= 0;
        if(major >= set.length)
        {
            Arrays.fill(set,0);
            return;
        }
        if(major != 0)
        {
            System.arraycopy(set,0,set,major,set.length - major);
            Arrays.fill(set,0,major,0);
        }
        final byte minor = (byte)(amount & 0b11111),
                    comp = (byte)(-amount & 0b11111); // 32 - minor
        if(minor != 0)
        {
            int i;
            for(i = set.length - 1;i > major;--i)
                set[i] = (set[i] << minor) | (set[i - 1] >>> comp);
            set[i] <<= minor;
        }
    }
    /** Performs a masked logical left shift */
    public static void lsh(final int[] set,final long start,final long end,final long amount)
    {
        if(amount == 0 | set.length == 0 | start == end)
            return;
        if(amount < 0)
        {
            rsh(set,start,end,-amount);
            return;
        }
        assert end > start;
        final int majorS = (int)(start >>> 5),
                  majorE = (int)(end >>> 5),
                  majorA = (int)(amount >>> 5);
        assert majorE >= majorS &&
               majorS >= 0 &&
               majorA >= 0 &&
               majorS < set.length &&
               majorE <= set.length;
        final byte minorA = (byte)(amount & 0b11111),
                    compA = (byte)(-amount & 0b11111); // 32 - minorA
        // Only the low 5 bits are used for the 32 bit shift operator. This is must be accounted for when
        // calculating the remainder of a shift operation (i.e. the edge case (y = 0) for (x >>> -y)).
        // See https://docs.oracle.com/javase/specs/jls/se23/html/jls-15.html#jls-15.19
        final int mskS = (1 << start) - 1,
                  mskE = (-1 << end);
        if(majorS + (mskE & 1) == majorE)
        {
            // Shift range is in the same word: only update this word
            
            // The first bit of 'mskE' encodes (minorE != 0) and, by extension, whether (end - 1) is in the same
            // word as 'start'. We want to disable 'mskE' if this bit is set, which can be done by sign extending
            // the inverse of the first bit (hence the '-(~mskE & 1)').
            final int msk = mskS | (mskE & -(~mskE & 1));
            // The same sign extension trick can be done to disable (set[majorS] & ~msk) when (majorA != 0). The
            // compiler should be able to trivially remove the branch.
            set[majorS] = (set[majorS] & msk) | (((set[majorS] & ~msk) << minorA) & ~msk & -(majorA == 0? 1 : 0));
        }
        else if(start + amount >= end)
        {
            // Shift amount exceeds range: fill zero
            set[majorS] &= mskS;
            Arrays.fill(set,majorS + 1,majorE,0);
            if(majorE < set.length)
                set[majorE] &= mskE;
        }
        else
        {
            if(majorE < set.length)
            {
                // Update the end word.
                int l = 0,r = 0;
                // This subtraction will never underflow since the shift amount is
                // guaranteed to be within the range.
                final int sig = majorE - majorA - majorS;
                if(sig >= 0)
                {
                    l = set[majorE - majorA];
                    if(majorA == 0)
                        l &= ~mskE;
                    if(sig == 0) // majorE - majorA == majorS
                        l &= ~mskS;
                    else
                    {
                        r = set[majorE - majorA - 1];
                        if(sig == 1) // majorE - majorA - 1 == majorS
                            r &= ~mskS;
                    }
                }
                set[majorE] = (set[majorE] & mskE) | (((l << minorA) | (minorA != 0? r >>> compA : 0)) & ~mskE);
            }
            // Update the middle words whose two source words' indices are greater than 'majorS'.
            int i;
            for(i = majorE - 1;i - majorA - majorS > 1;--i)
                set[i] = (set[i - majorA] << minorA) | (minorA != 0? set[i - majorA - 1] >>> compA : 0);
            // Update any remaining words.
            // This subtraction will never underflow since the shift amount is
            // guaranteed to be within the range and (majorE > majorS).
            final int sig = i - majorA - majorS;
            int fill = majorS + 1,r = 0;
            switch(sig)
            {
                case 1:
                    // i - majorA == majorS + 1
                    set[i] = (set[i - majorA] << minorA) | (minorA != 0? (set[majorS] & ~mskS) >>> compA : 0);
                    // intentional fallthrough
                case 0:
                    // i - majorA == majorS
                    if(majorA != 0)
                        // (sig == 1) implies (i - majorA == majorS + 1)
                        // (sig == 0) implies (i - majorA == majorS)
                        // (majorA != 0) therefore implies that (i > majorS + sig)
                        // assert i > majorS + sig;
                        set[fill = i - sig] = (set[majorS] & ~mskS) << minorA;
                    else
                        r = ((set[majorS] & ~mskS) << minorA) & ~mskS;
                    break;
                default:
                    // i - majorA < majorS
                    // (sig < 0) implies (i - majorA < majorS), which also implies that the for-loop exited
                    // before the first iteration. This therefore means that (i == majorE - 1).
                    // assert i == majorE - 1;
                    // (i - majorA < majorS) also means that the source for (set[i - majorA]) is effectively
                    // zero. This means that all bits in the range [start,majorE*32) (i.e. the unmasked bits
                    // in 'set[majorS]' and all words in the range (majorS,majorE)) should be cleared. The
                    // first top-level if statement guarantees (majorS < majorE), so this is a simple task.
                    // assert majorS < majorE;
                    fill = majorE;
            }
            Arrays.fill(set,majorS + 1,fill,0);
            set[majorS] = (set[majorS] & mskS) | r;
        }
    }
    /** Performs a logical right shift */
    public static void rsh(final int[] set,final long amount)
    {
        if(amount == 0 | set.length == 0)
            return;
        if(amount < 0)
        {
            lsh(set,-amount);
            return;
        }
        final int major = (int)(amount >>> 5);
        assert major >= 0;
        if(major >= set.length)
        {
            Arrays.fill(set,0);
            return;
        }
        if(major != 0)
        {
            System.arraycopy(set,major,set,0,set.length - major);
            Arrays.fill(set,set.length - major,set.length,0);
        }
        final byte minor = (byte)(amount & 0b11111),
                    comp = (byte)(-amount & 0b11111); // 32 - minor
        if(minor != 0)
        {
            int i;
            for(i = 0;i + 1 < set.length - major;++i)
                set[i] = (set[i] >>> minor) | (set[i + 1] << comp);
            set[i] >>>= minor;
        }
    }
    /** Performs a masked logical right shift */
    public static void rsh(final int[] set,final long start,final long end,final long amount)
    {
        if(amount == 0 | set.length == 0 | start == end)
            return;
        if(amount < 0)
        {
            lsh(set,start,end,-amount);
            return;
        }
        assert end > start;
        final int majorS = (int)(start >>> 5),
                  majorE = (int)(end >>> 5),
                  majorA = (int)(amount >>> 5);
        assert majorE >= majorS &&
               majorS >= 0 &&
               majorA >= 0 &&
               majorS < set.length &&
               majorE <= set.length;
        final byte minorA = (byte)(amount & 0b11111),
                    compA = (byte)(-amount & 0b11111); // 32 - minorA
        // Only the low 5 bits are used for the 32 bit shift operator. This is must be accounted for when
        // calculating the remainder of a shift operation (i.e. the edge case (y = 0) for (x << -y)).
        // See https://docs.oracle.com/javase/specs/jls/se23/html/jls-15.html#jls-15.19
        final int mskS = (1 << start) - 1,
                  mskE = (-1 << end);
        if(majorS + (mskE & 1) == majorE)
        {
            // Shift range is in the same word: only update this word
            
            // The first bit of 'mskE' encodes (minorE != 0) and, by extension, whether (end - 1) is in the same
            // word as 'start'. We want to disable 'mskE' if this bit is set, which can be done by sign extending
            // the inverse of the first bit (hence the '-(~mskE & 1)').
            final int msk = mskS | (mskE & -(~mskE & 1));
            // The same sign extension trick can be done to disable (set[majorS] & ~msk) when (majorA != 0). The
            // compiler should be able to trivially remove the branch.
            set[majorS] = (set[majorS] & msk) | (((set[majorS] & ~msk) >>> minorA) & ~msk & -(majorA == 0? 1 : 0));
        }
        else if(start + amount >= end)
        {
            // Shift amount exceeds range: fill zero
            set[majorS] &= mskS;
            Arrays.fill(set,majorS + 1,majorE,0);
            if(majorE < set.length)
                set[majorE] &= mskE;
        }
        else
        {
            {
                // Update the start word.
                int l = 0,r = 0;
                // This subtraction will never underflow since the shift amount is
                // guaranteed to be within the range.
                final int sig = majorE - majorA - majorS;
                if(sig >= 0)
                {
                    l = set[majorS + majorA];
                    if(majorA == 0)
                        l &= ~mskS;
                    if(sig == 0) // majorE - majorA == majorS
                        l &= ~mskE;
                    else if(majorS + majorA + 1 < set.length)
                    {
                        r = set[majorS + majorA + 1];
                        if(sig == 1)
                            r &= ~mskE;
                    }
                }
                set[majorS] = (set[majorS] & mskS) | (((l >>> minorA) | (minorA != 0? r << compA : 0)) & ~mskS);
            }
            // Update the middle words whose two source words' indices are greater than 'majorS'.
            int i;
            for(i = majorS + 1;i + majorA + 1 < majorE;++i)
                set[i] = (set[i + majorA] >>> minorA) | (minorA != 0? set[i + majorA + 1] << compA : 0);
            // Update any remaining words.
            // This subtraction will never underflow since the shift amount is
            // guaranteed to be within the range and (majorE > majorS).
            final int sig = majorE - majorA - i;
            int fill = majorE,r = 0;
            switch(sig)
            {
                case 1:
                    // i + majorA == majorE - 1
                    set[i] = (set[i + majorA] >>> minorA) | (minorA != 0 & majorE < set.length? (set[majorE] & ~mskE) << compA : 0);
                    // intentional fallthrough
                case 0:
                    // i + majorA == majorE
                    if(majorA != 0)
                    {
                        // (sig == 1) implies (i + majorA == majorE - 1)
                        // (sig == 0) implies (i + majorA == majorE)
                        // (majorA != 0) therefore implies that (i < majorE - sig)
                        // assert i < majorE - sig;
                        set[i + sig] = majorE < set.length? (set[majorE] & ~mskE) >>> minorA : 0;
                        fill = i + sig + 1;
                    }
                    else if(majorE < set.length)
                        r = (set[majorE] & ~mskE) >>> minorA & ~mskE;
                    break;
                default:
                    // i + majorA > majorE
                    // (sig < 0) implies (i + majorA > majorE), which also implies that the for-loop exited
                    // before the first iteration. This therefore means that (i == majorS + 1).
                    // assert i == majorS + 1;
                    // (i + majorA > majorE) also means that the source for (set[i + majorA]) is effectively
                    // zero. This means that all bits in the range [majorS*32,end) (i.e. the unmasked bits
                    // in 'set[majorE]' and all words in the range (majorS,majorE)) should be cleared. The
                    // first top-level if statement guarantees (majorS < majorE), so this is a simple task.
                    // assert majorS < majorE;
                    fill = i;
            }
            Arrays.fill(set,fill,majorE,0);
            if(majorE < set.length)
                set[majorE] = (set[majorE] & mskE) | r;
        }
    }
    
    /** @return {@code true} iff {@code a} contains all elements of {@code b}. */
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