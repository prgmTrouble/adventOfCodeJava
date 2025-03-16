package utils;

public final class PackedArray
{
    private PackedArray() {}
    
    /**
     * Creates an array of packed integers.
     *
     * @param bits Number of bits per element.
     * @param size Number of elements.
     */
    public static long[] create(final byte bits,final int size)
    {
        return new long[((bits * size) >>> 6) + (((bits * size) & 63) == 0? 0 : 1)];
    }
    
    /**
     * @param bits Number of bits per element.
     * @param idx  Element index.
     * @param arr  Packed array.
     * @return The unpacked integer
     */
    public static long get(final byte bits,final int idx,final long[] arr)
    {
        assert 0 < bits && bits <= 64 && 0 <= idx && (long)(idx + 1) * bits <= (long)arr.length << 6;
        final int major = (int)(((long)idx * bits) >>> 6);
        final byte minor = (byte)(((long)idx * bits) & 63);
        long out = arr[major] >>> minor;
        if(minor + bits > 64)
            out |= arr[major + 1] << (-minor & 63);
        return out & ((1L << bits) - 1L);
    }
    
    /**
     * Writes a value to a packed array.
     *
     * @param bits Number of bits per element.
     * @param idx  Element index.
     * @param arr  Packed array.
     * @param data Element to set.
     */
    public static void set(final byte bits,final int idx,final long[] arr,long data)
    {
        assert 0 < bits && bits <= 64 && 0 <= idx && (long)(idx + 1) * bits <= (long)arr.length << 6;
        final int major = (int)(((long)idx * bits) >>> 6);
        final byte minor = (byte)(((long)idx * bits) & 63);
        data &= (1L << bits) - 1;
        if(minor + bits > 64)
        {
            arr[major] = (arr[major] & ((1L << minor) - 1)) | (data << minor);
            arr[major + 1] = (arr[major + 1] & (-1L << ((bits + minor) & 63))) | (data >>> (64 - minor));
        }
        else
            arr[major] = (arr[major] & ~(((1L << bits) - 1) << minor)) | (data << minor);
    }
    
    /**
     * Fills a range in the packed array with the specified value.
     *
     * @param bits  Number of bits per element.
     * @param start First element index, inclusive.
     * @param end   Last element index, exclusive.
     * @param data  Element to fill.
     */
    public static void fill(final byte bits,final int start,final int end,final long[] arr,long data)
    {
        assert 0 < bits && bits <= 64 && 0 <= start && start <= end && (long)end * bits <= (long)arr.length << 6;
        if(start == end) return;
        final int majorS = (int)(((long)start * bits) >>> 6),
                  majorE = (int)(((long)end * bits) >>> 6);
        final byte minorS = (byte)(((long)start * bits) & 63),
                   minorE = (byte)(((long)end * bits) & 63);
        // Repeat the significant bits across the 64-bit word.
        data &= (1L << bits) - 1;
        for(byte i = 0;i < 32 - Integer.numberOfLeadingZeros(64 / bits);++i)
            data |= data << (bits << i);
        
        if(majorS == majorE)
            arr[majorS] = (arr[majorS] & (((1L << minorS) - 1) | (-1L << minorE))) | ((data << minorS) & ((1L << minorE) - 1));
        else
        {
            arr[majorS] = (arr[majorS] & ((1L << minorS) - 1)) | (data << minorS);
            // Fill intermediate words.
            byte d = (byte)-minorS;
            for(int i = majorS + 1;i < majorE;++i)
            {
                d = (byte)((64 + d) % bits);
                arr[i] = (data >>> d) | (data << (bits - d));
            }
            if(minorE != 0)
            {
                d = (byte)((64 + d) % bits);
                arr[majorE] = (arr[majorE] & (-1L << minorE)) | (((data >>> d) | (data << (bits - d))) & ((1L << minorE) - 1));
            }
        }
    }
}