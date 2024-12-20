import utils.ArrayUtils;
import utils.BitSet;

import static java.nio.charset.StandardCharsets.UTF_8;
import static utils.HttpUtils.httpGet;

static short part1(final int[][] map,short pos,final short width)
{
    final int[][] trace = new int[map.length][map[0].length];
    outer:
    while(true)
    {
        short rawPos = (short)(pos & ((1 << 14) - 1)),
                   r = (short)(rawPos / width),
                   c = (short)(rawPos % width);
        switch((pos & 0xFFFF) >>> 14)
        {
            
            case 0 ->
            {
                // -r
                while(true)
                {
                    BitSet.set(trace[r],c);
                    if(r == 0)
                        break outer;
                    if(BitSet.test(map[r - 1],c))
                        break;
                    --r;
                }
                pos = (short)(3 << 14);
            }
            case 1 ->
            {
                // -c
                final short wall = (short)(BitSet.lastSetBit(map[r],0,c) + 1);
                BitSet.set(trace[r],wall,c);
                if(wall == 0)
                    break outer;
                pos = 0;
                c = wall;
            }
            case 2 ->
            {
                // +r
                while(true)
                {
                    BitSet.set(trace[r],c);
                    if(r == map.length - 1)
                        break outer;
                    if(BitSet.test(map[r + 1],c))
                        break;
                    ++r;
                }
                pos = (short)(1 << 14);
            }
            default ->
            {
                // +c
                final short wall = (short)BitSet.firstSetBit(map[r],c + 1,width);
                if(wall == -1)
                {
                    BitSet.set(trace[r],c,width);
                    break outer;
                }
                BitSet.set(trace[r],c,wall);
                pos = (short)(2 << 14);
                c = (short)(wall - 1);
            }
        }
        pos |= (short)(r * width + c);
    }
    
    short count = 0;
    for(final int[] r : trace)
        for(final int c : r)
            count += (byte)Integer.bitCount(c);
    return count;
}

static boolean checkLoop(final int[][] map,short pos,final short width,final short[] collisions,int collisionSize)
{
    // A loop is guaranteed if the guard collides with the same obstacle face more than once.
    byte dir = (byte)((pos & 0xFFFF) >> 14);
    short rawPos = (short)(pos & ((1 << 14) - 1)),
               r = (short)(rawPos / width),
               c = (short)(rawPos % width);
    while(true)
    {
        // Find the next collision.
        switch(dir)
        {
            case 0 ->
            {
                // -r
                while(true)
                {
                    if(r <= 1)
                        return false;
                    if(BitSet.test(map[r - 1],c))
                        break;
                    --r;
                }
                dir = 3;
            }
            case 1 ->
            {
                // -c
                c = (short)(BitSet.lastSetBit(map[r],0,c) + 1);
                if(c == 0)
                    return false;
                dir = 0;
            }
            case 2 ->
            {
                // +r
                while(true)
                {
                    if(r == map.length - 1)
                        return false;
                    if(BitSet.test(map[r + 1],c))
                        break;
                    ++r;
                }
                dir = 1;
            }
            default ->
            {
                // +c
                c = (short)BitSet.firstSetBit(map[r],c + 1,width);
                if(c == -1)
                    return false;
                --c;
                dir = 2;
            }
        }
        pos = (short)((r * width + c) | (dir << 14));
        int idx = ArrayUtils.find(collisions,pos,collisionSize);
        if(idx >= 0)
            // Collision was already found.
            return true;
        idx = -(idx + 1);
        if(collisionSize > idx)
            System.arraycopy(collisions,idx,collisions,idx + 1,collisionSize - idx);
        collisions[idx] = pos;
        ++collisionSize;
    }
}
static short part2(final int[][] map,short pos,final short width)
{
    int possibleCollisions = 0;
    for(final int[] r : map)
        for(final int c : r)
            possibleCollisions += Integer.bitCount(c);
    int collisionsSize = 0;
    final short[] collisions = new short[4 * (1 + possibleCollisions)],
                  collisionsCopy = new short[collisions.length];
    final int[][] trace = new int[map.length][map[0].length];
    
    byte dir = (byte)((pos & 0xFFFF) >>> 14);
    short rawPos = (short)(pos & ((1 << 14) - 1)),
               r = (short)(rawPos / width),
               c = (short)(rawPos % width);
    short count = 0;
    while(true)
    {
        BitSet.set(trace[r],c);
        // Find the next position (nr,nc) for which 'trace' and 'map' are zero.
        short nr = r,nc = c;
        byte ndir;
        switch(dir)
        {
            case 0 ->
            {
                // -r
                while(true)
                {
                    if(r == 0)
                        return count;
                    if(BitSet.test(map[r - 1],c))
                    {
                        ndir = 3;
                        break;
                    }
                    if(!BitSet.test(trace[r - 1],c))
                    {
                        ndir = (byte)(3 | (1 << 7));
                        nr = (short)(r - 1);
                        break;
                    }
                    --r;
                }
            }
            case 1 ->
            {
                // -c
                final short mc = (short)BitSet.lastSetBit(map[r],0,c),
                            tc = (short)BitSet.lastUnsetBit(trace[r],0,c);
                if(mc == -1 && tc == -1)
                    return count;
                if(tc != -1 && tc != mc)
                {
                    ndir = (byte)(1 << 7);
                    c = (short)(tc + 1);
                    nc = tc;
                }
                else
                {
                    c = (short)(mc + 1);
                    ndir = 0;
                }
            }
            case 2 ->
            {
                // +r
                while(true)
                {
                    if(r == map.length - 1)
                        return count;
                    if(BitSet.test(map[r + 1],c))
                    {
                        ndir = 1;
                        break;
                    }
                    if(!BitSet.test(trace[r + 1],c))
                    {
                        ndir = (byte)(1 | (1 << 7));
                        nr = (short)(r + 1);
                        break;
                    }
                    ++r;
                }
            }
            default ->
            {
                // +c
                final short mc = (short)BitSet.firstSetBit(map[r],c + 1,width),
                            tc = (short)BitSet.firstUnsetBit(trace[r],c + 1,width);
                if(mc == -1 && tc == -1)
                    return count;
                if(tc != -1 && tc != mc)
                {
                    ndir = (byte)(2 | (1 << 7));
                    c = (short)(tc - 1);
                    nc = tc;
                }
                else
                {
                    c = (short)(mc - 1);
                    ndir = 2;
                }
            }
        }
        rawPos = (short)(r * width + c);
        if((ndir & (1 << 7)) != 0)
        {
            // Valid obstacle placement found
            BitSet.set(map[nr],nc);
            System.arraycopy(collisions,0,collisionsCopy,0,collisionsSize);
            pos = (short)(rawPos | ((ndir & 0x7F) << 14));
            ArrayUtils.insertSorted(collisionsCopy,pos,collisionsSize);
            if(checkLoop(map,pos,width,collisionsCopy,collisionsSize + 1))
                ++count;
            BitSet.unset(map[r = nr],c = nc);
        }
        else
        {
            // Collision found
            ArrayUtils.insertSorted(collisions,(short)(rawPos | ((dir = ndir) << 14)),collisionsSize++);
        }
    }
}

static void main()
{
    final byte[] input = httpGet("https://adventofcode.com/2024/day/6/input").getBytes(UTF_8);
    short width;
    for(width = 0;width < input.length;++width)
        if(Character.isWhitespace(input[width]))
            break;
    short lineEnd;
    for(lineEnd = width;++lineEnd < input.length;)
        if(!Character.isWhitespace(input[lineEnd]))
            break;
    final int[][] map = new int[input.length / lineEnd][BitSet.arraySize(width)];
    short pos = 0;
    for(short r = 0;r < map.length;++r)
        for(short c = 0;c < width;++c)
            switch(input[r * lineEnd + c])
            {
                case '#' -> BitSet.set(map[r],c);
                case '^','>','<','v','V' -> pos = (short)((r * width + c) | switch(input[r * lineEnd + c])
                {
                    case '^' -> 0;
                    case '<' -> 1 << 14;
                    case 'v','V' -> 2 << 14;
                    default -> 3 << 14;
                });
                default -> {/* do nothing */}
            }
    
    System.out.printf("Part 1: %d\n",part1(map,pos,width));
    System.out.printf("Part 2: %d\n",part2(map,pos,width));
}