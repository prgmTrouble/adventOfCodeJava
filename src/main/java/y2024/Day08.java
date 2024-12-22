import utils.BitSet;
import utils.map.ByteMap;

import static java.nio.charset.StandardCharsets.UTF_8;
import static utils.HttpUtils.httpGet;

static short part1(final byte[][] map,final ByteMap<short[]> frequencies)
{
    // Place the antinodes for each pair of antennae with matching frequencies
    final int[] antinodes = BitSet.create((long)map.length * map[0].length);
    for(byte f = 0;f < frequencies.size;++f)
    {
        final short[] coordinates = (short[])frequencies.values[f];
        for(byte p0 = 0;++p0 < coordinates[0];)
        {
            final byte r0 = (byte)(coordinates[p0] >>> 8),
                       c0 = (byte)(coordinates[p0] & 0xFF);
            for(byte p1 = p0;p1++ < coordinates[0];)
            {
                final byte r1 = (byte)(coordinates[p1] >>> 8),
                           c1 = (byte)(coordinates[p1] & 0xFF),
                           dr = (byte)(r0 - r1),
                           dc = (byte)(c0 - c1),
                          ar0 = (byte)(r0 + dr),
                          ac0 = (byte)(c0 + dc),
                          ar1 = (byte)(r1 - dr),
                          ac1 = (byte)(c1 - dc);
                if(0 <= ar0 && ar0 < map.length && 0 <= ac0 && ac0 < map[0].length)
                    BitSet.set(antinodes,(long)ar0 * map[0].length + ac0);
                if(0 <= ar1 && ar1 < map.length && 0 <= ac1 && ac1 < map[0].length)
                    BitSet.set(antinodes,(long)ar1 * map[0].length + ac1);
            }
        }
    }
    
    short result = 0;
    for(final int i : antinodes)
        result += (short)Integer.bitCount(i);
    return result;
}

static short part2(final byte[][] map,final ByteMap<short[]> frequencies)
{
    // Place the antinodes for each pair of antennae with matching frequencies
    final int[] antinodes = BitSet.create((long)map.length * map[0].length);
    for(byte f = 0;f < frequencies.size;++f)
    {
        final short[] coordinates = (short[])frequencies.values[f];
        for(byte p0 = 0;++p0 < coordinates[0];)
        {
            final byte r0 = (byte)(coordinates[p0] >>> 8),
                       c0 = (byte)(coordinates[p0] & 0xFF);
            for(byte p1 = p0;p1++ < coordinates[0];)
            {
                final byte r1 = (byte)(coordinates[p1] >>> 8),
                           c1 = (byte)(coordinates[p1] & 0xFF),
                           dr = (byte)(r0 - r1),
                           dc = (byte)(c0 - c1);
                
                // The loop conditions could be optimized based on the parities of
                // 'dr' and 'dc', but I'm too lazy to write out all the branches.
                byte r2 = r0,
                     c2 = c0;
                do
                {
                    BitSet.set(antinodes,(long)r2 * map[0].length + c2);
                    r2 += dr;
                    c2 += dc;
                }
                while(0 <= r2 && r2 < map.length && 0 <= c2 && c2 < map[0].length);
                
                r2 = r1;
                c2 = c1;
                do
                {
                    BitSet.set(antinodes,(long)r2 * map[0].length + c2);
                    r2 -= dr;
                    c2 -= dc;
                }
                while(0 <= r2 && r2 < map.length && 0 <= c2 && c2 < map[0].length);
            }
        }
    }
    
    short result = 0;
    for(final int i : antinodes)
        result += (short)Integer.bitCount(i);
    return result;
}

static void main()
{
    final byte[] input = httpGet("https://adventofcode.com/2024/day/8/input").getBytes(UTF_8);
    byte width;
    for(width = 0;width < input.length;++width)
        if(Character.isWhitespace(input[width]))
            break;
    byte lineEnd;
    for(lineEnd = width;lineEnd < input.length;++lineEnd)
        if(!Character.isWhitespace(input[lineEnd]))
            break;
    final byte[][] map = new byte[input.length / lineEnd][width];
    byte r = 0,c = 0;
    for(final byte i : input)
        if(i == '\n')
        {
            ++r;
            c = 0;
        }
        else if(!Character.isWhitespace(i))
        {
            if(('0' <= i && i <= '9') ||
               ('A' <= i && i <= 'Z') ||
               ('a' <= i && i <= 'z'))
                map[r][c] = i;
            ++c;
        }
    
    // Build a data structure to quickly find antennae of the same frequency
    final ByteMap<short[]> frequencies = new ByteMap<>(64);
    for(r = 0;r < map.length;++r)
        for(c = 0;c < map[0].length;++c)
            if(map[r][c] != 0)
            {
                final short[] coordinates = frequencies.putIfAbsent(map[r][c],() -> new short[5]);
                coordinates[++coordinates[0]] = (short)((r << 8) | c);
            }
    
    System.out.printf("Part 1: %d\n",part1(map,frequencies));
    System.out.printf("Part 2: %d\n",part2(map,frequencies));
}