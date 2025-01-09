import utils.BitSet;

import static java.nio.charset.StandardCharsets.UTF_8;
import static utils.HttpUtils.httpGet;

static int[] findPaths(final int[] map,final short start,final short end,final short width,final short height)
{
    // This could probably be solved more efficiently with a better pathfinding algorithm, but I use standard
    // DFS for simplicity. The array is 4x the size of the map because the direction from which a point is
    // visited from matters for the final score.
    final int[] path = new int[(width * height) << 2];
    Arrays.fill(path,Integer.MAX_VALUE);
    final int[] stk = new int[(width * height) << 2];
    path[stk[0] = ((start << 2) | 2)] = 2000;
    path[stk[1] = ((start << 2) | 1)] = 1000;
    path[stk[2] = ((start << 2) | 3)] = 1000;
    path[stk[3] = (start << 2)] = 0;
    int size = 4;
    do
    {
        --size;
        final byte dir = (byte)(stk[size] & 3);
        final short pos = (short)(stk[size] >>> 2),
                      r = (short)(pos / width),
                      c = (short)(pos % width);
        final int nextS = path[stk[size]] + 1;
        int nextP,msk = 1000 & -(~dir & 1);
        if((r + 1 < height & dir != 3) && (!BitSet.test(map,pos + width) & path[nextP = (((pos + width) << 2) | 1)] > nextS + msk))
        {
            path[nextP] = nextS + msk;
            if(pos + width != end)
                stk[size++] = nextP;
        }
        if((r > 0 & dir != 1) && (!BitSet.test(map,pos - width) & path[nextP = (((pos - width) << 2) | 3)] > nextS + msk))
        {
            path[nextP] = nextS + msk;
            if(pos - width != end)
                stk[size++] = nextP;
        }
        msk = 1000 & -(dir & 1);
        if((c + 1 < width & dir != 2) && (!BitSet.test(map,pos + 1) & path[nextP = ((pos + 1) << 2)] > nextS + msk))
        {
            path[nextP] = nextS + msk;
            if(pos + 1 != end)
                stk[size++] = nextP;
        }
        if((c > 0 & dir != 0) && (!BitSet.test(map,pos - 1) & path[nextP = (((pos - 1) << 2) | 2)] > nextS + msk))
        {
            path[nextP] = nextS + msk;
            if(pos - 1 != end)
                stk[size++] = nextP;
        }
    }
    while(size != 0);
    return path;
}

static int part1(final int[] path,final short end)
{
    // Return the best score for 'path[E_r][E_c]'
    return Math.min(Math.min(path[end << 2],path[(end << 2) | 1]),Math.min(path[(end << 2) | 2],path[(end << 2) | 3]));
}

static int part2(final int[] path,final int[] map,
                 final short start,final short end,
                 final short width,final short height,
                 final int bestScore)
{
    // The DFS algorithm populates an array where the best score to get to point (r,c) from direction 'd' is
    // recorded. It is trivial to prove that valid paths will strictly increase until the end is reached. In
    // addition, we know the score for the best path. Therefore, an optimal path is simply a path where the
    // end point has the best score and all other points (u->v) on the path must satisfy (v > u).
    Arrays.fill(map,0);
    final int[] stk = new int[(width * height) << 2];
    int size = 0;
    for(byte i = 0;i < 4;++i)
        if(path[(end << 2) | i] == bestScore)
            stk[size++] = (end << 2) | i;
    do
    {
        --size;
        final byte dir = (byte)(stk[size] & 3);
        final short pos = (short)(stk[size] >>> 2),
                      r = (short)(pos / width),
                      c = (short)(pos % width);
        final int currentScore = path[stk[size]];
        final short pos2,r2,c2;
        switch(dir)
        {
            case 0 -> {r2 = r; c2 = (short)(c - 1); pos2 = (short)(pos - 1);}
            case 1 -> {r2 = (short)(r - 1); c2 = c; pos2 = (short)(pos - width);}
            case 2 -> {r2 = r; c2 = (short)(c + 1); pos2 = (short)(pos + 1);}
            default -> {r2 = (short)(r + 1); c2 = c; pos2 = (short)(pos + width);}
        }
        BitSet.set(map,pos2);
        if(pos2 != start)
        {
            int nextD;
            if((r2 + 1 < height & dir != 1) && path[nextD = (pos2 << 2) | 3] < currentScore)
                stk[size++] = nextD;
            if((r2 > 0 & dir != 3) && path[nextD = (pos2 << 2) | 1] < currentScore)
                stk[size++] = nextD;
            if((c2 + 1 < width & dir != 0) && path[nextD = (pos2 << 2) | 2] < currentScore)
                stk[size++] = nextD;
            if((c2 > 0 & dir != 2) && path[nextD = pos2 << 2] < currentScore)
                stk[size++] = nextD;
        }
    }
    while(size != 0);
    for(final int m : map)
        size += Integer.bitCount(m);
    return size + 1;
}

static void main()
{
    final byte[] input = httpGet("https://adventofcode.com/2024/day/16/input").getBytes(UTF_8);
    short width;
    for(width = 0;width < input.length;++width)
        if(Character.isWhitespace(input[width]))
            break;
    short lineEnd;
    for(lineEnd = width;lineEnd < input.length;++lineEnd)
        if(!Character.isWhitespace(input[lineEnd]))
            break;
    width -= 2;
    final short height = (short)((input.length - 1 + lineEnd) / lineEnd - 2);
    short start = -1,end = -1;
    final int[] map = BitSet.create(height * width);
    for(short r = 0;r < height;++r)
        for(short c = 0;c < width;++c)
            switch(input[(r + 1) * lineEnd + c + 1])
            {
                case '#' -> BitSet.set(map,r * width + c);
                case 'S' -> start = (short)(r * width + c);
                case 'E' -> end = (short)(r * width + c);
            }
    assert start != -1 && end != -1;
    
    // Calculate an array 'path' such that 'path[r][c][d]' is the best possible score to reach
    // tile (r,c) facing direction 'd'.
    final int[] path = findPaths(map,start,end,width,height);
    final int bestScore = part1(path,end);
    System.out.printf("Part 1: %d\n",bestScore);
    System.out.printf("Part 2: %d\n",part2(path,map,start,end,width,height,bestScore));
}