import static java.nio.charset.StandardCharsets.UTF_8;
import static utils.HttpUtils.httpGet;


static String cmb(final byte operand)
{
    return switch(operand)
    {
        case 0,1,2,3 -> Byte.toString(operand);
        case 4,5,6 -> Character.toString('A' - 4 + operand);
        default -> "<err>";
    };
}
static void asm(final byte[] program,final byte programSize)
{
    for(byte pc = 0;pc < programSize;pc += 2)
    {
        final byte shift = (byte)((pc * 3) & 7);
        byte operation = (byte)((program[(pc * 3) >>> 3] & 0xFF) >>> shift);
        if(shift > 2)
            operation |= (byte)((program[((pc * 3) >>> 3) + 1] & 0xFF) << (8 - shift));
        final byte operand = (byte)((operation >>> 3) & 7);
        String op = switch(operation & 7)
        {
            case 0 -> "A = A >>> " + cmb(operand);
            case 1 -> "B = B ^ " + operand;
            case 2 -> "B = 7 & " + cmb(operand);
            case 3 -> "jnz " + cmb(operand);
            case 4 -> "B = B ^ C";
            case 5 -> "out(" + cmb(operand) + ")";
            case 6 -> "B = A >>> " + cmb(operand);
            case 7 -> "C = A >>> " + cmb(operand);
            default -> "<err>";
        };
        System.out.println(op);
    }
}

static long comboOperand(final byte operand,final long[] registers)
{
    return switch(operand)
    {
        case 0,1,2,3 -> operand;
        case 4,5,6 -> registers[operand - 4];
        default ->
        {
            assert false : "reserved combo operand";
            throw new AssertionError("reserved combo operand");
        }
    };
}

static String part1(final byte[] program,final byte programSize,final long[] registers)
{
    final StringJoiner out = new StringJoiner(",");
    for(byte pc = 0;pc < programSize;)
    {
        final byte shift = (byte)((pc * 3) & 7);
        byte operation = (byte)((program[(pc * 3) >>> 3] & 0xFF) >>> shift);
        if(shift > 2)
            operation |= (byte)((program[((pc * 3) >>> 3) + 1] & 0xFF) << (8 - shift));
        final byte operand = (byte)((operation >>> 3) & 7);
        switch(operation & 7)
        {
            case 0,6,7 -> // adv,bdv,cdv
                registers[(operation & 3) - ((operation >>> 1) & 1)] = registers[0] >>> comboOperand(operand,registers);
            case 1,4 -> // bxl,bxc
                registers[1] ^= (operation & 1) != 0? operand : registers[2];
            case 2,5 -> // bst,out
            {
                final byte combo = (byte)(comboOperand(operand,registers) & 7);
                if((operation & 1) != 0)
                    out.add(Character.toString('0' + combo));
                else
                    registers[1] = combo;
            }
            case 3 -> // jnz
            {
                if(registers[0] != 0)
                {
                    pc = operand;
                    continue;
                }
            }
        }
        pc += 2;
    }
    return out.toString();
}

static boolean predicate(final byte[] program,final byte programSize,final long registerA)
{
    final long[] registers = {registerA,0,0};
    byte out = 0;
    for(byte pc = 0;pc < programSize;)
    {
        final byte shift = (byte)((pc * 3) & 7);
        byte operation = (byte)((program[(pc * 3) >>> 3] & 0xFF) >>> shift);
        if(shift > 2)
            operation |= (byte)((program[((pc * 3) >>> 3) + 1] & 0xFF) << (8 - shift));
        final byte operand = (byte)((operation >>> 3) & 7);
        switch(operation & 7)
        {
            case 0,6,7 -> // adv,bdv,cdv
                registers[(operation & 3) - ((operation >>> 1) & 1)] = registers[0] >>> comboOperand(operand,registers);
            case 1,4 -> // bxl,bxc
                registers[1] ^= (operation & 1) != 0? operand : registers[2];
            case 2,5 -> // bst,out
            {
                final byte combo = (byte)(comboOperand(operand,registers) & 7);
                if((operation & 1) != 0)
                {
                    if(out == programSize)
                        return false;
                    final byte shiftO = (byte)((out * 3) & 7);
                    byte operationO = (byte)((program[(out * 3) >>> 3] & 0xFF) >>> shiftO);
                    if(shiftO > 5)
                        operationO |= (byte)((program[((out * 3) >>> 3) + 1] & 0xFF) << (8 - shiftO));
                    if(combo != (operationO & 7))
                        return false;
                    ++out;
                }
                else
                    registers[1] = combo;
            }
            case 3 -> // jnz
            {
                if(registers[0] != 0)
                {
                    pc = operand;
                    continue;
                }
            }
        }
        pc += 2;
    }
    return out == programSize;
}

static long part2General(final byte[] program,final byte programSize)
{
    return LongStream
        .rangeClosed(0,Long.MAX_VALUE)
        .parallel()
        .filter(i -> predicate(program,programSize,i))
        .findFirst()
        .orElse(-1L);
}

static long part2Specialized()
{
    final byte[] prgm = {0,3,5,5,5,1,3,4,3,0,5,7,3,1,4,2}; // Reversed input
    final byte[] stk = new byte[prgm.length];
    byte cursor = 0;
    long out = 0;
    outer:
    while(cursor < prgm.length)
    {
        while(stk[cursor] < 8)
        {
            if(((stk[cursor] ^ 6 ^ (((out << 3) | stk[cursor]) >>> (stk[cursor] ^ 3))) & 7) == prgm[cursor])
            {
                out = (out << 3) | stk[cursor++];
                continue outer;
            }
            ++stk[cursor];
        }
        if(cursor == 0)
            return -1L;
        stk[cursor] = 0;
        out >>>= 3;
        ++stk[--cursor];
    }
    return out;
}

static void main()
{
    final byte[] input = httpGet("https://adventofcode.com/2024/day/17/input").getBytes(UTF_8);
    
    final long[] registers = {0,0,0};
    int cursor = 0;
    for(byte i = 0;i < registers.length;++i)
    {
        cursor += 12; // "Register #: ".length()
        while(!Character.isWhitespace(input[cursor]))
        {
            registers[i] = registers[i] * 10 - '0' + input[cursor];
            ++cursor;
        }
        while(Character.isWhitespace(input[cursor])) ++cursor;
    }
    cursor += 9; // "Program: ".length()
    int end = input.length;
    while(Character.isWhitespace(input[end - 1])) --end;
    final byte programSize = (byte)((end - cursor + 1) >>> 1);
    final byte[] program = new byte[(programSize * 3 + 7) >>> 3];
    for(byte i = 0;i < programSize;++i)
    {
        final byte opcode = (byte)(input[cursor] - '0'),
                    shift = (byte)((i * 3) & 7);
        program[(i * 3) >>> 3] |= (byte)(opcode << shift);
        if(shift > 5)
            program[((i * 3) >>> 3) + 1] = (byte)(opcode >>> (8 - shift));
        cursor += 2;
    }
    asm(program,programSize);
    System.out.println();
    System.out.printf("Part 1: %s\n",part1(program,programSize,registers));
    final long p2 = part2Specialized(); //part2General(program,programSize);
    System.out.printf("Part 2: %d\n",p2);
    registers[0] = p2;
    registers[1] = 0;
    registers[2] = 0;
    System.out.printf("verify: %s\n",part1(program,programSize,registers));
}