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
    public static int find(final byte[] a,final byte v,final int size) {return find(a,v,0,size);}
    /**
     * @param a     An ascending sorted array.
     * @param v     The search key.
     * @param start The start of the search region, inclusive.
     * @param end   The end of the search region, exclusive.
     * @return      An arbitrary index {@code i} in {@code a} such that {@code start <= i && i < end && v == a[i]},
     *              if it exists. If not, {@code -(i + 1)} is returned where {@code i} is the insertion position.
     */
    public static int find(final byte[] a,final byte v,int start,int end)
    {
        while(start < end)
        {
            final int mid = (start + end - 1) >>> 1;
            final int comp = Byte.compareUnsigned(a[mid],v);
            if(comp < 0) start = mid + 1;
            else if(comp > 0) end = mid;
            else return mid;
        }
        return -(start + 1);
    }
    /**
     * @param a    An ascending sorted array.
     * @param v    The search key.
     * @param size The size of the search region.
     * @return     An arbitrary index {@code i} in {@code a} such that {@code 0 <= i && i < size && v == a[i]},
     *             if it exists. If not, {@code -(i + 1)} is returned where {@code i} is the insertion position.
     */
    public static int find(final short[] a,final short v,final int size) {return find(a,v,0,size);}
    /**
     * @param a     An ascending sorted array.
     * @param v     The search key.
     * @param start The start of the search region, inclusive.
     * @param end   The end of the search region, exclusive.
     * @return      An arbitrary index {@code i} in {@code a} such that {@code start <= i && i < end && v == a[i]},
     *              if it exists. If not, {@code -(i + 1)} is returned where {@code i} is the insertion position.
     */
    public static int find(final short[] a,final short v,int start,int end)
    {
        while(start < end)
        {
            final int mid = (start + end - 1) >>> 1;
            final int comp = Short.compareUnsigned(a[mid],v);
            if(comp < 0) start = mid + 1;
            else if(comp > 0) end = mid;
            else return mid;
        }
        return -(start + 1);
    }
    /**
     * @param a    An ascending sorted array.
     * @param v    The search key.
     * @param size The size of the search region.
     * @return     An arbitrary index {@code i} in {@code a} such that {@code 0 <= i && i < size && v == a[i]},
     *             if it exists. If not, {@code -(i + 1)} is returned where {@code i} is the insertion position.
     */
    public static int find(final int[] a,final int v,final int size) {return find(a,v,0,size);}
    /**
     * @param a     An ascending sorted array.
     * @param v     The search key.
     * @param start The start of the search region, inclusive.
     * @param end   The end of the search region, exclusive.
     * @return      An arbitrary index {@code i} in {@code a} such that {@code start <= i && i < end && v == a[i]},
     *              if it exists. If not, {@code -(i + 1)} is returned where {@code i} is the insertion position.
     */
    public static int find(final int[] a,final int v,int start,int end)
    {
        while(start < end)
        {
            final int mid = (start + end - 1) >>> 1;
            final int comp = Integer.compareUnsigned(a[mid],v);
            if(comp < 0) start = mid + 1;
            else if(comp > 0) end = mid;
            else return mid;
        }
        return -(start + 1);
    }
    /**
     * @param a    An ascending sorted array.
     * @param v    The search key.
     * @param size The size of the search region.
     * @return     An arbitrary index {@code i} in {@code a} such that {@code 0 <= i && i < size && v == a[i]},
     *             if it exists. If not, {@code -(i + 1)} is returned where {@code i} is the insertion position.
     */
    public static int find(final long[] a,final long v,final int size) {return find(a,v,0,size);}
    /**
     * @param a     An ascending sorted array.
     * @param v     The search key.
     * @param start The start of the search region, inclusive.
     * @param end   The end of the search region, exclusive.
     * @return      An arbitrary index {@code i} in {@code a} such that {@code start <= i && i < end && v == a[i]},
     *              if it exists. If not, {@code -(i + 1)} is returned where {@code i} is the insertion position.
     */
    public static int find(final long[] a,final long v,int start,int end)
    {
        while(start < end)
        {
            final int mid = (start + end - 1) >>> 1;
            final int comp = Long.compareUnsigned(a[mid],v);
            if(comp < 0) start = mid + 1;
            else if(comp > 0) end = mid;
            else return mid;
        }
        return -(start + 1);
    }
    /**
     * @param a    An ascending sorted array.
     * @param v    The search key.
     * @param size The size of the search region.
     * @return     An arbitrary index {@code i} in {@code a} such that {@code 0 <= i && i < size && v == a[i]},
     *             if it exists. If not, {@code -(i + 1)} is returned where {@code i} is the insertion position.
     */
    public static int find(final float[] a,final float v,final int size) {return find(a,v,0,size);}
    /**
     * @param a     An ascending sorted array.
     * @param v     The search key.
     * @param start The start of the search region, inclusive.
     * @param end   The end of the search region, exclusive.
     * @return      An arbitrary index {@code i} in {@code a} such that {@code start <= i && i < end && v == a[i]},
     *              if it exists. If not, {@code -(i + 1)} is returned where {@code i} is the insertion position.
     */
    public static int find(final float[] a,final float v,int start,int end)
    {
        while(start < end)
        {
            final int mid = (start + end - 1) >>> 1;

            if(a[mid] < v) start = mid + 1;
            else if(a[mid] > v) end = mid;
            else return mid;
        }
        return -(start + 1);
    }
    /**
     * @param a    An ascending sorted array.
     * @param v    The search key.
     * @param size The size of the search region.
     * @return     An arbitrary index {@code i} in {@code a} such that {@code 0 <= i && i < size && v == a[i]},
     *             if it exists. If not, {@code -(i + 1)} is returned where {@code i} is the insertion position.
     */
    public static int find(final double[] a,final double v,final int size) {return find(a,v,0,size);}
    /**
     * @param a     An ascending sorted array.
     * @param v     The search key.
     * @param start The start of the search region, inclusive.
     * @param end   The end of the search region, exclusive.
     * @return      An arbitrary index {@code i} in {@code a} such that {@code start <= i && i < end && v == a[i]},
     *              if it exists. If not, {@code -(i + 1)} is returned where {@code i} is the insertion position.
     */
    public static int find(final double[] a,final double v,int start,int end)
    {
        while(start < end)
        {
            final int mid = (start + end - 1) >>> 1;

            if(a[mid] < v) start = mid + 1;
            else if(a[mid] > v) end = mid;
            else return mid;
        }
        return -(start + 1);
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
            final int comp = Byte.compareUnsigned(a[mid],v);
            if(comp < 0) low = mid + 1;
            else if(comp > 0) high = mid - 1;
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
            final int comp = Short.compareUnsigned(a[mid],v);
            if(comp < 0) low = mid + 1;
            else if(comp > 0) high = mid - 1;
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
            final int comp = Integer.compareUnsigned(a[mid],v);
            if(comp < 0) low = mid + 1;
            else if(comp > 0) high = mid - 1;
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
            final int comp = Long.compareUnsigned(a[mid],v);
            if(comp < 0) low = mid + 1;
            else if(comp > 0) high = mid - 1;
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
}