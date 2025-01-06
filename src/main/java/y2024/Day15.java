import utils.BitSet;

import static java.nio.charset.StandardCharsets.UTF_8;
import static utils.HttpUtils.httpGet;

static long part1(final int[] map,
                  final int[] box,
                  final byte[] moves,
                  final short moveSize,
                  final byte width,
                  final byte height,
                  byte r,byte c)
{
    for(short move = 0;move < moveSize;++move)
    {
        byte nr = r,nc = c;
        final short free = switch((moves[move >>> 2] >>> ((move & 3) << 1)) & 3)
        {
            case 0 ->
            {
                if(--nr >= 0)
                {
                    short fr = (short)(nr * width + c);
                    do if(!BitSet.test(box,fr)) yield fr;
                    while((fr -= width) > 0);
                }
                yield -1;
            }
            case 1 ->
            {
                if(++nr < height)
                {
                    short fr = (short)(nr * width + c);
                    do if(!BitSet.test(box,fr)) yield fr;
                    while((fr += width) < width * height);
                }
                yield -1;
            }
            case 2 -> --nc >= 0 ? (short)BitSet.lastUnsetBit(box,r * width,r * width + c) : -1;
            default -> ++nc < width ? (short)BitSet.firstUnsetBit(box,r * width + nc,(r + 1) * width) : -1;
        };
        if(free != -1 && !BitSet.test(map,free) && BitSet.test(box,(r = nr) * width + (c = nc)))
        {
            BitSet.unset(box,r * width + c);
            BitSet.set(box,free);
        }
    }
    final int[] filter = new int[32];
    for(byte i = 0;i < box.length;++i)
        for(byte j = 0;j < 32;++j)
            if((box[i] & (1 << j)) != 0)
            {
                final short k = (short)((i << 5) | j);
                filter[j] += 100 * (k / width) + (k % width) + 101;
            }
    long result = 0;
    for(final int f : filter)
        result += f;
    return result;
}

static long part2(final int[] map,
                  final int[] box,
                  final byte[] moves,
                  final short moveSize,
                  final byte width,
                  final byte height,
                  byte r,byte c)
{
    // This could be optimized by combining other bitwise operations with shifting operators
    // instead of using the Bitset::lsh and Bitset::rsh functions, but I can't be bothered.
    final int size = BitSet.arraySize(width * height);
    final int[] parity = new int[size],
                  push = new int[size];
    final int[][] work = new int[5][size];
    c <<= 1;
    moving:
    for(short move = 0;move < moveSize;++move)
    {
        final byte dir = (byte)((moves[move >>> 2] >>> ((move & 3) << 1)) & 3);
        switch(dir)
        {
            case 0,1 ->
            {
                byte nr = r;
                if(dir == 0)
                {
                    if(r == 0) break;
                    --nr;
                }
                else if(++nr == height) break;
                
                // Get the parities of the (north/south) and (north/south)-west boxes
                final byte initial = (byte)
                (
                    // ((~P[nr][C/2] | C) & B[nr][C/2]) << 1
                    (
                        (
                            ((BitSet.get(parity,nr * width + (c >>> 1)) ^ 1) | c) &
                            BitSet.get(box,nr * width + (c >>> 1))
                        ) << 1
                    ) |
                    // P[nr][C/2-1] & ~C
                    (c > 1 ? BitSet.get(parity,nr * width + (c >>> 1) - 1) & ~c : 0)
                );
                assert initial != 3 : "Overlapping boxes";
                if(initial == 0)
                {
                    // No boxes to push: check for wall and move
                    if(!BitSet.test(map,nr * width + (c >>> 1)))
                        if(dir == 0) --r;
                        else ++r;
                    continue;
                }
                final short first = (short)(nr * width + (c >>> 1) - 2 + initial);
                BitSet.select(push,box,first);
                
                // Initialize T[r]
                Arrays.fill(work[4],0);
                work[4][first >>> 5] = push[first >>> 5];
                // Initialize P[r]
                BitSet.select(work[0],parity,first);
                
                byte currentParity = 0;
                while(dir == 0? (nr-- != 0) : (++nr != height))
                {
                    // Get next row
                    // P[nr]
                    BitSet.mask(work[1 - currentParity],parity,nr * width,(nr + 1) * width);
                    
                    // a = T[r] & ~P[r]
                    BitSet.andNot(work[2],work[4],work[currentParity]);
                    // b = T[r] & P[r]
                    BitSet.and(work[3],work[4],work[currentParity]);
                    
                    // a >>= 1, b <<= 1, align T[r],a,b with [nr]
                    if(dir == 0)
                    {
                        BitSet.rsh(work[2],width + 1);
                        BitSet.rsh(work[3],width - 1);
                        BitSet.rsh(work[4],width);
                    }
                    else
                    {
                        BitSet.lsh(work[2],width - 1);
                        BitSet.lsh(work[3],width + 1);
                        BitSet.lsh(work[4],width);
                    }
                    
                    // T[nr] = B[nr] & (T[r] | (a & P[nr]) | (b & ~P[nr]))
                    for(int i = 0;i < size;++i)
                        work[4][i] =
                            box[i] &
                            (
                                work[4][i] |
                                (work[2][i] & work[1 - currentParity][i]) |
                                (work[3][i] & ~work[1 - currentParity][i])
                            );
                    BitSet.mask(work[4],nr * width,(nr + 1) * width);
                    
                    currentParity ^= 1;
                    int i;
                    for(i = 0;i < size;++i)
                        if(work[4][i] != 0)
                            break;
                    if(i == size)
                    {
                        // No more boxes to push: check for walls and move
                        
                        // Check for walls
                        System.arraycopy(push,0,work[0],0,size);
                        BitSet.and(work[1],work[0],parity);
                        if(dir == 0)
                        {
                            BitSet.rsh(push,width);
                            BitSet.rsh(work[1],width);
                        }
                        else
                        {
                            BitSet.lsh(push,width);
                            BitSet.lsh(work[1],width);
                        }
                        // Wall = (T & (M | ((M >> 1) & P))) != 0
                        for(i = 0;i < size;++i)
                            if((push[i] & (map[i] | (((map[i] >>> 1) | (i + 1 < size? map[i + 1] << 31 : 0)) & work[1][i]))) != 0)
                                // Wall is blocking movement
                                continue moving;
                        
                        // Move the boxes
                        for(i = 0;i < size;++i)
                        {
                            parity[i] = (parity[i] & ~work[0][i]) | work[1][i];
                            box[i] = (box[i] & ~work[0][i]) | push[i];
                        }
                        if(dir == 0) --r;
                        else ++r;
                        continue moving;
                    }
                    // Add T[r-1] to push
                    for(;i < size;++i)
                        push[i] |= work[4][i];
                }
            }
            case 2 ->
            {
                if(c == 0)
                    // Map edge blocks movement
                    continue;
                final short start = (short)(r * width),end = (short)(start + (c >>> 1));
                if((c & 1) == 0)
                {
                    final short m = (short)BitSet.lastSetBit(map,start,end),
                                b = (short)BitSet.lastUnsetBit(box,start,end);
                    if(m == b)
                        // Wall or map edge blocks movement
                        continue;
                    --c;
                    if(b + 1 == end)
                        // No boxes to push
                        continue;
                    // We know that all boxes in range (b,end) must have 0 parity since they would
                    // otherwise overlap. We also know that (b >= start) for a few reasons:
                    // - (m == -1) implies that there is no wall in the west
                    // - (m != b) implies (b > -1)
                    // - '-1' is the smallest value that 'b' could have apart from 'start'
                    BitSet.set(box,b);
                    BitSet.unset(box,end - 1);
                    BitSet.set(parity,b,end - 1);
                }
                else
                {
                    final short p = (short)Math.max(BitSet.lastUnsetBit(parity,start,end),start - 1);
                    // If there are any boxes adjacent to the bot, then they will always be pushable
                    // since boxes cannot occupy an index containing a wall regardless of parity.
                    if(p + 1 != end)
                        BitSet.unset(parity,p + 1,end);
                    c &= -2;
                }
            }
            default ->
            {
                final short start = (short)(r * width + (c >>> 1) + 1),end = (short)((r + 1) * width);
                if(start == end)
                    // Map edge blocks push
                    c |= 1;
                else if((c & 1) == 1)
                {
                    final short m = (short)BitSet.firstSetBit(map,start,end),
                                b = (short)BitSet.firstUnsetBit(box,start,end),
                                p = (short)BitSet.firstSetBit(parity,start,end);
                    if(b == m)
                        // Wall or map edge blocks push
                        continue;
                    if(b != start & p != start)
                        BitSet.set(parity,start,p != -1 & p < b? p : b);
                    ++c;
                }
                else
                {
                    final short p = (short)BitSet.firstUnsetBit(parity,start - 1,end);
                    // If there are any boxes adjacent to the bot, then they will always be pushable
                    // since boxes cannot occupy an index containing a wall regardless of parity.
                    if(p != start - 1)
                    {
                        BitSet.unset(parity,start - 1,p);
                        BitSet.unset(box,start - 1);
                        BitSet.set(box,p);
                    }
                    c |= 1;
                }
            }
        }
    }
    final int[] filter = new int[32];
    for(byte i = 0;i < box.length;++i)
        for(byte j = 0;j < 32;++j)
            if((box[i] & (1 << j)) != 0)
            {
                final short k = (short)((i << 5) | j);
                filter[j] += 100 * (k / width) + ((k % width) << 1) + ((parity[i] >>> j) & 1) + 102;
            }
    long result = 0;
    for(final int f : filter)
        result += f;
    return result;
}

static void main()
{
    final byte[] input = httpGet("https://adventofcode.com/2024/day/15/input").getBytes(UTF_8);
    byte width;
    for(width = 0;width < input.length;++width)
        if(Character.isWhitespace(input[width]))
            break;
    byte lineEnd;
    for(lineEnd = width;lineEnd < input.length;++lineEnd)
        if(!Character.isWhitespace(input[lineEnd]))
            break;
    byte height;
    for(height = 0;height < input.length / lineEnd;++height)
        if(Character.isWhitespace(input[height * lineEnd]))
            // The map end is marked by two line breaks
            break;
    // The entire border is walled off, so we can ignore it
    final int[] map = BitSet.create((height - 2) * (width - 2)),
                box = BitSet.create((height - 2) * (width - 2));
    byte bR = -1,bC = -1;
    for(byte r = 1;r < height - 1;++r)
        for(byte c = 1;c < width - 1;++c)
            switch(input[r * lineEnd + c])
            {
                case '#' -> BitSet.set(map,(r - 1) * (width - 2) - 1 + c);
                case 'O' -> BitSet.set(box,(r - 1) * (width - 2) - 1 + c);
                case '@' -> {bR = (byte)(r - 1); bC = (byte)(c - 1);}
            }
    assert bR != -1 && bC != -1;
    short move = 0;
    byte[] moves = new byte[(input.length - height * lineEnd + 3) >> 2];
    for(int i = height * lineEnd;i < input.length;++i)
        switch(input[i])
        {
            case '^' -> ++move;
            case 'v' -> moves[move >>> 2] |= (byte)(1 << ((move++ & 3) << 1));
            case '<' -> moves[move >>> 2] |= (byte)(2 << ((move++ & 3) << 1));
            case '>' -> moves[move >>> 2] |= (byte)(3 << ((move++ & 3) << 1));
        }
    System.arraycopy(moves,0,moves = new byte[(move >>> 2) + ((move | (move >>> 1)) & 1)],0,moves.length);
    
    final int[] box2 = new int[box.length];
    System.arraycopy(box,0,box2,0,box.length);
    System.out.printf("Part 1: %d\n",part1(map,box,moves,move,(byte)(width - 2),(byte)(height - 2),bR,bC));
    System.out.printf("Part 2: %d\n",part2(map,box2,moves,move,(byte)(width - 2),(byte)(height - 2),bR,bC));
}