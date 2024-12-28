import static java.nio.charset.StandardCharsets.UTF_8;
import static utils.HttpUtils.httpGet;

static int part1(final short[] in)
{
    int result = 0;
    short i = 0;
    while(i < in.length)
    {
        final short Ax = in[i++],Ay = in[i++],
                    Bx = in[i++],By = in[i++],
                    Cx = in[i++],Cy = in[i++];
        // Change the basis of vector C.
        final int det = Ax * By - Ay * Bx,
                   D0 = By * Cx - Bx * Cy,
                   D1 = Ax * Cy - Ay * Cx,
                   Q0 = D0 / det,
                   Q1 = D1 / det;
        // Ensure that the quotients are within bounds and the number of button presses are integers.
        if((0 < Q0) & (Q0 <= 100) & (0 < Q1) & (Q1 <= 100) & (D0 % det == 0) & (D1 % det == 0))
            result += 3 * Q0 + Q1;
    }
    return result;
}

static final long ERROR = 10000000000000L;
static long part2(final short[] in)
{
    long result = 0;
    short i = 0;
    while(i < in.length)
    {
        final short Ax = in[i++],Ay = in[i++],
                    Bx = in[i++],By = in[i++],
                    Cx = in[i++],Cy = in[i++];
        // Change the basis of vector C.
        final long det = Ax * By - Ay * Bx,
                    D0 = By * (Cx + ERROR) - Bx * (Cy + ERROR),
                    D1 = Ax * (Cy + ERROR) - Ay * (Cx + ERROR),
                    Q0 = D0 / det,
                    Q1 = D1 / det;
        // Ensure that the quotients are within bounds and the number of button presses are integers.
        if((0 < Q0) & (0 < Q1) & (D0 % det == 0) & (D1 % det == 0))
            result += 3 * Q0 + Q1;
    }
    return result;
}

static void main()
{
    final byte[] input = httpGet("https://adventofcode.com/2024/day/13/input").getBytes(UTF_8);
    short[] in = new short[1 << 11];
    byte flag = 0;
    short insert = 0;
    for(final byte b : input)
        if(('0' <= b) & (b <= '9'))
        {
            if(insert == in.length)
                System.arraycopy(in,0,in = new short[insert << 1],0,insert);
            in[insert] = (short)(10 * in[insert] - '0' + b);
            flag = 1;
        }
        else if(flag == 1)
        {
            flag = 0;
            ++insert;
        }
    System.arraycopy(in,0,in = new short[insert],0,insert);
    
    System.out.printf("Part 1: %d\n",part1(in));
    System.out.printf("Part 2: %d\n",part2(in));
}