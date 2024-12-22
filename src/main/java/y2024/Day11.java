import utils.map.LongMap;

import static java.nio.charset.StandardCharsets.UTF_8;
import static utils.HttpUtils.httpGet;
import static utils.IntegerUtils.i64pow10;
import static utils.IntegerUtils.log10;

static long helper(final long stone,byte blink,final byte maxBlink,final LongMap<long[]> memoize)
{
    final long[] memo = memoize.putIfAbsent(stone,() -> new long[maxBlink]);
    if(blink-- == 0)
        return 1;
    if(memo[blink] != 0)
        return memo[blink];
    if(stone == 0)
        return memo[blink] = helper(1,blink,maxBlink,memoize);
    final byte log10 = log10(stone);
    if((log10 & 1) == 0)
    {
        final long pow10 = i64pow10((byte)(log10 >>> 1));
        return memo[blink] = helper(stone / pow10,blink,maxBlink,memoize)
                           + helper(stone % pow10,blink,maxBlink,memoize);
    }
    return memo[blink] = helper(stone * 2024,blink,maxBlink,memoize);
}
static long solution(final long[] stones,final byte blink)
{
    final LongMap<long[]> memoize = new LongMap<>(1 << 16);
    memoize.grow = true;
    long count = 0;
    for(final long stone : stones)
        count += helper(stone,blink,blink,memoize);
    return count;
}

static void main()
{
    final byte[] input = httpGet("https://adventofcode.com/2024/day/11/input").getBytes(UTF_8);
    long[] data = new long[8];
    int size = 0;
    boolean flag = false;
    for(final byte b : input)
        //noinspection AssignmentUsedAsCondition
        if(flag = Character.isWhitespace(b))
        {
            if(size == data.length)
                System.arraycopy(data,0,data = new long[size << 1],0,size);
            ++size;
        }
        else
            data[size] = data[size] * 10 - '0' + b;
    if(!flag) // no trailing whitespace
        ++size;
    if(size != data.length)
        System.arraycopy(data,0,data = new long[size],0,size);
    
    System.out.printf("Part 1: %d\n",solution(data,(byte)25));
    System.out.printf("Part 2: %d\n",solution(data,(byte)75));
}