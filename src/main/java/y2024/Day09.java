import utils.BitSet;

import static java.nio.charset.StandardCharsets.UTF_8;
import static utils.HttpUtils.httpGet;

/* See Day09.tex for a more intuitive description of the algorithms. */

static long part1(final byte[] in)
{
    long result = 0;
    short l = 0,r = (short)(in.length - 1);
    long x = 0;
    byte ar = (byte)(in[r] & 0xF);
    while(l < r)
    {
        final byte al = (byte)(in[l] & 0xF);
        byte bl = (byte)((in[l] >>> 4) & 0xF);
        /* Add the occupied blocks */
        result += l * (al * x + ((al * (al - 1L)) >>> 1));
        x += al;
        /* Loop through contiguous right-most file blocks that fit within the gap */
        while(ar <= bl)
        {
            result += r * (ar * x + ((ar * (ar - 1L)) >>> 1));
            x += ar;
            bl -= ar;
            if(--r <= l)
                return result;
            ar = (byte)(in[r] & 0xF);
        }
        /* Fill the remaining gap with the right-most file blocks */
        result += r * (bl * x + ((bl * (bl - 1L)) >>> 1));
        x += bl;
        ar -= bl;
        ++l;
    }
    /* Add any remaining file blocks */
    return result + r * (ar * x + ((ar * (ar - 1L)) >>> 1));
}

static long part2(final byte[] in)
{
    final int[] moved = BitSet.create(in.length);
    long result = 0,x = 0;
    short l = 0;
    while(true)
    {
        final byte al = (byte)(in[l] & 0xF);
        byte bl = (byte)((in[l] >>> 4) & 0xF);
        /* Add the occupied blocks */
        if(!BitSet.test(moved,l))
            result += l * (al * x + ((al * (al - 1L)) >>> 1));
        if(++l == in.length) break;
        x += al;
        endif:
        if(bl != 0)
        {
            /* Loop through contiguous right-most file blocks that fit within the gap */
            short r = (short)BitSet.lastUnsetBit(moved,l,in.length);
            while(r != -1)
            {
                final byte ar = (byte)(in[r] & 0xF);
                if(ar <= bl)
                {
                    BitSet.set(moved,r);
                    result += r * (ar * x + ((ar * (ar - 1L)) >>> 1));
                    x += ar;
                    if((bl -= ar) == 0) break endif; // basically just skips the (x += bl)
                }
                // (r == l) case is covered by (r != -1)
                r = (short)BitSet.lastUnsetBit(moved,l,r);
            }
            /* Increment the base index past the remaining gap */
            x += bl;
        }
    }
    return result;
}

static long part2optimized(final byte[] in)
{
    final int[] moved = BitSet.create(in.length);
    long result = 0,x = in[0] & 0xF;
    short l = 0;
    while(true)
    {
        byte bl = (byte)((in[l++] >>> 4) & 0xF);
        endif:
        if(bl != 0)
        {
            /* Loop through contiguous right-most file blocks that fit within the gap */
            short r = (short)BitSet.lastUnsetBit(moved,l,in.length);
            while(r != -1)
            {
                final byte ar = (byte)(in[r] & 0xF);
                if(ar <= bl)
                {
                    BitSet.set(moved,r);
                    result += r * (ar * x + ((ar * (ar - 1L)) >>> 1));
                    x += ar;
                    if((bl -= ar) == 0) break endif; // skip the (x += bl)
                }
                // (r == l) case is covered by (r != -1)
                r = (short)BitSet.lastUnsetBit(moved,l,r);
            }
            /* Increment the base index past the remaining gap */
            x += bl;
        }
        if(l == in.length) break;
        final byte al = (byte)(in[l] & 0xF);
        /* Add the occupied blocks */
        if(!BitSet.test(moved,l))
            result += l * (al * x + ((al * (al - 1L)) >>> 1));
        x += al;
    }
    return result;
}

static void main()
{
    byte[] input = httpGet("https://adventofcode.com/2024/day/9/input").getBytes(UTF_8);
    int end = input.length;
    while(Character.isWhitespace(input[end - 1]))
        --end;
    assert (end & 1) != 0;
    // Compress to binary coded decimal
    for(short i = 0;i < (end >>> 1);++i)
        input[i] = (byte)((input[i << 1] - '0') | ((input[(i << 1) + 1] - '0') << 4));
    input[end >>> 1] = (byte)(input[end - 1] - '0');
    System.arraycopy(input,0,input = new byte[(end >>> 1) + 1],0,input.length);
    
    System.out.printf("Part 1:%d\n",part1(input));
    System.out.printf("Part 2:%d\n",part2(input));
    System.out.printf("Part 2 (optimized):%d\n",part2optimized(input));
}