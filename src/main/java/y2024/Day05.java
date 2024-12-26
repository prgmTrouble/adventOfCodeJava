import utils.BitSet;
import utils.map.ByteMap;

import java.util.Arrays;

import static java.nio.charset.StandardCharsets.UTF_8;
import static utils.HttpUtils.httpGet;

static short part1(final ByteMap<int[]> rules,final byte[][] updates)
{
    short medians = 0;
    final int[] visited = BitSet.create(100);
    outer:
    for(final byte[] update : updates)
    {
        Arrays.fill(visited,0);
        for(byte page = 0;page + 1 < update.length;++page)
        {
            // Ensure that the already visited values do not conflict with the rules.
            final int[] ruleset = rules.get(update[page]);
            if(ruleset != null)
                for(byte k = 0;k < visited.length;++k)
                    if((ruleset[k] & visited[k]) != 0)
                        continue outer;
            BitSet.set(visited,update[page]);
        }
        medians += update[update.length >> 1];
    }
    return medians;
}

static short part2(final ByteMap<int[]> fmap,final ByteMap<int[]> bmap,final byte[][] updates)
{
    short medians = 0;
    final int[] visited = BitSet.create(100);
    outer:
    for(final byte[] update : updates)
    {
        Arrays.fill(visited,0);
        for(byte page = 0;page + 1 < update.length;++page)
        {
            // Find a page where the already visited values conflict with the rules.
            int[] fget = fmap.get(update[page]);
            if(fget != null)
                for(byte k = 0;k < visited.length;++k)
                    if((fget[k] & visited[k]) != 0)
                    {
                        // Fill 'visited' with all pages that appear in the update.
                        for(;page < update.length;++page)
                            BitSet.set(visited,update[page]);
                        
                        // Find a page 'm' where both of the following are true:
                        // 1. For all 'a' in 'update', there exists a rule 'a|m' or 'm|a'
                        // 2. There are an equal number of rules 'a|m' and 'm|b' where 'a' and 'b' are in 'update'
                        byte m;
                        for(m = 0;m < update.length;++m)
                        {
                            fget = fmap.get(update[m]);
                            final int[] bget = bmap.get(update[m]);
                            
                            byte countA = 0,countB = 0;
                            if(fget != null)
                                for(byte i = 0;i < visited.length;++i)
                                    countA += (byte)Integer.bitCount(fget[i] & visited[i]);
                            if(bget != null)
                                for(byte i = 0;i < visited.length;++i)
                                    countB += (byte)Integer.bitCount(bget[i] & visited[i]);
                            
                            if(countA == countB && countA + countB == update.length - 1)
                                break;
                        }
                        
                        // Add the median and continue to the next update.
                        assert m != update.length - 1;
                        medians += update[m];
                        continue outer;
                    }
            BitSet.set(visited,update[page]);
        }
    }
    return medians;
}

static void main()
{
    final byte[] input = httpGet("https://adventofcode.com/2024/day/5/input").getBytes(UTF_8);
    
    // Parse rules
    byte[] rules = new byte[128];
    short size = 0;
    boolean ws = true;
    int i;
    outer:
    for(i = 0;i < input.length;++i)
        switch(input[i])
        {
            case '0','1','2','3','4',
                 '5','6','7','8','9' ->
            {
                // Parse a number
                ws = false;
                if(size == rules.length)
                    System.arraycopy(rules,0,rules = new byte[rules.length * 2],0,size);
                rules[size] = (byte)(rules[size] * 10 - '0' + input[i]);
            }
            case '|' ->
            {
                // Separator found: move the cursor
                ws = false;
                ++size;
            }
            case '\n' ->
            {
                if(ws)
                {
                    // Second line break found: escape the loop
                    ++i;
                    break outer;
                }
                // First line break found: move the cursor and set the flag
                ++size;
                ws = true;
            }
            default -> {/* do nothing */}
        }
    // Shrink the array
    System.arraycopy(rules,0,rules = new byte[size],0,size);
    
    // Parse updates
    size = 0;
    byte[][] updates = new byte[128][64];
    byte updateSize = 0;
    for(;i < input.length;++i)
        switch(input[i])
        {
            case '0','1','2','3','4',
                 '5','6','7','8','9' ->
            {
                // Parse a number
                if(updateSize == updates[size].length)
                    System.arraycopy(updates[size],0,updates[size] = new byte[updateSize * 2],0,updateSize);
                updates[size][updateSize] = (byte)(updates[size][updateSize] * 10 - '0' + input[i]);
            }
            case ',' -> ++updateSize; // Separator found: move the current update cursor
            case '\n' ->
            {
                // Line break found: shrink the current update array and move the update list cursor
                System.arraycopy(updates[size],0,updates[size] = new byte[++updateSize],0,updateSize);
                updateSize = 0;
                if(++size == updates.length)
                {
                    System.arraycopy(updates,0,updates = new byte[size * 2][],0,size);
                    for(int j = size;j < updates.length;++j)
                        updates[j] = new byte[64];
                }
            }
            default -> {/* do nothing */}
        }
    if(updateSize != 0) ++size; // Account for possible newline at EOF
    // Shrink the updates list array
    System.arraycopy(updates,0,updates = new byte[size][],0,size);
    
    // Create two maps for bitsets to make querying rules easier.
    // 'fmap[a]' yields a set of pages that must be placed after 'a'
    // 'bmap[a]' yields a set of pages that must be placed before 'a'
    final ByteMap<int[]> fmap = new ByteMap<>(100),
                         bmap = new ByteMap<>(100);
    for(int rule = 0;rule + 1 < rules.length;rule += 2)
    {
        BitSet.set
        (
            fmap.insert(rules[rule],() -> BitSet.create(100)),
            rules[rule + 1]
        );
        BitSet.set
        (
            bmap.insert(rules[rule + 1],() -> BitSet.create(100)),
            rules[rule]
        );
    }
    
    System.out.printf("Part 1: %d\n",part1(fmap,updates));
    System.out.printf("Part 2: %d\n",part2(fmap,bmap,updates));
}