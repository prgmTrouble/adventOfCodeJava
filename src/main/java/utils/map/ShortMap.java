package utils.map;

import java.util.function.Supplier;

import static java.lang.System.arraycopy;
import static utils.ArrayUtils.find;

@SuppressWarnings("unchecked")
public final class ShortMap<T>
{
    public short[] keys;
    public Object[] values;
    public int size = 0;
    public boolean grow = false;
    
    public ShortMap(final int capacity)
    {
        keys = new short[capacity];
        values = new Object[capacity];
    }
    
    public T get(final short key)
    {
        final int find = find(keys,key,size);
        return find < 0? null : (T)values[find];
    }
    public void putImpl(final short key,final T value,final int index)
    {
        final short[] oldK = keys;
        final Object[] oldV = values;
        if(grow && size == keys.length)
        {
            arraycopy(keys,0,keys = new short[size * 2],0,index);
            arraycopy(values,0,values = new Object[size * 2],0,index);
        }
        arraycopy(oldK,index,keys,index + 1,size - index);
        arraycopy(oldV,index,values,index + 1,size - index);
        keys[index] = key;
        values[index] = value;
        ++size;
    }
    public boolean put(final short key,final T value)
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
    public T remove(final short key)
    {
        final int find = find(keys,key,size);
        if(find < 0) return null;
        
        final T out = (T)values[find];
        final short[] oldK;
        final Object[] oldV;
        if(grow && size < keys.length / 4)
        {
            arraycopy(oldK = keys,0,keys = new short[oldK.length / 2],0,find);
            arraycopy(oldV = values,0,values = new Object[oldK.length / 2],0,find);
        }
        else
        {
            oldK = keys;
            oldV = values;
        }
        --size;
        arraycopy(oldK,find + 1,keys,find,size - find);
        arraycopy(oldV,find + 1,values,find,size - find);
        return out;
    }
    public T putIfAbsent(final short key,final Supplier<T> value)
    {
        final int find = find(keys,key,size);
        if(find >= 0) return (T)values[find];
        final T out = value.get();
        putImpl(key,out,-(find + 1));
        return out;
    }
}