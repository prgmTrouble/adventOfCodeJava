package utils.queue;

import static utils.queue.Queue.*;

public final class FloatQueue
{
    public float[] data;
    public int read = 0,write = 0;
    public byte state = 0;

    public FloatQueue(final float[] data) {this.data = data;}
    public FloatQueue(final int capacity) {this(new float[capacity]);}

    /** @return The number of elements in the queue. */
    public int size()
    {
        final int d = write - read,s = d >> 31;
        return ((-((state & FULL) >>> 1) | s) & data.length) + d * (s | (-d >>> 31));
    }
    /** @return {@code true} if this queue is empty. */
    public boolean empty() {return (state & ~GROW) == 0;}
    /** Adds {@code v} to the head of the queue. */
    public void push(final float v)
    {
        if((write + 1) % data.length == read)
            state = (byte)((state & GROW) | FULL);
        else
        {
            if(state == (FULL | GROW))
            {
                final float[] next = new float[data.length << 1];
                System.arraycopy(data,0,next,0,write);
                System.arraycopy(data,write,next,data.length + write,data.length - write);
                data = next;
            }
            state = NORMAL | GROW;
        }
        data[(write = (write % data.length) + 1)] = v;
    }
    /** Removes and returns the tail from the queue. */
    public float pop()
    {
        if(read + 1 == write) state &= GROW;
        else
        {
            assert (state & ~GROW) != 0;
            state = (byte)((state & GROW) | NORMAL);
        }
        final float out = data[read];
        read = (read + 1) % data.length;
        return out;
    }
    /** Empties the queue. */
    public void clear() {read = write = 0; state &= GROW;}
}