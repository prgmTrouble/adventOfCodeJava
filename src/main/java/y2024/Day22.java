import utils.BitSet;

import static java.nio.charset.StandardCharsets.UTF_8;
import static utils.HttpUtils.httpGet;

static int prng(int seed)
{
    seed = (seed ^ (seed << 6)) & 0x00FFFFFF;
    seed = seed ^ (seed >>> 5);
    seed = (seed ^ (seed << 11)) & 0x00FFFFFF;
    return seed;
}

static void solution(final byte[] input)
{
    // ((19<<15)|(9<<10)|(9<<5)|9) + 1 = 632106
    final int[] sequences = new int[632106],
                visited = BitSet.create(632106);
    long part1 = 0;
    int i = 0;
    do
    {
        int seed = 0;
        do seed = 10 * seed - '0' + input[i]; while(++i < input.length && !Character.isWhitespace(input[i]));
        
        Arrays.fill(visited,0);
        int key = 0;
        for(byte j = 0;j < 3;++j)
            key = (key << 5) | ((seed % 10) - ((seed = prng(seed)) % 10) + 9);
        for(short j = 3;j < 2000;++j)
        {
            key = ((key << 5) | ((seed % 10) - ((seed = prng(seed)) % 10) + 9)) & 0xFFFFF;
            if(!BitSet.test(visited,key))
            {
                BitSet.set(visited,key);
                sequences[key] += seed % 10;
            }
        }
        
        part1 += seed;
        
        do ++i; while(i < input.length && Character.isWhitespace(input[i]));
    }
    while(i < input.length);
    
    System.out.printf("Part 1: %d\n",part1);
    int max = 0;
    for(i = 1;i < sequences.length;++i)
        if(sequences[max] < sequences[i])
            max = i;
    System.out.printf
    (
        "Part 2: %d [%d,%d,%d,%d]\n",
        sequences[max],
        ((max >>> 15) & 0x1F) - 9,
        ((max >>> 10) & 0x1F) - 9,
        ((max >>> 5) & 0x1F) - 9,
        (max & 0x1F) - 9
    );
}

static void main()
{
    solution(httpGet("https://adventofcode.com/2024/day/22/input").getBytes(UTF_8));
}