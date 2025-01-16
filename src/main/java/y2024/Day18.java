import utils.BitSet;

import static java.nio.charset.StandardCharsets.UTF_8;
import static utils.HttpUtils.httpGet;

static final byte DIM = 71;

@FunctionalInterface interface Heuristic {short cost(short pos);}
static short findPath(final short[] path,final short[] score,final int[] open,final Heuristic heuristic)
{
    while(true)
    {
        short current = (short)BitSet.firstSetBit(open);
        if(current == -1)
            return -1;
        short tmpP = (short)BitSet.firstSetBit(open,current + 1,DIM * DIM),
              tmpS = (short)(score[current] + heuristic.cost(current));
        while(tmpP != -1)
        {
            final short tmpN = (short)(score[tmpP] + heuristic.cost(tmpP));
            if(tmpN < tmpS)
            {
                current = tmpP;
                tmpS = tmpN;
            }
            tmpP = (short)BitSet.firstSetBit(open,tmpP + 1,DIM * DIM);
        }
        if(heuristic.cost(current) == 0)
            return current;
        BitSet.unset(open,current);
        tmpS = (short)(score[current] + 1);
        if((tmpP = (short)(current + DIM)) < DIM * DIM && tmpS < score[tmpP])
        {
            path[tmpP] = current;
            score[tmpP] = tmpS;
            BitSet.set(open,tmpP);
        }
        if((tmpP = (short)(current - DIM)) >= 0 && tmpS < score[tmpP])
        {
            path[tmpP] = current;
            score[tmpP] = tmpS;
            BitSet.set(open,tmpP);
        }
        if((tmpP = (short)(current + 1)) < (current - (current % DIM) + DIM) && tmpS < score[tmpP])
        {
            path[tmpP] = current;
            score[tmpP] = tmpS;
            BitSet.set(open,tmpP);
        }
        if((tmpP = (short)(current - 1)) >= (current - (current % DIM)) && tmpS < score[tmpP])
        {
            path[tmpP] = current;
            score[tmpP] = tmpS;
            BitSet.set(open,tmpP);
        }
    }
}

static short part1(final short[] corruption)
{
    final short[] path = new short[DIM * DIM],
                 score = new short[DIM * DIM];
    Arrays.fill(path,(short)-1);
    Arrays.fill(score,Short.MAX_VALUE);
    score[0] = 0;
    for(short c = 0;c < 1024;++c)
        score[corruption[c]] = -1;
    final int[] open = BitSet.create(DIM * DIM);
    open[0] = 1;
    short current = findPath(path,score,open,pos -> (short)((DIM << 1) - (pos / DIM) - (pos % DIM) - 2));
    if(current == -1)
        return -1;
    
    short tmpS = 0;
    while((current = path[current]) != -1)
        ++tmpS;
    return tmpS;
}

static short part2(final short[] corruption)
{
    // If a corruption lands on index 'k' of an arbitrary valid and simple (i.e. with no self-intersections) path,
    // then all points excluding 'k' are still valid. The most common case is that the two points on either side of
    // 'k' can be easily connected with a small adjustment to the original path, so it is typically more efficient
    // to calculate a path for [k-1,k+1] than to recalculate the entire path.
    final int[] open = BitSet.create(DIM * DIM);
    final short[] path = new short[DIM * DIM];
    final short[][] work = new short[2][DIM * DIM],
                   score = new short[2][DIM * DIM];
    short p;
    for(p = 0;p < DIM;++p)
        path[p] = p;
    for(short q = (DIM << 1) - 1;q < DIM * DIM;q += DIM)
        path[p++] = q;
    Arrays.fill(score[0],Short.MAX_VALUE);
    short i;
    for(i = 0;i < corruption.length;++i)
    {
        if(corruption[i] == 0 | corruption[i] == DIM * DIM - 1)
            // Corrupted start or end
            return i;
        
        score[0][corruption[i]] = -1;
        for(short k = 1;k < p - 1;++k)
            if(path[k] == corruption[i])
            {
                // Find path [k-1,k+1] and augment
                Arrays.fill(work[0],(short)-1);
                System.arraycopy(score[0],0,score[1],0,DIM * DIM);
                score[1][path[k - 1]] = 0;
                Arrays.fill(open,0);
                BitSet.set(open,path[k - 1]);
                final byte targetR = (byte)(path[k + 1] / DIM),
                           targetC = (byte)(path[k + 1] % DIM);
                short current = findPath
                (
                    work[0],
                    score[1],
                    open,
                    pos ->
                    {
                        final byte r = (byte)(pos / DIM),
                                   c = (byte)(pos % DIM);
                        return (short)Math.min
                        (
                            (DIM << 1) - r - c - 2,
                            Math.abs(targetR - r) + Math.abs(targetC - c)
                        );
                    }
                );
                if(current == -1)
                    // Exit is completely blocked
                    return i;
                // Trace path [k-1,k+1]
                short size = 0;
                do
                {
                    work[1][DIM * DIM - 1 - size++] = current;
                    current = work[0][current];
                    if(current == path[k - 1])
                    {
                        work[1][DIM * DIM - 1 - size++] = current;
                        break;
                    }
                }
                while(current != -1);
                
                final short offset = (short)(DIM * DIM - size);
                // Find the latest intersection for paths [0,k-1] and [k-1,k+1]
                short iA,iB0 = 0;
                intersect:
                for(iA = 0;iA < k;++iA)
                    for(iB0 = size;iB0-- > 0;)
                        if(path[iA] == work[1][offset + iB0])
                            break intersect;
                // Find the earliest intersection for paths [k-1,k+1] and [k+1,p]
                short iB1,iC = (short)(p - 1);
                intersect:
                for(iB1 = iB0;++iB1 < size;)
                    for(iC = p;--iC > k;)
                        if(path[iC] == work[1][offset + iB1])
                            break intersect;
                
                // Augment path
                System.arraycopy(path,iC,path,iA - iB0 + iB1,p - iC);
                System.arraycopy(work[1],offset + iB0,path,iA,iB1 - iB0);
                p = (short)(p - iC + iB1 - iB0 + iA);
                break;
            }
    }
    return i;
}

static void main()
{
    final byte[] input = httpGet("https://adventofcode.com/2024/day/18/input").getBytes(UTF_8);
    int i = 0;
    short write = 0;
    while(i < input.length)
    {
        while(i < input.length && ('9' < input[i] | input[i] < '0'))
            ++i;
        byte tmp = 0;
        while(i < input.length && ('0' <= input[i] & input[i] <= '9'))
            tmp = (byte)(tmp * 10 - '0' + input[i++]);
        input[write++] = tmp;
    }
    assert (write & 1) == 0;
    final short[] corruption = new short[write >>> 1];
    for(i = 0;i < corruption.length;++i)
        corruption[i] = (short)(input[i << 1] + DIM * input[(i << 1) + 1]);
    
    System.out.printf("Part 1: %d\n",part1(corruption));
    final short p2 = part2(corruption);
    final byte r = (byte)(corruption[p2] / DIM),
               c = (byte)(corruption[p2] % DIM);
    System.out.printf("Part 2: %d,%d\n",c,r);
}