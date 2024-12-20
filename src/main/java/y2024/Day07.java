import java.util.Arrays;

import static java.lang.Math.max;
import static java.nio.charset.StandardCharsets.UTF_8;
import static utils.HttpUtils.httpGet;
import static utils.IntegerUtils.*;

static long part1(final long[] values,final short[][] operands)
{
    long result = 0;
    for(short l = 0;l < values.length;++l)
        for(short permutation = 0;(permutation & (1 << (operands[l].length - 1))) == 0;++permutation)
        {
            long value = operands[l][0];
            for(byte op = 0;op < operands[l].length - 1;++op)
                if((permutation & (1 << op)) == 0)
                    value += operands[l][op + 1];
                else
                    value *= operands[l][op + 1];
            if(value == values[l])
            {
                result += values[l];
                break;
            }
        }
    return result;
}

static long part2(final long[] values,final short[][] operands)
{
    final long[] powCache = new long[16];
    for(byte i = 0;i < powCache.length;++i)
        powCache[i] = pow(3,i);
    long result = 0;
    for(short l = 0;l < values.length;++l)
        // It might be more efficient to use a stack instead of re-computing the intermediate values
        // every time, but I'm not going to bother timing it.
        for(int permutation = 0;permutation < powCache[operands[l].length - 1];++permutation)
        {
            long value = operands[l][0];
            for(byte op = 0;op < operands[l].length - 1;++op)
                switch((byte)((permutation / powCache[op]) % 3))
                {
                    case 0 -> value += operands[l][op + 1];
                    case 1 -> value *= operands[l][op + 1];
                    default -> value = value * i32pow10((byte)max(log10(operands[l][op + 1]),1)) + operands[l][op + 1];
                }
            if(value == values[l])
            {
                result += values[l];
                break;
            }
        }
    return result;
}

static void main()
{
    final byte[] input = httpGet("https://adventofcode.com/2024/day/7/input").getBytes(UTF_8);
    long[] values = new long[256];
    short[][] operands = new short[values.length][];
    short line = 0,operand = -1;
    short[] buf = new short[16];
    for(final byte c : input)
        switch(c)
        {
            case '0','1','2','3','4',
                 '5','6','7','8','9' ->
            {
                if(operand == buf.length)
                    System.arraycopy(buf,0,buf = new short[operand * 2],0,operand);
                if(line == values.length)
                {
                    System.arraycopy(values,0,values = new long[line * 2],0,line);
                    System.arraycopy(operands,0,operands = new short[line * 2][],0,line);
                }
                if(operand == -1)
                    values[line] = values[line] * 10 - '0' + c;
                else
                    buf[operand] = (short)(buf[operand] * 10 - '0' + c);
            }
            case ' ' -> ++operand;
            case '\n' ->
            {
                System.arraycopy(buf,0,operands[line++] = new short[++operand],0,operand);
                operand = -1;
                Arrays.fill(buf,(short)0);
            }
            default -> {/* do nothing */}
        }
    if(operand != -1)
        // No '\n' at end of file
        ++line;
    System.arraycopy(values,0,values = new long[line],0,line);
    System.arraycopy(operands,0,operands = new short[line][],0,line);
    
    System.out.printf("Part 1: %d\n",part1(values,operands));
    System.out.printf("Part 2: %d\n",part2(values,operands));
}