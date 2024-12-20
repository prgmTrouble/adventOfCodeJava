package utils.queue;

public final class ByteQueue
{
    public static final byte NORMAL = 1,FULL = 2,GROW = 4;
    public byte[] data;
    public int read = 0,write = 0;
    public byte state = 0;
    
    public ByteQueue(final byte[] data) {this.data = data;}
    public ByteQueue(final int capacity) {this(new byte[capacity]);}
    
    public int size()
    {
        final int d = write - read,s = d >> 31;
        return ((-((state & FULL) >>> 1) | s) & data.length) + d * (s | (-d >>> 31));
    }
    public boolean empty() {return (state & ~GROW) == 0;}
    public void push(final byte v)
    {
        if((write + 1) % data.length == read)
            state = (byte)((state & GROW) | FULL);
        else
        {
            if(state == (FULL | GROW))
            {
                final byte[] next = new byte[data.length << 1];
                System.arraycopy(data,0,next,0,write);
                System.arraycopy(data,write,next,data.length + write,data.length - write);
                data = next;
            }
            state = NORMAL | GROW;
        }
        data[(write = (write % data.length) + 1)] = v;
    }
    public byte pop()
    {
        if(read + 1 == write) state &= GROW;
        else
        {
            assert (state & ~GROW) != 0;
            state = (byte)((state & GROW) | NORMAL);
        }
        final byte out = data[read];
        read = (read + 1) % data.length;
        return out;
    }
    public void clear() {read = write = 0; state &= GROW;}
}