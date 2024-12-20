package utils.queue;

@SuppressWarnings("unchecked")
public final class Queue<T>
{
    public static final byte NORMAL = 1,FULL = 2,GROW = 4;
    public Object[] data;
    public int read = 0,write = 0;
    public byte state = 0;
    
    public Queue(final T[] data) {this.data = data;}
    public Queue(final int capacity) {this((T[])new Object[capacity]);}
    
    public int size()
    {
        final int d = write - read,s = d >> 31;
        return ((-((state & FULL) >>> 1) | s) & data.length) + d * (s | (-d >>> 31));
    }
    public boolean empty() {return (state & ~GROW) == 0;}
    public void push(final T v)
    {
        if((write + 1) % data.length == read)
            state = (byte)((state & GROW) | FULL);
        else
        {
            if(state == (FULL | GROW))
            {
                final Object[] next = new Object[data.length << 1];
                System.arraycopy(data,0,next,0,write);
                System.arraycopy(data,write,next,data.length + write,data.length - write);
                data = next;
            }
            state = NORMAL | GROW;
        }
        data[(write = (write % data.length) + 1)] = v;
    }
    public T pop()
    {
        if(read + 1 == write) state &= GROW;
        else
        {
            assert (state & ~GROW) != 0;
            state = (byte)((state & GROW) | NORMAL);
        }
        final T out = (T)data[read];
        read = (read + 1) % data.length;
        return out;
    }
    public void clear() {read = write = 0; state &= GROW;}
}