import utils.ArrayUtils;

import static java.nio.charset.StandardCharsets.UTF_8;
import static utils.HttpUtils.httpGet;

static void main()
{
    final byte[] input = httpGet("https://adventofcode.com/2024/day/25/input").getBytes(UTF_8);
    int i;
    byte pins = 0,width,depth = -1;
    for(i = 0;i < input.length;++i,++pins)
        if(Character.isWhitespace(input[i]))
            break;
    while(i < input.length && Character.isWhitespace(input[i]))
        ++i;
    width = (byte)i;
    do ++depth;
    while((i += width) < input.length && !Character.isWhitespace(input[i]));
    while(i < input.length && Character.isWhitespace(input[i]))
        ++i;
    
    final byte bits = (byte)(32 - Integer.numberOfLeadingZeros(depth)),
               block = (byte)i;
    final short keyFlag = (short)(1 << (bits * pins));
    final short[] list = new short[(input.length + block - 1) / block];
    short size = 0;
    i = 0;
    do
    {
        short tmp = 0;
        for(byte d = 0;d < depth;++d)
            for(byte p = 0;p < pins;++p)
                if(input[i + width * (d + 1) + p] == input[i])
                    tmp += (short)(1 << (p * bits));
        if(input[i] == '.')
            tmp |= keyFlag;
        ArrayUtils.insertSorted(list,tmp,size);
        i += block;
    }
    while(++size < list.length);
    
    short keyBegin = (short)ArrayUtils.find(list,keyFlag,list.length);
    if(keyBegin < 0)
        keyBegin = (short)(-keyBegin - 1);
    
    // For each lock, count the number of keys where each void in the key is larger than the corresponding pin in the lock.
    final short[] mask = new short[pins - 1];
    for(byte p = 0;p < mask.length;++p)
        mask[p] = (short)((1 << ((p + 1) * bits)) - 1);
    short out = 0;
    for(short l = 0;l < keyBegin;++l)
    {
        final short lock = list[l];
        // Find the first key where the first pin fits the corresponding lock pin.
        short k = (short)ArrayUtils.find(list,(short)(lock | keyFlag),keyBegin,list.length);
        if(k < 0)
            k = (short)(-k - 1);
        // Check all keys in the range for pins that do not fit in the lock.
        outer:
        for(;k < list.length;++k)
        {
            final short key = list[k];
            for(byte p = (byte)(pins - 1);--p >= 0;)
            {
                final short diff = (short)((key & mask[p]) - (lock & mask[p]));
                if(diff == 0)
                    // Remaining pins are equal: exit early.
                    break;
                if(diff < 0)
                    continue outer;
            }
            ++out;
        }
    }
    
    System.out.printf("Solution: %d\n",out);
}