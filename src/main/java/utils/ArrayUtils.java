package utils;

public final class ArrayUtils
{
    private ArrayUtils() {}

    /**
     * @param a    An ascending sorted array.
     * @param v    The search key.
     * @param size The size of the search region.
     * @return     An arbitrary index {@code i} in {@code a} such that {@code 0 <= i && i < size && v == a[i]},
     *             if it exists. If not, {@code -(i + 1)} is returned where {@code i} is the insertion position.
     */
    public static int find(final byte[] a,final byte v,int size)
    {
        int low = 0;
        while(low < size)
        {
            final int mid = (low + size - 1) >>> 1;

            if(a[mid] < v) low = mid + 1;
            else if(a[mid] > v) size = mid;
            else return mid;
        }
        return -(low + 1);
    }
    /**
     * @param a    An ascending sorted array.
     * @param v    The search key.
     * @param size The size of the search region.
     * @return     An arbitrary index {@code i} in {@code a} such that {@code 0 <= i && i < size && v == a[i]},
     *             if it exists. If not, {@code -(i + 1)} is returned where {@code i} is the insertion position.
     */
    public static int find(final short[] a,final short v,int size)
    {
        int low = 0;
        while(low < size)
        {
            final int mid = (low + size - 1) >>> 1;

            if(a[mid] < v) low = mid + 1;
            else if(a[mid] > v) size = mid;
            else return mid;
        }
        return -(low + 1);
    }
    /**
     * @param a    An ascending sorted array.
     * @param v    The search key.
     * @param size The size of the search region.
     * @return     An arbitrary index {@code i} in {@code a} such that {@code 0 <= i && i < size && v == a[i]},
     *             if it exists. If not, {@code -(i + 1)} is returned where {@code i} is the insertion position.
     */
    public static int find(final int[] a,final int v,int size)
    {
        int low = 0;
        while(low < size)
        {
            final int mid = (low + size - 1) >>> 1;

            if(a[mid] < v) low = mid + 1;
            else if(a[mid] > v) size = mid;
            else return mid;
        }
        return -(low + 1);
    }
    /**
     * @param a    An ascending sorted array.
     * @param v    The search key.
     * @param size The size of the search region.
     * @return     An arbitrary index {@code i} in {@code a} such that {@code 0 <= i && i < size && v == a[i]},
     *             if it exists. If not, {@code -(i + 1)} is returned where {@code i} is the insertion position.
     */
    public static int find(final long[] a,final long v,int size)
    {
        int low = 0;
        while(low < size)
        {
            final int mid = (low + size - 1) >>> 1;

            if(a[mid] < v) low = mid + 1;
            else if(a[mid] > v) size = mid;
            else return mid;
        }
        return -(low + 1);
    }
    /**
     * @param a    An ascending sorted array.
     * @param v    The search key.
     * @param size The size of the search region.
     * @return     An arbitrary index {@code i} in {@code a} such that {@code 0 <= i && i < size && v == a[i]},
     *             if it exists. If not, {@code -(i + 1)} is returned where {@code i} is the insertion position.
     */
    public static int find(final float[] a,final float v,int size)
    {
        int low = 0;
        while(low < size)
        {
            final int mid = (low + size - 1) >>> 1;

            if(a[mid] < v) low = mid + 1;
            else if(a[mid] > v) size = mid;
            else return mid;
        }
        return -(low + 1);
    }
    /**
     * @param a    An ascending sorted array.
     * @param v    The search key.
     * @param size The size of the search region.
     * @return     An arbitrary index {@code i} in {@code a} such that {@code 0 <= i && i < size && v == a[i]},
     *             if it exists. If not, {@code -(i + 1)} is returned where {@code i} is the insertion position.
     */
    public static int find(final double[] a,final double v,int size)
    {
        int low = 0;
        while(low < size)
        {
            final int mid = (low + size - 1) >>> 1;

            if(a[mid] < v) low = mid + 1;
            else if(a[mid] > v) size = mid;
            else return mid;
        }
        return -(low + 1);
    }

    /**
     * Inserts the element in an array sorted in ascending order.
     *
     * @param a    An ascending sorted array.
     * @param v    The search key.
     * @param size The size of the search region.
     */
    public static void insertSorted(final byte[] a,final byte v,final int size)
    {
        assert size >= 0;
        if(size == 0)
        {
            a[0] = v;
            return;
        }
        int low = 0,high = size - 1;
        while(low <= high)
        {
            final int mid = (low + high) >>> 1;

            if(a[mid] < v) low = mid + 1;
            else if(a[mid] > v) high = mid - 1;
            else {low = mid; break;}
        }
        System.arraycopy(a,low,a,low + 1,size - low);
        a[low] = v;
    }
    /**
     * Inserts the element in an array sorted in ascending order.
     *
     * @param a    An ascending sorted array.
     * @param v    The search key.
     * @param size The size of the search region.
     */
    public static void insertSorted(final short[] a,final short v,final int size)
    {
        assert size >= 0;
        if(size == 0)
        {
            a[0] = v;
            return;
        }
        int low = 0,high = size - 1;
        while(low <= high)
        {
            final int mid = (low + high) >>> 1;

            if(a[mid] < v) low = mid + 1;
            else if(a[mid] > v) high = mid - 1;
            else {low = mid; break;}
        }
        System.arraycopy(a,low,a,low + 1,size - low);
        a[low] = v;
    }
    /**
     * Inserts the element in an array sorted in ascending order.
     *
     * @param a    An ascending sorted array.
     * @param v    The search key.
     * @param size The size of the search region.
     */
    public static void insertSorted(final int[] a,final int v,final int size)
    {
        assert size >= 0;
        if(size == 0)
        {
            a[0] = v;
            return;
        }
        int low = 0,high = size - 1;
        while(low <= high)
        {
            final int mid = (low + high) >>> 1;

            if(a[mid] < v) low = mid + 1;
            else if(a[mid] > v) high = mid - 1;
            else {low = mid; break;}
        }
        System.arraycopy(a,low,a,low + 1,size - low);
        a[low] = v;
    }
    /**
     * Inserts the element in an array sorted in ascending order.
     *
     * @param a    An ascending sorted array.
     * @param v    The search key.
     * @param size The size of the search region.
     */
    public static void insertSorted(final long[] a,final long v,final int size)
    {
        assert size >= 0;
        if(size == 0)
        {
            a[0] = v;
            return;
        }
        int low = 0,high = size - 1;
        while(low <= high)
        {
            final int mid = (low + high) >>> 1;

            if(a[mid] < v) low = mid + 1;
            else if(a[mid] > v) high = mid - 1;
            else {low = mid; break;}
        }
        System.arraycopy(a,low,a,low + 1,size - low);
        a[low] = v;
    }
    /**
     * Inserts the element in an array sorted in ascending order.
     *
     * @param a    An ascending sorted array.
     * @param v    The search key.
     * @param size The size of the search region.
     */
    public static void insertSorted(final float[] a,final float v,final int size)
    {
        assert size >= 0;
        if(size == 0)
        {
            a[0] = v;
            return;
        }
        int low = 0,high = size - 1;
        while(low <= high)
        {
            final int mid = (low + high) >>> 1;

            if(a[mid] < v) low = mid + 1;
            else if(a[mid] > v) high = mid - 1;
            else {low = mid; break;}
        }
        System.arraycopy(a,low,a,low + 1,size - low);
        a[low] = v;
    }
    /**
     * Inserts the element in an array sorted in ascending order.
     *
     * @param a    An ascending sorted array.
     * @param v    The search key.
     * @param size The size of the search region.
     */
    public static void insertSorted(final double[] a,final double v,final int size)
    {
        assert size >= 0;
        if(size == 0)
        {
            a[0] = v;
            return;
        }
        int low = 0,high = size - 1;
        while(low <= high)
        {
            final int mid = (low + high) >>> 1;

            if(a[mid] < v) low = mid + 1;
            else if(a[mid] > v) high = mid - 1;
            else {low = mid; break;}
        }
        System.arraycopy(a,low,a,low + 1,size - low);
        a[low] = v;
    }
    
    /**
     * Creates an array of packed integers.
     *
     * @param bits Number of bits per element.
     * @param size Number of elements.
     */
    public static long[] packed(final byte bits,final int size)
    {
        return new long[((bits * size) >>> 6) + (((bits * size) & 63) == 0? 0 : 1)];
    }
    /**
     * @param bits Number of bits per element.
     * @param idx  Element index.
     * @param arr  Packed array.
     * @return The unpacked integer
     */
    public static long getPacked(final byte bits,final int idx,final long[] arr)
    {
        final byte shift = (byte)((idx * bits) & 63);
        long out = arr[(idx * bits) >>> 6] >>> shift;
        if(shift > 64 - bits)
            out |= arr[((idx * bits) >>> 6) + 1] >>> -shift;
        return out & ((1L << bits) - 1L);
    }
}