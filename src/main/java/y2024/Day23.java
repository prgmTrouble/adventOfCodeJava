import utils.BitSet;

import static java.lang.Math.max;
import static java.nio.charset.StandardCharsets.UTF_8;
import static utils.HttpUtils.httpGet;

static final short NODE_COUNT = 27 * 27,
                   PREFIX_T = ('t' - 'a') * 27,
                   PREFIX_T_END = ('t' - 'a' + 1) * 27;
static final int EDGE_COUNT = NODE_COUNT * NODE_COUNT;

static int part1(final int[] network)
{
    int out = 0;
    int e = (int)BitSet.firstSetBit(network,PREFIX_T * NODE_COUNT,PREFIX_T_END * NODE_COUNT);
    while(e != -1)
    {
        final short t = (short)(e / NODE_COUNT),
                    a = (short)(e % NODE_COUNT);
        //assert a != t;
        if(PREFIX_T <= a & a < t)
        {
            // case (a.str[0] == 't' && t.str[1] > a.str[1])
            // (a) is a node starting with 't' that was already visited.
            e = (int)BitSet.firstSetBit(network,t * (NODE_COUNT + 1) + 1,PREFIX_T_END * NODE_COUNT);
            continue;
        }
        //assert t < a | a < PREFIX_T;
        
        // Count the number of nodes in the graph which are connected to both (t) and (a), excluding any
        // previously visited values of (t).
        for(short b = a;++b < PREFIX_T;)
            if(BitSet.test(network,t * NODE_COUNT + b) && BitSet.test(network,a * NODE_COUNT + b))
                ++out;
        for(short b = (short)max(a,t);++b < NODE_COUNT;)
            if(BitSet.test(network,t * NODE_COUNT + b) && BitSet.test(network,a * NODE_COUNT + b))
                ++out;
        
        e = (int)BitSet.firstSetBit(network,e + 1,PREFIX_T_END * NODE_COUNT);
    }
    
    return out;
}

static String part2(final int[] network)
{
    final int[] nodes = BitSet.create(NODE_COUNT);
    short size = 0;
    for(short v = 0;v < NODE_COUNT;++v)
        if(BitSet.test(network,v * NODE_COUNT,(v + 1) * NODE_COUNT))
        {
            ++size;
            BitSet.set(nodes,v);
        }
    // Remove the node with the smallest degree until all nodes in the network are
    // fully connected.
    while(size > 0)
    {
        short minD = (short)(size - 1),
              minV = -1;
        // If the largest connected subgraph is not unique, we want the one which is
        // lexicographically lesser than the others. This means we should remove nodes
        // in lexicographically descending order if they share the same minimum degree.
        short v = (short)BitSet.lastSetBit(nodes);
        //assert v != -1;
        do
        {
            final short d = (short)BitSet.count(network,v * NODE_COUNT,(v + 1) * NODE_COUNT);
            //assert d != 0;
            if(d < minD)
            {
                minD = d;
                minV = v;
            }
            v = (short)BitSet.lastSetBit(nodes,0,v);
        }
        while(v != -1);
        if(minV == -1)
            break;
        // Remove (minV) from the network.
        int s = (int)BitSet.firstSetBit(network,minV * NODE_COUNT,(minV + 1) * NODE_COUNT);
        while(s != -1)
        {
            BitSet.unset(network,(s % NODE_COUNT) * NODE_COUNT + minV);
            s = (int)BitSet.firstSetBit(network,s + 1,(minV + 1) * NODE_COUNT);
        }
        BitSet.unset(network,minV * NODE_COUNT,(minV + 1) * NODE_COUNT);
        BitSet.unset(nodes,minV);
        --size;
    }
    if(size == 0) return "";
    final byte[] out = new byte[size * 3 - 1];
    short idx = 0;
    short v = (short)BitSet.firstSetBit(nodes);
    if(v != -1)
    {
        out[idx++] = (byte)(v / 27 + 'a');
        out[idx++] = (byte)(v % 27 + 'a');
        v = (short)BitSet.firstSetBit(nodes,v + 1,NODE_COUNT);
        while(v != -1)
        {
            out[idx++] = ',';
            out[idx++] = (byte)(v / 27 + 'a');
            out[idx++] = (byte)(v % 27 + 'a');
            v = (short)BitSet.firstSetBit(nodes,v + 1,NODE_COUNT);
        }
    }
    //assert out.length == idx;
    return new String(out);
}

static void main()
{
    final byte[] input = httpGet("https://adventofcode.com/2024/day/23/input").getBytes(UTF_8);
    final int[] network = BitSet.create(EDGE_COUNT);
    int i = 0;
    do
    {
        final short first = (short)((input[i++] - 'a') * 27 - 'a' + input[i++]),
                   second = (short)((input[++i] - 'a') * 27 - 'a' + input[++i]);
        // assert first != second;
        BitSet.set(network,first * NODE_COUNT + second);
        BitSet.set(network,second * NODE_COUNT + first);
        
        do ++i; while(i < input.length && Character.isWhitespace(input[i]));
    }
    while(i < input.length);
    
    System.out.printf("Part 1: %d\n",part1(network));
    System.out.printf("Part 2: %s\n",part2(network));
}