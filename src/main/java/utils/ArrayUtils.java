package utils;

import java.lang.reflect.Array;
import java.util.Arrays;

public final class ArrayUtils
{
    private ArrayUtils() {}
    
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
    
    public static byte[] append(byte[] a,final byte v,final int size)
    {
        if(size == a.length)
            System.arraycopy(a,0,a = new byte[size * 2],0,size);
        a[size] = v;
        return a;
    }
    public static short[] append(short[] a,final short v,final int size)
    {
        if(size == a.length)
            System.arraycopy(a,0,a = new short[size * 2],0,size);
        a[size] = v;
        return a;
    }
    public static int[] append(int[] a,final int v,final int size)
    {
        if(size == a.length)
            System.arraycopy(a,0,a = new int[size * 2],0,size);
        a[size] = v;
        return a;
    }
    public static long[] append(long[] a,final long v,final int size)
    {
        if(size == a.length)
            System.arraycopy(a,0,a = new long[size * 2],0,size);
        a[size] = v;
        return a;
    }
    @SuppressWarnings("unchecked")
    public static <T> T[] append(T[] a,final T v,final int size,final Class<? extends T> cls)
    {
        if(size == a.length)
            System.arraycopy(a,0,a = (T[])Array.newInstance(cls,size * 2),0,size);
        a[size] = v;
        return a;
    }
}
