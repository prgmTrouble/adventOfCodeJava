import static utils.HttpUtils.httpGet;

static boolean invalid(final byte a,final byte b,final boolean parity)
{
    final byte diff = parity? (byte)(b - a) : (byte)(a - b);
    // 'diff < 1' also accounts for parity defects!
    return diff < 1 || diff > 3;
}
static byte defect(final byte[] report,final byte begin,final byte end,final boolean parity)
{
    for(byte i = begin;i < end - 1;++i)
        if(invalid(report[i],report[i + 1],parity))
            return i;
    return -1;
}

static int part1(final byte[][] reports)
{
    int safe = 0;
    for(final byte[] report : reports)
        if(report.length <= 1 || defect(report,(byte)0,(byte)report.length,report[0] < report[1]) == -1)
            ++safe;
    return safe;
}

static int part2(final byte[][] reports)
{
    int safe = 0;
    for(final byte[] report : reports)
    {
        if(report.length > 2)
        {
            final byte l = defect(report,(byte)0,(byte)(report.length - 1),report[0] < report[1]);
            if(l != -1)
            {
                // Use the parity of an interval that is guaranteed to be correct if removing
                // only one level from the report is possible.
                final boolean parity = l > 1
                    ? (report[0] < report[1])
                    : (report[report.length - 2] < report[report.length - 1]);
                
                // If the report is defective at level 'l' (i.e. the edge '[l,l+1]' is invalid):
                // Make sure that the range '[l+2,report.length)' are valid
                if(defect(report,(byte)(l + 2),(byte)report.length,parity) != -1)
                    continue;
                
                // Try removing 'l-1' (i.e. range '[l-2]U[l,l+2]'), 'l' (i.e. range '[l-1]U[l+1,l+2]'), or 'l+1' (i.e. range '[l]U[l+2]')
                assert l < 2 || !invalid(report[l - 2],report[l - 1],parity);
                // Each boolean is 'true' if the edge between the two specified levels is defective.
                // e.g.: 'vm11 == false' implies that 'l-1' and 'l+1' are valid neighbors
                final boolean vm20 = l > 1 && invalid(report[l - 2],report[l],parity);
                final boolean vm10 = l != 0 && invalid(report[l - 1],report[l],parity);
                final boolean vm11 = l != 0 && invalid(report[l - 1],report[l + 1],parity);
                final boolean v01 = invalid(report[l],report[l + 1],parity);
                final boolean v12 = l != report.length - 2 && invalid(report[l + 1],report[l + 2],parity);
                final boolean v02 = l != report.length - 2 && invalid(report[l],report[l + 2],parity);
                // Test removing 'l-1', 'l', and 'l+1', respectively
                if((vm20 || v01 || v12) && (vm11 || v12) && (vm10 || v02))
                    continue;
            }
        }
        
        ++safe;
    }
    return safe;
}

static void main()
{
    final String input = httpGet("https://adventofcode.com/2024/day/2/input");
    final String[] lines = input.split("\n");
    final byte[][] reports = new byte[lines.length][];
    for(int report = 0;report < lines.length;++report)
    {
        final String[] columns = lines[report].split(" ");
        reports[report] = new byte[columns.length];
        for(byte level = 0;level < columns.length;++level)
            reports[report][level] = Byte.parseByte(columns[level]);
    }
    System.out.printf("Part 1: %d\n",part1(reports));
    System.out.printf("Part 2: %d\n",part2(reports));
}