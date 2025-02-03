import utils.BitSet;

import static java.lang.Math.abs;
import static java.lang.Math.min;
import static java.nio.charset.StandardCharsets.UTF_8;
import static utils.HttpUtils.httpGet;

static final byte THRESHOLD = 100,CHEAT = 20;
static void solution(final int[] map,final byte width,final byte height,
                     short start,final short end)
{
    final short[] dp = new short[(width & 0xFF) * (height & 0xFF)];
    Arrays.fill(dp,(short)-1);
    
    // Quickly run through the portions of the path for which a shortcut above the threshold is not possible.
    short path;
    for(path = 0;path < THRESHOLD + 2 & start != end;++path)
    {
        dp[start] = path;
        if(start % (width & 0xFF) > 0 && dp[start - 1] == -1 && !BitSet.test(map,start - 1))
            start = (short)(start - 1);
        else if(start % (width & 0xFF) + 1 < (width & 0xFF) && dp[start + 1] == -1 && !BitSet.test(map,start + 1))
            start = (short)(start + 1);
        else if(start / (width & 0xFF) > 0 && dp[start - (width & 0xFF)] == -1 && !BitSet.test(map,start - (width & 0xFF)))
            start = (short)(start - (width & 0xFF));
        else
            start = (short)(start + (width & 0xFF));
    }
    short part1 = 0;
    int part2 = 0;
    while(true)
    {
        // Iterate through all points 'p' such that 'p' is in bounds and the rectilinear distance between 'p' and the
        // current position is at most the maximum shortcut distance.
        for
        (
            byte dr = (byte)-min(start / (width & 0xFF),CHEAT);
            dr < min((height & 0xFF) - start / (width & 0xFF),CHEAT + 1);
            ++dr
        )
            for
            (
                byte dc = (byte)-min(start % (width & 0xFF),CHEAT - abs(dr));
                dc < min((width & 0xFF) - start % (width & 0xFF),CHEAT - abs(dr) + 1);
                ++dc
            )
            {
                // Determine if the shortcut lands on a visited point and if the distance saved meets or exceeds the
                // minimum threshold.
                final short score = dp[(start / (width & 0xFF) + dr) * (width & 0xFF) + start % (width & 0xFF) + dc];
                if(score != -1 && path - score >= THRESHOLD + abs(dr) + abs(dc))
                {
                    ++part2;
                    if(abs(dr) + abs(dc) <= 2)
                        ++part1;
                }
            }
        
        if(start == end)
            break;
        // Advance to the next point in the route.
        dp[start] = path;
        if(start % (width & 0xFF) > 0 && dp[start - 1] == -1 && !BitSet.test(map,start - 1))
            start = (short)(start - 1);
        else if(start % (width & 0xFF) + 1 < (width & 0xFF) && dp[start + 1] == -1 && !BitSet.test(map,start + 1))
            start = (short)(start + 1);
        else if(start / (width & 0xFF) > 0 && dp[start - (width & 0xFF)] == -1 && !BitSet.test(map,start - (width & 0xFF)))
            start = (short)(start - (width & 0xFF));
        else
            start = (short)(start + (width & 0xFF));
        ++path;
    }
    System.out.printf("Part 1: %d\nPart 2: %d\n",part1,part2);
}

// An alternative solution which is easier to read. This algorithm is slower than the first because it runs N^2 comparisons
// instead of being able to limit the search space to a (CHEAT * CHEAT * 4) area for each point.
static void alternative(final int[] map,final byte width,final byte height,
                        short start,final short end)
{
    final byte[] path = new byte[((width & 0xFF) * (height & 0xFF)) << 1];
    short index;
    for(index = 0;start != end;index += 2)
    {
        BitSet.set(map,start);
        path[index] = (byte)(start / (width & 0xFF));
        path[index | 1] = (byte)(start % (width & 0xFF));
        if(start % (width & 0xFF) > 0 && !BitSet.test(map,start - 1))
            start = (short)(start - 1);
        else if(start % (width & 0xFF) + 1 < (width & 0xFF) && !BitSet.test(map,start + 1))
            start = (short)(start + 1);
        else if(start / (width & 0xFF) > 0 && !BitSet.test(map,start - (width & 0xFF)))
            start = (short)(start - (width & 0xFF));
        else
            start = (short)(start + (width & 0xFF));
    }
    path[index] = (byte)(end / (width & 0xFF));
    path[index | 1] = (byte)(end % (width & 0xFF));
    short part1 = 0;
    int part2 = 0;
    for(;index >= THRESHOLD << 1;index -= 2)
        for(short i = (short)(index - (THRESHOLD << 1));i >= 0;i -= 2)
        {
            // Path distance
            final short d0 = (short)((index >> 1) - (i >> 1));
            assert d0 >= THRESHOLD;
            // Rectilinear distance
            final short d1 = (short)
            (
                abs((path[index] & 0xFF) - (path[i] & 0xFF)) +
                abs((path[index | 1] & 0xFF) - (path[i | 1] & 0xFF))
            );
            
            if(d0 >= THRESHOLD + d1 & d1 <= CHEAT)
            {
                ++part2;
                if(d1 <= 2)
                    ++part1;
            }
        }
    System.out.printf("Part 1: %d\nPart 2: %d\n",part1,part2);
}

static void main()
{
    final byte[] input = httpGet("https://adventofcode.com/2024/day/20/input").getBytes(UTF_8);
    byte width;
    for(width = 0;(width & 0xFF) < input.length;++width)
        if(Character.isWhitespace(input[width & 0xFF]))
            break;
    byte lineEnd;
    for(lineEnd = width;(++lineEnd & 0xFF) < input.length;)
        if(!Character.isWhitespace(input[(lineEnd & 0xFF)]))
            break;
    final byte height = (byte)((input.length - 1 + (lineEnd & 0xFF)) / (lineEnd & 0xFF) - 2);
    width -= 2;
    
    final int[] map = BitSet.create((width & 0xFF) * (height & 0xFF));
    short start = -1,end = -1;
    for(byte r = 0;(r & 0xFF) < (height & 0xFF);++r)
        for(byte c = 0;(c & 0xFF) < (width & 0xFF);++c)
        {
            final short p = (short)((r & 0xFF) * (width & 0xFF) + (c & 0xFF));
            switch(input[((r & 0xFF) + 1) * (lineEnd & 0xFF) + (c & 0xFF) + 1])
            {
                case '#' -> BitSet.set(map,p);
                case 'S' -> start = p;
                case 'E' -> end = p;
                default -> {/* do nothing */}
            }
        }
    assert start != -1 & end != -1;
    
    solution(map,width,height,start,end);
    alternative(map,width,height,start,end);
}