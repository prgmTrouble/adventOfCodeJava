package utils.map;

import java.util.function.Supplier;

import static java.lang.System.arraycopy;
import static utils.ArrayUtils.find;

@SuppressWarnings("unchecked")
public final class ByteMap<T>
{
    public byte[] keys;
    public Object[] values;
    public int size = 0;
    public boolean grow = false;

    public ByteMap(final int capacity)
    {
        keys = new byte[capacity];
        values = new Object[capacity];
    }
    
    /** @return The value at the specified key, or {@code null} if not present. */
    public T get(final byte key)
    {
        final int find = find(keys,key,size);
        return find < 0? null : (T)values[find];
    }
    
    /**
     * Performs the {@code put} operation with the assumption that {@code index} is
     * the correct location in the backing arrays and the key does not already exist
     * in the map.
     */
    public void putImpl(final byte key,final T value,final int index)
    {
        final byte[] oldK = keys;
        final Object[] oldV = values;
        if(grow && size == keys.length)
        {
            arraycopy(keys,0,keys = new byte[size * 2],0,index);
            arraycopy(values,0,values = new Object[size * 2],0,index);
        }
        arraycopy(oldK,index,keys,index + 1,size - index);
        arraycopy(oldV,index,values,index + 1,size - index);
        keys[index] = key;
        values[index] = value;
        ++size;
    }
    
    /**
     * Associates the key with the specified value.
     * @return {@code true} if the map's size changed.
     */
    public boolean put(final byte key,final T value)
    {
        final int find = find(keys,key,size);
        if(find < 0)
        {
            putImpl(key,value,-(find + 1));
            return true;
        }
        values[find] = value;
        return false;
    }
    
    /**
     * Performs the {@code remove} operation on the specified index in the
     * backing array.
     */
    public void removeImpl(final int index)
    {
        final byte[] oldK;
        final Object[] oldV;
        if(grow && size < keys.length / 4)
        {
            arraycopy(oldK = keys,0,keys = new byte[oldK.length / 2],0,index);
            arraycopy(oldV = values,0,values = new Object[oldK.length / 2],0,index);
        }
        else
        {
            oldK = keys;
            oldV = values;
        }
        --size;
        arraycopy(oldK,index + 1,keys,index,size - index);
        arraycopy(oldV,index + 1,values,index,size - index);
    }
    
    /**
     * Removes the mapping associated with the specified key.
     * @return The value associated with {@code key}, or {@code null} if the map
     *         did not contain {@code key}.
     */
    public T remove(final byte key)
    {
        final int find = find(keys,key,size);
        if(find < 0) return null;

        final T out = (T)values[find];
        removeImpl(find);
        return out;
    }
    
    /**
     * Inserts the specified value if the key is not already present in the map.
     * @return The value associated with {@code key} after the insertion.
     */
    public T insert(final byte key,final Supplier<T> value)
    {
        final int find = find(keys,key,size);
        if(find >= 0) return (T)values[find];
        final T out = value.get();
        putImpl(key,out,-(find + 1));
        return out;
    }
    
    /**
     * Replaces the specified value if the key exists in the map.
     * @return The previous value associated with {@code key}, or {@code null} if the
     *         map did not contain {@code key}.
     */
    public T replace(final byte key,final Supplier<T> value)
    {
        final int find = find(keys,key,size);
        if(find < 0) return null;
        final T out = (T)values[find];
        values[find] = value.get();
        return out;
    }
}