import utils.BitSet;
import utils.map.ShortMap;

import static java.nio.charset.StandardCharsets.UTF_8;
import static utils.HttpUtils.httpGet;

static short origin(final int[][] merge,short pos)
{
    short first = (short)BitSet.firstSetBit(merge[pos]);
    // update the merge bitset until the origin is found
    while(first != pos) //TODO check for -1?
    {
        for(byte i = 0;i < merge[first].length;++i)
            if(merge[first][i] != 0)
            {
                merge[pos][i] |= merge[first][i];
                break;
            }
        first = (short)BitSet.firstSetBit(merge[pos = first]);
    }
    // return the origin
    return pos;
}
static long part1(final byte[] map,final byte width)
{
    final byte height = (byte)(map.length / width);
    final ShortMap<Integer> regions = new ShortMap<>(1024);
    regions.grow = true;
    final short[] regionMap = new short[height * width];
    final int[][] merge = new int[height * width][BitSet.arraySize(height * width)];
    short p = 0;
    for(byte r = 0;r < height;++r)
        for(byte c = 0;c < width;++c)
        {
            final byte type = map[p];
            
            byte fence = 4;
            if(c == 0 || map[p - 1] == type)
                --fence;
            if(c + 1 == width || map[p + 1] == type)
                --fence;
            if(r == 0 || map[p - width] == type)
                --fence;
            if(r + 1 == height || map[p + width] == type)
                --fence;
            
            byte neighbors = (byte)((c != 0 && map[p - 1] == type? 2 : 0) | (r != 0 && map[p - width] == type? 1 : 0));
            switch(neighbors)
            {
                case 0 -> regions.put(regionMap[p] = p,0);
                case 1 -> regionMap[p] = origin(merge,regionMap[p - width]);
                case 2 -> regionMap[p] = origin(merge,regionMap[p - 1]);
                default ->
                {
                    short north = origin(merge,regionMap[p - width]),
                           west = origin(merge,regionMap[p - 1]),
                            min = (short)Math.min(north,west),
                           swap = (short)(north ^ west);
                    regionMap[p] = min;
                    if(swap != 0)
                    {
                        final short max = (short)(swap ^ min);
                        BitSet.set(merge[max],min);
                    }
                }
            }
            
            ++p;
        }
    return -1;
}

static void main()
{
    byte[] input = httpGet("https://adventofcode.com/2024/day/12/input").getBytes(UTF_8);
    byte width;
    for(width = 0;width < input.length;++width)
        if(Character.isWhitespace(input[width]))
            break;
    byte lineEnd;
    for(lineEnd = width;lineEnd < input.length;++lineEnd)
        if(!Character.isWhitespace(input[lineEnd]))
            break;
    for(byte r = 0;r < (input.length - 1 + lineEnd) / lineEnd;++r)
        System.arraycopy(input,r * lineEnd,input,r * width,width);
    System.arraycopy(input,0,input = new byte[((input.length - 1 + lineEnd) / lineEnd) * width],0,input.length);
}