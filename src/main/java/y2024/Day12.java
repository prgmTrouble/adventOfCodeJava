import utils.ArrayUtils;
import utils.BitSet;
import utils.map.ShortMap;

import static java.nio.charset.StandardCharsets.UTF_8;
import static utils.HttpUtils.httpGet;

static short origin(final int[][] merge,short pos)
{
    short first = (short)BitSet.firstSetBit(merge[pos]);
    // Update the merge bitset until the origin is found.
    while(first != -1 & first != pos)
    {
        for(short i = 0;i < merge[first].length;++i)
            if(merge[first][i] != 0)
            {
                merge[pos][i] |= merge[first][i];
                break;
            }
        first = (short)BitSet.firstSetBit(merge[pos = first]);
    }
    return pos;
}

static long part1(final byte[] map,final short width,final short height)
{
    final ShortMap<Integer> regions = new ShortMap<>(1024);
    regions.grow = true;
    final short[] regionMap = new short[height * width];
    final int[][] origin = new int[height * width][BitSet.arraySize(height * width)];
    short p = 0;
    for(short r = 0;r < height;++r)
        for(short c = 0;c < width;++c)
        {
            final byte neighbors = (byte)((c != 0 && map[p - 1] == map[p]? 2 : 0) |
                                          (r != 0 && map[p - width] == map[p]? 1 : 0));
            switch(neighbors)
            {
                // No matching neighbors north or west: create a new region
                case 0 -> regions.put(regionMap[p] = p,0);
                
                // One matching neighbor: record the region appropriately
                case 1 -> regionMap[p] = origin(origin,regionMap[p - width]);
                case 2 -> regionMap[p] = origin(origin,regionMap[p - 1]);
                
                // Two matching neighbors: find the north-west origin and merge if necessary
                default ->
                {
                    final short north = origin(origin,regionMap[p - width]),
                                 west = origin(origin,regionMap[p - 1]),
                                  min = (short)Math.min(north,west),
                                 swap = (short)(north ^ west);
                    regionMap[p] = min;
                    if(swap != 0)
                    {
                        // The two neighbors disagree about the origin, so we need to
                        // merge the two areas.
                        final short max = (short)(swap ^ min);
                        BitSet.set(origin[max],min);
                        
                        Integer maxRegion = regions.remove(regionMap[max]);
                        assert maxRegion != null;
                        
                        final int find = ArrayUtils.find(regions.keys,min,regions.size);
                        assert find >= 0;
                        Integer minRegion = (Integer)regions.values[find];
                        assert minRegion != null;
                        
                        regions.values[find] = minRegion + maxRegion;
                    }
                }
            }
            BitSet.set(origin[p],regionMap[p]);
            
            // Update the region with the contributed area and perimeter.
            final int find = ArrayUtils.find(regions.keys,regionMap[p],regions.size);
            assert find >= 0;
            final Integer region = (Integer)regions.values[find];
            assert region != null;
            
            final byte fence = (byte)
            (
                (c == 0 || map[p - 1] != map[p] ? 1 : 0) +
                (c + 1 == width || map[p + 1] != map[p] ? 1 : 0) +
                (r == 0 || map[p - width] != map[p] ? 1 : 0) +
                (r + 1 == height || map[p + width] != map[p] ? 1 : 0)
            );
            regions.values[find] = region + ((1 << 16) | fence);
            
            ++p;
        }
    long result = 0;
    for(int region = 0;region < regions.size;++region)
        result += (long)((Integer)regions.values[region] >>> 16) * ((Integer)regions.values[region] & 0xFFFF);
    return result;
}

static long part2(final byte[] map,final short width,final short height)
{
    final ShortMap<Integer> regions = new ShortMap<>(1024);
    regions.grow = true;
    final short[] regionMap = new short[height * width];
    final int[][] origin = new int[height * width][BitSet.arraySize(height * width)];
    short p = 0;
    for(short r = 0;r < height;++r)
        for(short c = 0;c < width;++c)
        {
            final byte bound = (byte)((r != 0? 1 : 0) | (c != 0? 2 : 0) | (r + 1 != height? 4 : 0) | (c + 1 != width? 8 : 0));
            final byte neighbors = (byte)(((bound & 0b0001) != 0      && map[p - width    ] == map[p] ? 128 : 0) | // N
                                          ((bound & 0b0010) != 0      && map[p         - 1] == map[p] ? 64 : 0) |  // W
                                          ((bound & 0b0100) != 0      && map[p + width    ] == map[p] ? 32 : 0) |  // S
                                          ((bound & 0b1000) != 0      && map[p         + 1] == map[p] ? 16 : 0) |  // E
                                          ((bound & 0b0011) == 0b0011 && map[p - width - 1] == map[p] ? 8 : 0) |   // NW
                                          ((bound & 0b0110) == 0b0110 && map[p + width - 1] == map[p] ? 4 : 0) |   // SW
                                          ((bound & 0b1100) == 0b1100 && map[p + width + 1] == map[p] ? 2 : 0) |   // SE
                                          ((bound & 0b1001) == 0b1001 && map[p - width + 1] == map[p] ? 1 : 0));   // NE
            
            switch((neighbors >>> 6) & 0b11)
            {
                // No matching neighbors north or west: create a new region
                case 0 -> regions.put(regionMap[p] = p,0);
                
                // One matching neighbor: record the region appropriately
                case 1 -> regionMap[p] = origin(origin,regionMap[p - 1]);
                case 2 -> regionMap[p] = origin(origin,regionMap[p - width]);
                
                // Two matching neighbors: find the north-west origin and merge if necessary
                default ->
                {
                    final short north = origin(origin,regionMap[p - width]),
                                 west = origin(origin,regionMap[p - 1]),
                                  min = (short)Math.min(north,west),
                                 swap = (short)(north ^ west);
                    regionMap[p] = min;
                    if(swap != 0)
                    {
                        // The two neighbors disagree about the origin, so we need to
                        // merge the two areas.
                        final short max = (short)(swap ^ min);
                        BitSet.set(origin[max],min);
                        
                        Integer maxRegion = regions.remove(regionMap[max]);
                        assert maxRegion != null;
                        
                        final int find = ArrayUtils.find(regions.keys,min,regions.size);
                        assert find >= 0;
                        Integer minRegion = (Integer)regions.values[find];
                        assert minRegion != null;
                        
                        regions.values[find] = minRegion + maxRegion;
                    }
                }
            };
            BitSet.set(origin[p],regionMap[p]);
            
            // Update the region with the contributed area and perimeter.
            final int find = ArrayUtils.find(regions.keys,regionMap[p],regions.size);
            assert find >= 0;
            final Integer region = (Integer)regions.values[find];
            assert region != null;
            
            // The number of 'sides' is the same as the number of corners.
            final byte sides = (byte)Integer.bitCount
            (
                (neighbors ^ ~(((neighbors & 0b01110000) << 1) | ((neighbors >>> 3) & 0b00010000)))
                & ~(neighbors & (neighbors << 4))
                & 0xF0
            );
            regions.values[find] = region + (1 << 16) + sides;
            
            ++p;
        }
    long result = 0;
    for(int region = 0;region < regions.size;++region)
        result += (long)((Integer)regions.values[region] >>> 16) * ((Integer)regions.values[region] & 0xFFFF);
    return result;
}

static void main()
{
    byte[] input = httpGet("https://adventofcode.com/2024/day/12/input").getBytes(UTF_8);
    short width;
    for(width = 0;width < input.length;++width)
        if(Character.isWhitespace(input[width]))
            break;
    short lineEnd;
    for(lineEnd = width;lineEnd < input.length;++lineEnd)
        if(!Character.isWhitespace(input[lineEnd]))
            break;
    final short height = (short)((input.length - 1 + lineEnd) / lineEnd);
    for(short r = 0;r < height;++r)
        System.arraycopy(input,r * lineEnd,input,r * width,width);
    System.arraycopy(input,0,input = new byte[((input.length - 1 + lineEnd) / lineEnd) * width],0,input.length);
    
    System.out.printf("Part 1: %d\n",part1(input,width,height));
    System.out.printf("Part 2: %d\n",part2(input,width,height));
}