package utils;

public final class IntegerUtils
{
    public static long pow(long b,byte p)
    {
        long r = 1;
        while(true)
        {
            if((p & 1) != 0)
                r *= b;
            if((p >>>= 1) == 0)
                break;
            b *= b;
        }
        return r;
    }
    
    static final byte[] GUESS_LG10 =
    {
         0, 0, 0, 0, 1, 1, 1, 2, 2, 2,
         3, 3, 3, 3, 4, 4, 4, 5, 5, 5,
         6, 6, 6, 6, 7, 7, 7, 8, 8, 8,
         9, 9, 9, 9,10,10,10,11,11,11,
        12,12,12,12,13,13,13,14,14,14,
        15,15,15,15,16,16,16,17,17,17,
        18,18,18,18,19
    };
    static final int[] i32pow10 =
    {
        1,
        10,
        100,
        1000,
        10000,
        100000,
        1000000,
        10000000,
        100000000,
        1000000000
    };
    static final long[] i64pow10 =
    {
        1L,
        10L,
        100L,
        1000L,
        10000L,
        100000L,
        1000000L,
        10000000L,
        100000000L,
        1000000000L,
        10000000000L,
        100000000000L,
        1000000000000L,
        10000000000000L,
        100000000000000L,
        1000000000000000L,
        10000000000000000L,
        100000000000000000L,
        1000000000000000000L,
        -8446744073709551616L // signed value of 10 ^ 19
    };
    public static byte log10(final int x)
    {
        final byte d = GUESS_LG10[32 - Integer.numberOfLeadingZeros(x)];
        return (byte)(d + (x < i32pow10[d]? 0 : 1));
    }
    public static byte log10(final long x)
    {
        final byte d = GUESS_LG10[64 - Long.numberOfLeadingZeros(x)];
        return (byte)(d + (x < i64pow10[d]? 0 : 1));
    }
    public static int i32pow10(final byte p) {return i32pow10[p];}
    public static long i64pow10(final byte p) {return i64pow10[p];}
}