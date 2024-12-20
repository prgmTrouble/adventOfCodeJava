import static java.nio.charset.StandardCharsets.UTF_8;
import static utils.HttpUtils.httpGet;

static final byte[] MUL = {'m','u','l','('},
                    NT  = {'\'','t','('};

static long part1(final byte[] input)
{
    long value = 0;
    byte state = 0;
    short a = 0,b = 0;
    for(final byte i : input)
        switch(state)
        {
            case 0,1,2,3 ->
            {
                if(i != MUL[state++]) state = 0;
            }
            case 4 ->
            {
                switch(i)
                {
                    case '0','1','2','3','4',
                         '5','6','7','8','9' ->
                        a = (short)(10 * a + (i - '0'));
                    case ',' ->
                        ++state;
                    default ->
                        a = state = 0;
                }
            }
            case 5 ->
            {
                switch(i)
                {
                    case '0','1','2','3','4',
                         '5','6','7','8','9':
                        b = (short)(10 * b + (i - '0'));
                        break;
                    case ')':
                        value += a * b;
                        // intentional fallthrough
                    default:
                        a = b = state = 0;
                }
            }
            //default -> {assert false;}
        }
    return value;
}

static long part2(final byte[] input)
{
    long value = 0;
    byte state = 0;
    short a = 0,b = 0;
    for(final byte i : input)
    {
        final byte ignoreMul = (byte)(state & 0x80),
                     stateId = (byte)(state & 0x7F);
        state = switch(stateId)
        {
            case 0 -> switch(i)
            {
                case 'm' -> ignoreMul == 0? (byte)1 : ignoreMul;
                case 'd' -> (byte)(ignoreMul | 6);
                default -> ignoreMul;
            };
            
            // mul
            case 1,2,3 -> i == MUL[state]? (byte)(state + 1) : 0;
            case 4 -> switch(i)
            {
                case '0','1','2','3','4',
                     '5','6','7','8','9' ->
                {
                    a = (short)(10 * a + (i - '0'));
                    yield 4;
                }
                case ',' -> 5;
                default -> (byte)(a = 0);
            };
            case 5 -> switch(i)
            {
                case '0','1','2','3','4',
                     '5','6','7','8','9':
                    b = (short)(10 * b + (i - '0'));
                    yield 5;
                case ')':
                    value += a * b;
                    // intentional fallthrough
                default:
                    yield (byte)(a = b = 0);
            };
            
            // do
            case 6 -> (byte)(ignoreMul | (i != 'o'? 0 : 7));
            case 7 -> (byte)(ignoreMul | switch(i)
            {
                case '(' -> 8;
                case 'n' -> 9;
                default -> 0;
            });
            case 8 -> i == ')'? 0 : ignoreMul;
            
            // don't
            case 9,10,11 -> (byte)(ignoreMul | (i == NT[stateId - 9]? (stateId + 1) : 0));
            case 12 -> i == ')'? (byte)0x80 : ignoreMul;
            
            default -> {assert false; yield 0;}
        };
    }
    return value;
}

static void main()
{
    final byte[] input = httpGet("https://adventofcode.com/2024/day/3/input").getBytes(UTF_8);
    System.out.printf("Part 1: %d\n",part1(input));
    System.out.printf("Part 2: %d\n",part2(input));
}