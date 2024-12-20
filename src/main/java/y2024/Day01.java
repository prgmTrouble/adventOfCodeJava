import static utils.ArrayUtils.insertSorted;
import static utils.HttpUtils.httpGet;

static long part1(final String[] lines)
{
    final int[][] list = new int[2][lines.length];
    int sizeL = 0,sizeR = 0;
    for(final String line : lines)
    {
        final String[] pair = line.split(" {3}",2);
        insertSorted(list[0],Integer.parseInt(pair[0]),sizeL++);
        insertSorted(list[1],Integer.parseInt(pair[1]),sizeR++);
    }
    
    long diff = 0;
    for(int i = 0;i < lines.length;++i)
        diff += Math.abs(list[0][i] - list[1][i]);
    return diff;
}

static byte increment(final int[] a,final int v,final int size,final byte parity)
{
    int low = 0,high = size - 1;
    while(low <= high)
    {
        final int mid = (low + high) >>> 1;
        
        if(a[mid * 3] < v) low = mid + 1;
        else if(a[mid * 3] > v) high = mid - 1;
        else
        {
            ++a[mid * 3 + parity];
            return 0;
        }
    }
    System.arraycopy(a,low * 3,a,low * 3 + 3,size * 3 - low * 3);
    a[low * 3] = v;
    a[low * 3 + parity] = 1;
    a[low * 3 + 2 + (1 - parity)] = 0;
    return 1;
}
static long part2(final String[] lines)
{
    final int[] list = new int[(lines.length << 1) * 3];
    int size = 0;
    for(final String line : lines)
    {
        final String[] pair = line.split(" {3}",2);
        size += increment(list,Integer.parseInt(pair[0]),size,(byte)1);
        size += increment(list,Integer.parseInt(pair[1]),size,(byte)2);
    }
    
    long score = 0;
    for(int i = 0;i < size;++i)
        score += (long)list[i * 3] * (long)list[i * 3 + 1] * (long)list[i * 3 + 2];
    return score;
}

static void main()
{
    final String[] input = httpGet("https://adventofcode.com/2024/day/1/input").split("\n");
    System.out.printf("Part 1: %d\n",part1(input));
    System.out.printf("Part 2: %d\n",part2(input));
}
