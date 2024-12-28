import utils.BitSet;

import static java.nio.charset.StandardCharsets.UTF_8;
import static utils.HttpUtils.httpGet;

static int part1(final byte[] in)
{
    final byte[] quad = new byte[4];
    int i = 0;
    while(i < in.length)
    {
        final byte px = in[i++],py = in[i++],
                   vx = in[i++],vy = in[i++],
                   fx = (byte)((px + vx * 100) % 101),
                   fy = (byte)((py + vy * 100) % 103),
                   gx = (byte)(fx + (fx < 0 ? 101 : 0)),
                   gy = (byte)(fy + (fy < 0 ? 103 : 0));
        if((gx != 50) & (gy != 51))
            ++quad[((gx > 50) ? 2 : 0) | ((gy > 51) ? 1 : 0)];
    }
    return (quad[0] & 0xFF) * (quad[1] & 0xFF) * (quad[2] & 0xFF) * (quad[3] & 0xFF);
}

static float variance(final byte[] v,final int total)
{
    final float mean = (float)total / v.length;
    float out = 0;
    for(final byte b : v)
    {
        final float d = b - mean;
        out = Math.fma(d,d / v.length,out);
    }
    return out;
}
static int part2(final byte[] in)
{
    // For this solution, we will assume the following:
    // 1. A "Christmas Tree" shape has a low variance in both the x and y dimensions
    // 2. The robots will not form a pattern with lower variance than a "Christmas Tree" before
    //    the "Christmas Tree" appears (i.e. the "Christmas Tree" pattern is the lowest variance
    //    possible)
    // Assuming the above conditions are met, we just need to find the lowest value 't' which
    // produces the pattern with the lowest variance.
    final byte[][] bots = new byte[2][in.length >>> 2];
    byte x = 0,y = 0;
    float varX = Float.POSITIVE_INFINITY,varY = Float.POSITIVE_INFINITY;
    // The x and y values are statistically independent and will repeat according to
    // their respective bounds. Therefore, we only need to find the lowest x and y
    // variances independently and then infer the time which the two x and y values
    // would occur.
    for(byte t = 0;t < 103;++t)
    {
        int totalX = 0,totalY = 0;
        for(int i = 0;i < bots[0].length;++i)
        {
            final byte fx = (byte)((in[i << 2] + in[(i << 2) + 2] * t) % 101),
                       fy = (byte)((in[(i << 2) + 1] + in[(i << 2) + 3] * t) % 103);
            totalX += (bots[0][i] = (byte)(fx + (fx < 0 ? 101 : 0)));
            totalY += (bots[1][i] = (byte)(fy + (fy < 0 ? 103 : 0)));
        }
        final float vx = variance(bots[0],totalX);
        if(vx < varX)
        {
            varX = vx;
            x = t;
        }
        final float vy = variance(bots[1],totalY);
        if(vy < varY)
        {
            varY = vy;
            y = t;
        }
    }
    // The desired value is a 't' such that (t = (x MOD W)) and (t = (y MOD H)).
    // We can introduce a value 'n' where (y MOD H = x + n * W)
    // After solving for 'n', we get (t = x + (inv(W,H)(y - x) MOD H) * W) where
    // 'inv(W,H)' is the modular multiplicative inverse of 'W' and 'H'
    final byte fz = (byte)((51 * (y - x)) % 103);
    return x + (fz + (fz < 0 ? 103 : 0)) * 101;
}
static void printTree(final byte[] in,final int t)
{
    final int[] map = BitSet.create(101 * 103);
    int i = 0;
    while(i < in.length)
    {
        final byte px = in[i++],py = in[i++],
                   vx = in[i++],vy = in[i++],
                   fx = (byte)((px + vx * t) % 101),
                   fy = (byte)((py + vy * t) % 103),
                   gx = (byte)(fx + (fx < 0 ? 101 : 0)),
                   gy = (byte)(fy + (fy < 0 ? 103 : 0));
        BitSet.set(map,gy * 101 + gx);
    }
    for(byte r = 0;r < 103;++r)
    {
        System.out.println();
        for(byte c = 0;c < 101;++c)
            System.out.print(BitSet.test(map,r * 101 + c) ? '#' : ' ');
    }
}

static void main()
{
    byte[] input = httpGet("https://adventofcode.com/2024/day/14/input").getBytes(UTF_8);
    byte flag = 0;
    short write = 0;
    for(int read = 0;read < input.length;++read)
        if(flag == 0)
        {
            if(input[read] == '-')
            {
                flag = 1;
                input[write] = 0;
            }
            else if(('0' <= input[read]) & (input[read] <= '9'))
            {
                flag = 2;
                input[write] = (byte)(input[read] - '0');
            }
        }
        else if(('0' <= input[read]) & (input[read] <= '9'))
            input[write] = (byte)(10 * input[write] + (flag == 1 ? ('0' - input[read]) : (input[read] - '0')));
        else
        {
            ++write;
            flag = 0;
        }
    if(flag != 0)
        ++write;
    System.arraycopy(input,0,input = new byte[write],0,write);
    
    System.out.printf("Part 1: %d\n",part1(input));
    final int t = part2(input);
    System.out.printf("Part 2: %d\n",t);
    printTree(input,t);
}