import static java.lang.Math.abs;
import static java.lang.Math.min;
import static java.nio.charset.StandardCharsets.UTF_8;
import static utils.HttpUtils.httpGet;

static byte state(final byte c)
{
    return switch(c)
    {
        case 'A' -> 0;
        case '^' -> 1;
        case '>' -> 2;
        case 'v' -> 3;
        case '<' -> 4;
        default -> (byte)(c - '0' + 1);
    };
}
static byte digitRow(final byte s)
{
    return switch(s)
    {
        case 0,1 -> 0;
        case 2,3,4 -> 1;
        case 5,6,7 -> 2;
        default -> 3;
    };
}
static byte digitCol(final byte s)
{
    return switch(s)
    {
        case 2,5,8 -> 0;
        case 1,3,6,9 -> 1;
        default -> 2;
    };
}
static byte arrowRow(final byte s)
{
    return switch(s)
    {
        case 2,3,4 -> 0;
        default -> 1;
    };
}
static byte arrowCol(final byte s)
{
    return switch(s)
    {
        case 4 -> 0;
        case 1,3 -> 1;
        default -> 2;
    };
}

static byte[][][] lut()
{
    /*
      LUT format:
        1: 1 if the direction is not reversible (paths intersecting the blank spot are invalid)
        1: 1 if the horizontal displacement is first to avoid the blank spot
        2: size of horizontal displacement
        2: size of vertical displacement
        1: col(src) > col(dst)
        1: row(src) > row(dst)
    */
    final byte[][] digits = new byte[11][11];
    for(byte src = 0;src < 11;++src)
        for(byte dst = 0;dst < 11;++dst)
        {
            final byte sr = digitRow(src),sc = digitCol(src),
                       dr = digitRow(dst),dc = digitCol(dst);
            digits[src][dst] = (byte)
            (
                ((sr == 0 & dc == 0) | (sc == 0 & dr == 0) ? 1 << 7 : 0) |
                (sc == 0 & dr == 0 ? 1 << 6 : 0) |
                (abs(sc - dc) << 4) |
                (abs(sr - dr) << 2) |
                (sc > dc ? 1 << 1 : 0) |
                (sr > dr ? 1 : 0)
            );
        }
    final byte[][] arrows = new byte[5][5];
    for(byte src = 0;src < 5;++src)
        for(byte dst = 0;dst < 5;++dst)
        {
            final byte sr = arrowRow(src),sc = arrowCol(src),
                       dr = arrowRow(dst),dc = arrowCol(dst);
            arrows[src][dst] = (byte)
            (
                ((sr == 1 & dc == 0) | (sc == 0 & dr == 1) ? 1 << 7 : 0) |
                (sc == 0 & dr == 1 ? 1 << 6 : 0) |
                (abs(sc - dc) << 4) |
                (abs(sr - dr) << 2) |
                (sc > dc ? 1 << 1 : 0) |
                (sr > dr ? 1 : 0)
            );
        }
    return new byte[][][] {digits,arrows};
}
static final byte[][][] LUT = lut();

static long solution(final byte[] input,final byte depth)
{
    /*
    This algorithm exploits a few fundamental facts about the problem space:
        1. A keystroke on the current layer is translated into a certain
           number of traversal keystrokes followed by a certain number of
           'A' keystrokes.
        2. The optimal set of traversal keystrokes will contain either one
           or two unique keys.
        3. The optimal set of traversal keystrokes are in sorted order (i.e.
           each key appears as a contiguous sequence in the result).
    These rules mean that the search space can be drastically cut down to only
    look at a maximum of two paths between any two keys. Additionally, it is
    possible to memoize each layer of recursion since each robot must return
    to the 'A' key.
    
    The iterative algorithm is functionally equivalent to the following:
        def helper(p,c,d,count,maxDepth):
            if(d - 1 == 0)
                return count
            path = (d == maxDepth? DIGIT_LUT : ARROW_LUT)[p][c]
            left = 0
            p2 = state[A]
            for(element in path[0])
                left += helper(p2,p2 = element.direction,d - 1,element.count,maxDepth)
            left += helper(p2,state[A],d - 1,count,maxDepth)
            right = 0
            p2 = state[A]
            for(element in path[1])
                right += helper(p2,p2 = element.direction,d - 1,element.count,maxDepth)
            right += helper(p2,state[A],d - 1,count,maxDepth)
            return min(left,right)
        
        def solution(str,maxDepth):
            complexity = 0
            p = state[A]
            for(key in str)
                complexity += helper(p,p = key,maxDepth,1,maxDepth)
            return int(str[:-1]) * complexity
    */
    
    final long[][][][] memo = new long[depth][][][];
    memo[depth - 1] = new long[11][11][4];
    for(byte d = 0;d < depth - 1;++d)
        memo[d] = new long[5][5][4];
    for(final long[][][] a : memo)
        for(final long[][] b : a)
            for(final long[] c : b)
                Arrays.fill(c,-1L);
    
    short stkCap = 1 << 5;
    /*
      stack variable format:
        4: previous state
        4: current state
        5: depth
        2: count
        1: phase
    */
    short[] stkVar = new short[stkCap];
    /*
      source stack position:
        - if negative: add result to stkR[-(x+1)]
        - otherwise: add result to stkL[x]
    */
    int[] stkSrc = new int[stkCap];
    long[] stkL = new long[stkCap],
           stkR = new long[stkCap];
    
    long out = 0;
    int i = 0;
    do
    {
        short coefficient = 0;
        long complexity = 0;
        
        byte p0 = state((byte)'A');
        while(i < input.length && !Character.isWhitespace(input[i]))
        {
            if(input[i] != 'A')
                coefficient = (short)(10 * coefficient - '0' + input[i]);
            byte stkSize = 1;
            stkVar[0] = (short)((p0 << 12) | ((p0 = state(input[i++])) << 8) | (depth << 3) | (1 << 1));
            while(true)
            {
                --stkSize;
                final byte p = (byte)((stkVar[stkSize] >>> 12) & 0b1111),
                           c = (byte)((stkVar[stkSize] >>> 8) & 0b1111),
                           d = (byte)((stkVar[stkSize] >>> 3) & 0b11111),
                           count = (byte)((stkVar[stkSize] >>> 1) & 0b11),
                           phase = (byte)(stkVar[stkSize] & 0b1);
                long m = memo[d - 1][p][c][count];
                if(m == -1)
                {
                    if(d - 1 == 0)
                        m = count;
                    else
                    {
                        final byte path = LUT[d < depth? 1 : 0][p][c],
                                   h = (byte)((path >>> 4) & 3),
                                   v = (byte)((path >>> 2) & 3),
                                   hs = state((byte)((path & (1 << 1)) == 0 ? '>' : '<')),
                                   vs = state((byte)((path & 1) == 0 ? '^' : 'v'));
                        if(phase == 0)
                        {
                            final byte current = stkSize++;
                            // Grow arrays when necessary.
                            if(stkSize + 5 >= stkCap)
                            {
                                stkCap <<= 1;
                                System.arraycopy(stkVar,0,stkVar = new short[stkCap],0,stkSize);
                                System.arraycopy(stkSrc,0,stkSrc = new int[stkCap],0,stkSize);
                                System.arraycopy(stkL,0,stkL = new long[stkCap],0,stkSize);
                                System.arraycopy(stkR,0,stkR = new long[stkCap],0,stkSize);
                            }
                            // Update current stack position to the reduction phase.
                            stkVar[current] |= 1;
                            // Push the (A -> vertical -> horizontal -> A) path.
                            if(((~path & (1 << 7)) | (~path & (1 << 6))) != 0)
                            {
                                stkL[current] = 0;
                                byte c2 = state((byte)'A');
                                byte count2 = count;
                                if(h != 0)
                                {
                                    stkVar[stkSize] = (short)((hs << 12) | (c2 << 8) | ((d - 1) << 3) | (count2 << 1));
                                    stkSrc[stkSize] = current;
                                    ++stkSize;
                                    c2 = hs;
                                    count2 = h;
                                }
                                if(v != 0)
                                {
                                    stkVar[stkSize] = (short)((vs << 12) | (c2 << 8) | ((d - 1) << 3) | (count2 << 1));
                                    stkSrc[stkSize] = current;
                                    ++stkSize;
                                    c2 = vs;
                                    count2 = v;
                                }
                                stkVar[stkSize] = (short)((state((byte)'A') << 12) | (c2 << 8) | ((d - 1) << 3) | (count2 << 1));
                                stkSrc[stkSize] = current;
                                ++stkSize;
                            }
                            else
                                stkL[current] = Long.MAX_VALUE;
                            // Push the (A -> horizontal -> vertical -> A) path.
                            if(((~path & (1 << 7)) | (path & (1 << 6))) != 0)
                            {
                                stkR[current] = 0;
                                byte c2 = state((byte)'A');
                                byte count2 = count;
                                if(v != 0)
                                {
                                    stkVar[stkSize] = (short)((vs << 12) | (c2 << 8) | ((d - 1) << 3) | (count2 << 1));
                                    stkSrc[stkSize] = -(current + 1);
                                    ++stkSize;
                                    c2 = vs;
                                    count2 = v;
                                }
                                if(h != 0)
                                {
                                    stkVar[stkSize] = (short)((hs << 12) | (c2 << 8) | ((d - 1) << 3) | (count2 << 1));
                                    stkSrc[stkSize] = -(current + 1);
                                    ++stkSize;
                                    c2 = hs;
                                    count2 = h;
                                }
                                stkVar[stkSize] = (short)((state((byte)'A') << 12) | (c2 << 8) | ((d - 1) << 3) | (count2 << 1));
                                stkSrc[stkSize] = -(current + 1);
                                ++stkSize;
                            }
                            else
                                stkR[current] = Long.MAX_VALUE;
                            continue;
                        }
                        else
                            m = min(stkL[stkSize],stkR[stkSize]);
                    }
                    memo[d - 1][p][c][count] = m;
                }
                if(stkSize == 0)
                {
                    complexity += m;
                    break;
                }
                // Add the result to the appropriate accumulator.
                final long[] src;
                if(stkSrc[stkSize] < 0)
                {
                    src = stkR;
                    stkSrc[stkSize] = -(stkSrc[stkSize] + 1);
                }
                else
                    src = stkL;
                src[stkSrc[stkSize]] += m;
            }
        }
        
        out += coefficient * complexity;
        do ++i; while(i < input.length && Character.isWhitespace(input[i]));
    }
    while(i < input.length);
    return out;
}

static void main()
{
    final byte[] input = httpGet("https://adventofcode.com/2024/day/21/input").getBytes(UTF_8);
    System.out.printf("Part 1: %d\n",solution(input,(byte)4));
    System.out.printf("Part 2: %d\n",solution(input,(byte)27));
}