import static java.nio.charset.StandardCharsets.UTF_8;
import static utils.HttpUtils.httpGet;

static byte popc(byte f)
{
    f -= (byte)((f >>> 1) & 0x55);
    f = (byte)((f & 0x33) + ((f >>> 2) & 0x33));
    return (byte)((f + (f >>> 4)) & 0x0F);
}

static final byte NW = (byte)0x7F,NC = (byte)0xBF,NE = (byte)0xDF,
                  CW = (byte)0xEF,CC = (byte)0x00,CE = (byte)0xF7,
                  SW = (byte)0xFB,SC = (byte)0xFD,SE = (byte)0xFE,
                  FF = (byte)0xFF;
static final byte[][] MASK_LUT =
{
    {NW,FF,FF,NC,FF,FF,NE},
    {FF,NW,FF,NC,FF,NE,FF},
    {FF,FF,NW,NC,NE,FF,FF},
    {CW,CW,CW,CC,CE,CE,CE},
    {FF,FF,SW,SC,SE,FF,FF},
    {FF,SW,FF,SC,FF,SE,FF},
    {SW,FF,FF,SC,FF,FF,SE}
};
static final byte[][] CHR_LUT =
{
    {'S',000,000,'S',000,000,'S'},
    {000,'A',000,'A',000,'A',000},
    {000,000,'M','M','M',000,000},
    {'S','A','M','X','M','A','S'},
    {000,000,'M','M','M',000,000},
    {000,'A',000,'A',000,'A',000},
    {'S',000,000,'S',000,000,'S'}
};
static final byte[] INIT_LUT =
{
    (byte)0x00,(byte)0x08,(byte)0x10,(byte)0x18,
    (byte)0x02,(byte)0x0B,(byte)0x16,(byte)0x1F,
    (byte)0x40,(byte)0x68,(byte)0xD0,(byte)0xF8,
    (byte)0x42,(byte)0x6B,(byte)0xD6,(byte)0xFF
};
static byte count(final byte[] input,final short width,final short r,final short c)
{
    // There's probably a way to squeeze more cache performance out of the loops, but I'm using
    // Java so it would probably be pointless to bother trying.
    final boolean N = r > 2,S = r + 3 < input.length / width,
                  W = c > 2,E = c + 3 < width;
    // Since we know that the string is 4 characters long, we can clip the search bounds if
    // 'r' or 'c' are too close to the edges.
    final short minR = N? (short)(r - 3) : 0,maxR = (short)(r + (S? 4 : 1)),
                minC = W? (short)(c - 3) : 0,maxC = (short)(c + (E? 4 : 1));
    // Initialize the set based on which search bounds were clipped.
    byte set = INIT_LUT[(N? 8 : 0) | (S? 4 : 0) | (W? 2 : 0) | (E? 1 : 0)];
    // If the letter at the corresponding position does not match the search string in that
    // direction, mask out the bit in the set corresponding to the appropriate direction.
    for(short fr = minR;fr < maxR;++fr)
        for(short fc = minC;fc < maxC;++fc)
            if(input[fr * width + fc] != CHR_LUT[fr - r + 3][fc - c + 3])
                set &= MASK_LUT[fr - r + 3][fc - c + 3];
    // Count the number of set bits and return.
    return popc(set);
}

static short part1(final byte[] input,final short width)
{
    // I could easily multi-thread this, but who cares lmao
    short count = 0;
    for(short r = 0;r * width < input.length;++r)
        for(short c = 0;c < width;++c)
            count += count(input,width,r,c);
    return count;
}

static short part2(final byte[] input,final short width)
{
    short count = 0;
    for(short r = 1;(r + 1) * width < input.length;++r)
        for(short c = 1;c + 1 < width;++c)
            /*
            It is trivial to prove that a valid pattern must be some rotation of the
            following grid:
                M . S      C0.C1
                . A .  ->  . A .
                M . S      C2.C3
            If we call the corner characters 'C0' to 'C3' by ascending row and column,
            then we can assert that ((C0 == C1) == (C2 == C3)). We can also say that
            ((C0 != C3) && (C1 != C2)) for the diagonals. Using these two assertions,
            we can take each diagonal pair and ensure that one corner is 'M' and the
            other is 'S'. To test this, all we need to do is use the XOR interference
            pattern. If the pattern is valid, then (('M'^'S'^C0^C3) + ('M'^'S'^C1^C2))
            must be zero.
            */
            if
            (
                input[r * width + c] == 'A' &&
                (
                    ('M' ^ 'S' ^ input[(r - 1) * width + c - 1] ^ input[(r + 1) * width + c + 1]) +
                    ('M' ^ 'S' ^ input[(r - 1) * width + c + 1] ^ input[(r + 1) * width + c - 1])
                ) == 0
            )
                ++count;
    
    return count;
}

static void main()
{
    byte[] input = httpGet("https://adventofcode.com/2024/day/4/input").getBytes(UTF_8);
    short i;
    for(i = 0;i < input.length;++i) if(Character.isWhitespace(input[i])) break;
    short j;
    for(j = i;j < input.length;++j) if(!Character.isWhitespace(input[j])) break;
    final byte[] flat = new byte[((input.length - 1 + j) / j) * i];
    for(short k = 0;k < ((input.length - 1 + j) / j);++k)
        System.arraycopy(input,k * j,flat,k * i,i);
    
    System.out.printf("Part 1: %d\n",part1(flat,i));
    System.out.printf("Part 2: %d\n",part2(flat,i));
}