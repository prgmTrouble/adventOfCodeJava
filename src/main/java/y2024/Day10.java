import utils.BitSet;

import java.util.Arrays;

import static java.nio.charset.StandardCharsets.UTF_8;
import static utils.HttpUtils.httpGet;

static short part1(final byte[][] map)
{
    final int[] visited = BitSet.create((long)map.length * map[0].length);
    final byte[] dfs = new byte[40];
    byte size;
    short result = 0;
    for(byte r = 0;r < map.length;++r)
        for(byte c = 0;c < map[0].length;++c)
            if(map[r][c] == 0)
            {
                Arrays.fill(visited,0);
                size = 1;
                dfs[0] = r;
                dfs[1] = c;
                do
                {
                    final byte r2 = dfs[--size << 1],c2 = dfs[(size << 1) + 1];
                    final short idx = (short)(r2 * map[0].length + c2);
                    if(BitSet.test(visited,idx)) continue;
                    BitSet.set(visited,idx);
                    if(map[r2][c2] == 9) ++result;
                    else
                    {
                        if(r2 + 1 < map.length && map[r2 + 1][c2] == map[r2][c2] + 1)
                        {
                            dfs[size << 1] = (byte)(r2 + 1);
                            dfs[(size++ << 1) + 1] = c2;
                        }
                        if(r2 > 0 && map[r2 - 1][c2] == map[r2][c2] + 1)
                        {
                            dfs[size << 1] = (byte)(r2 - 1);
                            dfs[(size++ << 1) + 1] = c2;
                        }
                        if(c2 + 1 < map[0].length && map[r2][c2 + 1] == map[r2][c2] + 1)
                        {
                            dfs[size << 1] = r2;
                            dfs[(size++ << 1) + 1] = (byte)(c2 + 1);
                        }
                        if(c2 > 0 && map[r2][c2 - 1] == map[r2][c2] + 1)
                        {
                            dfs[size << 1] = r2;
                            dfs[(size++ << 1) + 1] = (byte)(c2 - 1);
                        }
                    }
                }
                while(size != 0);
            }
    return result;
}

static short part2(final byte[][] map)
{
    // This does the same thing as part 1 except check the 'visited' set
    final short[][] dp = new short[map.length][map[0].length];
    for(byte r = 0;r < map.length;++r)
        for(byte c = 0;c < map[0].length;++c)
            if(map[r][c] == 0)
                dp[r][c] = 1;
    for(byte step = 0;step < 9;++step)
        for(byte r = 0;r < map.length;++r)
            for(byte c = 0;c < map[0].length;++c)
                if(map[r][c] == step + 1)
                    dp[r][c] = (short)
                    (
                        (c > 0 && map[r][c - 1] == step ? dp[r][c - 1] : 0) +
                        (c + 1 < map[0].length && map[r][c + 1] == step ? dp[r][c + 1] : 0) +
                        (r > 0 && map[r - 1][c] == step ? dp[r - 1][c] : 0) +
                        (r + 1 < map.length && map[r + 1][c] == step ? dp[r + 1][c] : 0)
                    );
    short result = 0;
    for(byte r = 0;r < map.length;++r)
        for(byte c = 0;c < map[0].length;++c)
            if(map[r][c] == 9)
                result += (short)
                (
                    (c > 0 && map[r][c - 1] == 8 ? dp[r][c - 1] : 0) +
                    (c + 1 < map[0].length && map[r][c + 1] == 8 ? dp[r][c + 1] : 0) +
                    (r > 0 && map[r - 1][c] == 8 ? dp[r - 1][c] : 0) +
                    (r + 1 < map.length && map[r + 1][c] == 8 ? dp[r + 1][c] : 0)
                );
    return result;
}

static void main()
{
    byte[] input = httpGet("https://adventofcode.com/2024/day/10/input").getBytes(UTF_8);
    byte width;
    for(width = 0;width < input.length;++width)
        if(Character.isWhitespace(input[width]))
            break;
    byte lineEnd;
    for(lineEnd = width;lineEnd < input.length;++lineEnd)
        if(!Character.isWhitespace(input[lineEnd]))
            break;
    final byte[][] map = new byte[input.length/lineEnd][width];
    for(byte r = 0;r < map.length;++r)
        for(byte c = 0;c < map[0].length;++c)
            map[r][c] = (byte)(input[r * lineEnd + c] - '0');
    
    System.out.printf("Part 1: %d\n",part1(map));
    System.out.printf("Part 2: %d\n",part2(map));
}