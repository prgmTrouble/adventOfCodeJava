package utils.map;

import java.util.function.Supplier;

import static java.lang.System.arraycopy;
import static utils.ArrayUtils.find;

@SuppressWarnings("unchecked")
public final class FloatMap<T>
{
    public float[] keys;
    public Object[] values;
    public int size = 0;
    public boolean grow = false;

    public FloatMap(final int capacity)
    {
        keys = new float[capacity];
        values = new Object[capacity];
    }

    public T get(final float key)
    {
        final int find = find(keys,key,size);
        return find < 0? null : (T)values[find];
    }
    public void putImpl(final float key,final T value,final int index)
    {
        final float[] oldK = keys;
        final Object[] oldV = values;
        if(grow && size == keys.length)
        {
            arraycopy(keys,0,keys = new float[size * 2],0,index);
            arraycopy(values,0,values = new Object[size * 2],0,index);
        }
        arraycopy(oldK,index,keys,index + 1,size - index);
        arraycopy(oldV,index,values,index + 1,size - index);
        keys[index] = key;
        values[index] = value;
        ++size;
    }
    public boolean put(final float key,final T value)
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
    public void removeImpl(final int index)
    {
        final float[] oldK;
        final Object[] oldV;
        if(grow && size < keys.length / 4)
        {
            arraycopy(oldK = keys,0,keys = new float[oldK.length / 2],0,index);
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
    public T remove(final float key)
    {
        final int find = find(keys,key,size);
        if(find < 0) return null;

        final T out = (T)values[find];
        removeImpl(find);
        return out;
    }
    public T insert(final float key,final Supplier<T> value)
    {
        final int find = find(keys,key,size);
        if(find >= 0) return (T)values[find];
        final T out = value.get();
        putImpl(key,out,-(find + 1));
        return out;
    }
    public T replace(final float key,final Supplier<T> value)
    {
        final int find = find(keys,key,size);
        if(find < 0) return null;
        final T out = (T)values[find];
        values[find] = value.get();
        return out;
    }
}