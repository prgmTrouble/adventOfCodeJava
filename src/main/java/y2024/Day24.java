import utils.ArrayUtils;
import utils.BitSet;

import static java.nio.charset.StandardCharsets.UTF_8;
import static utils.HttpUtils.httpGet;

static short id(final int idx,final byte[] input)
{
    return (short)
    (
        ((input[idx] - 'a') << 10) |
        ((input[idx + 1] - ('a' <= input[idx + 1] & input[idx + 1] <= 'z' ? 'a' : '0')) << 5) |
        (input[idx + 2] - ('a' <= input[idx + 2] & input[idx + 2] <= 'z' ? 'a' : '0'))
    );
}

static String name(final short id)
{
    char a = (char)(((id >>> 10) & 0x1F) + 'a');
    final char offset = a < 'x' ? 'a' : '0';
    char b = (char)(((id >>> 5) & 0x1F) + offset),
         c = (char)((id & 0x1F) + offset);
    return String.format("%c%c%c",a,b,c);
}

static final byte ARG0 = 20,ARG1 = 11,OUTPUT = 2,
                  AND = 0,OR = 1,XOR = 2;

static void main()
{
    final byte[] input = httpGet("https://adventofcode.com/2024/day/24/input").getBytes(UTF_8);
    long x = 0,y = 0;
    final byte word;
    int idx;
    {
        if(input[5] == '1') x = 1;
        for(idx = 6;idx < input.length;++idx)
            if(!Character.isWhitespace(input[idx]))
                break;
        final int width = idx;
        byte bit;
        for(bit = 1;input[idx] == 'x';++bit)
        {
            if(input[idx + 5] == '1')
                x |= 1L << bit;
            idx += width;
        }
        word = bit;
        for(bit = 0;bit < word;++bit)
            if(input[idx + (bit * width) + 5] == '1')
                y |= 1L << bit;
        idx = (width * word) << 1;
        // assert Character.isWhitespace(input[idx]);
    }
    // Total number of instructions in an N-bit full-adder is (5N + 3).
    final int[] instructions = new int[5 * word - 3];
    // Total number of named bits is the number of instructions plus the number of input bits.
    final short[] names = new short[instructions.length + (word << 1)];
    for(byte i = 0;i < word;++i)
    {
        final short low = (short)(((i / 10) << 5) | (i % 10));
        names[i] = (short)((('x' - 'a') << 10) | low);
        names[i + word] = (short)((('y' - 'a') << 10) | low);
    }
    {
        final long[] parsed = new long[instructions.length];
        byte i = 0;
        outer:
        while(idx < input.length)
        {
            do {if(++idx >= input.length) break outer;}
            while(Character.isWhitespace(input[idx]));
            
            final short a = id(idx,input);
            
            idx += 4;
            final byte op = switch(input[idx])
            {
                case 'A' -> AND;
                case 'O' -> OR;
                default  -> XOR;
            };
            idx += op == OR ? 3 : 4;
            
            final short b = id(idx,input);
            
            idx += 7;
            final short c = id(idx,input);
            ArrayUtils.insertSorted(names,c,(i & 0xFF) + (word << 1));
            
            parsed[i++ & 0xFF] = ((a & 0xFFFFL) << 48) | ((b & 0xFFFFL) << 32) | ((c & 0xFFFFL) << 16) | op;
            idx += 3;
        }
        // assert (i & 0xFF) == instructions.length;
        // Pack the instructions to take up less space.
        for(i = 0;(i & 0xFF) < instructions.length;++i)
        {
            final short a = (short)ArrayUtils.find(names,(short)((parsed[i & 0xFF] >>> 48) & 0xFFFF),names.length),
                        b = (short)ArrayUtils.find(names,(short)((parsed[i & 0xFF] >>> 32) & 0xFFFF),names.length),
                        c = (short)ArrayUtils.find(names,(short)((parsed[i & 0xFF] >>> 16) & 0xFFFF),names.length);
            // assert x > 0 & y > 0 & z > 0;
            instructions[i & 0xFF] = ((a & 0x1FF) << ARG0) | ((b & 0x1FF) << ARG1) | ((c & 0x1FF) << OUTPUT) | (byte)(parsed[i & 0xFF] & 3);
        }
    }
    final short xBegin = (short)ArrayUtils.find(names,(short)(('x' - 'a') << 10),names.length),
                yBegin = (short)(xBegin + word),
                zBegin = (short)(yBegin + word),
                zEnd = (short)(zBegin + word + 1);
    // assert yBegin == ArrayUtils.find(names,(short)(('y' - 'a') << 10),names.length);
    // assert zBegin == ArrayUtils.find(names,(short)(('z' - 'a') << 10),names.length);
    
    // Part 1
    {
        final int[] available = BitSet.create(names.length),
                    memory = BitSet.create(names.length);
        BitSet.set(available,xBegin,zBegin);
        for(byte i = 0;i < word;++i)
        {
            if((x & (1L << i)) != 0)
                BitSet.set(memory,xBegin + i);
            if((y & (1L << i)) != 0)
                BitSet.set(memory,yBegin + i);
        }
        final int[] open = BitSet.create(instructions.length);
        while(true)
        {
            short i = (short)BitSet.firstUnsetBit(open,0,instructions.length);
            if(i < 0) break;
            do
            {
                final short a = (short)((instructions[i] >>> ARG0) & 0x1FF),
                            b = (short)((instructions[i] >>> ARG1) & 0x1FF),
                            c = (short)((instructions[i] >>> OUTPUT) & 0x1FF);
                if(BitSet.test(available,a) && BitSet.test(available,b))
                {
                    BitSet.set(available,c);
                    BitSet.set(open,i);
                    if
                    (
                        switch(instructions[i] & 3)
                        {
                            case AND -> BitSet.get(memory,a) & BitSet.get(memory,b);
                            case OR  -> BitSet.get(memory,a) | BitSet.get(memory,b);
                            default  -> BitSet.get(memory,a) ^ BitSet.get(memory,b);
                        } != 0
                    )
                        BitSet.set(memory,c);
                }
                i = (short)BitSet.firstUnsetBit(open,i + 1,instructions.length);
            }
            while(i >= 0);
        }
        long z = 0;
        for(byte i = word;i >= 0;--i)
            z = (z << 1) | BitSet.get(memory,zBegin + i);
        System.out.printf("Part 1: %d\n",z);
    }
    
    // Part 2
    final int[] swap = BitSet.create(names.length);
    for(final int i : instructions)
    {
        final short ia = (short)((i >>> ARG0) & 0x1FF),
                    ib = (short)((i >>> ARG1) & 0x1FF),
                    ic = (short)((i >>> OUTPUT) & 0x1FF);
        final byte io = (byte)(i & 3);
        inner:
        {
            if
            (
                io == XOR
                    // An XOR gate must operate on input bits or produce an output bit.
                    ? (zEnd <= ia | ia < xBegin) & (zEnd <= ic | ic < xBegin)
                    // A non-XOR gate must not produce an output bit which is not the last output bit.
                    : (zBegin <= ic & ic < zEnd - 1)
            )
                break inner;
            // An XOR gate must not be an argument to an OR gate.
            // An AND gate operating on non-input bits must be an argument to an OR gate.
            if(io == XOR | (io == AND & ia != xBegin & ib != xBegin))
                for(final int j : instructions)
                {
                    final short ja = (short)((j >>> ARG0) & 0x1FF),
                                jb = (short)((j >>> ARG1) & 0x1FF);
                    final byte jo = (byte)(j & 3);
                    if((ic == ja | ic == jb) & ((jo == OR) == (io == XOR)))
                        break inner;
                }
            continue;
        }
        BitSet.set(swap,ic);
    }
    
    System.out.print("Part 2: ");
    short n = (short)BitSet.firstSetBit(swap);
    if(n != -1)
    {
        System.out.print(name(names[n]));
        n = (short)BitSet.firstSetBit(swap,n + 1,names.length);
        while(n != -1)
        {
            System.out.printf(",%s",name(names[n]));
            n = (short)BitSet.firstSetBit(swap,n + 1,names.length);
        }
    }
    System.out.println();
}