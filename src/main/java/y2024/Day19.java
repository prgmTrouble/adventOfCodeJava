import static java.lang.Math.min;
import static java.nio.charset.StandardCharsets.UTF_8;
import static utils.HttpUtils.httpGet;

static byte tableIdx(final byte c)
{
    return switch(c)
    {
        case 'b' -> 0;
        case 'g' -> 1;
        case 'r' -> 2;
        case 'u' -> 3;
        default  -> 4;
    };
}

static long[] solution(final byte[] input)
{
    /* Initialize Dictionary */
    // Sort the dictionary portion of the input in ascending lexicographical order
    short[] dictionary = new short[1 << 8];
    final short[] table = new short[6];
    short i;
    {
        // Get the first word.
        i = 0;
        while(input[i] != ',')
            ++i;
        dictionary[1] = i;
        for(byte t = (byte)(tableIdx(input[0]) + 1);t < 6;++t)
            table[t] = 1;
        i += 2;
        
        boolean flag;
        final byte[] buf = new byte[8];
        do
        {
            // Read the next word into the buffer.
            short k;
            for(k = i;input[k] != ',' & (flag = !Character.isWhitespace(input[k]));++k)
                buf[k - i] = input[k];
            final byte wordSize = (byte)(k - i);
            
            // Binary search to find the insertion position
            short low = table[tableIdx(buf[0])],high = table[tableIdx(buf[0]) + 1];
            while(low < high)
            {
                final short mid = (short)((low + high - 1) >>> 1);
                // compare input[dictionary[mid]:dictionary[mid + 1]] to input[i:k]
                final byte dictSize = (byte)(dictionary[mid + 1] - dictionary[mid]);
                byte cmp,l;
                for(l = cmp = 0;l < min(wordSize,dictSize) & cmp == 0;++l)
                    cmp = (byte)(buf[l] - input[dictionary[mid] + l]);
                assert cmp != 0 | wordSize != dictSize : "duplicate dictionary entry";
                if((cmp == 0 & wordSize > dictSize) | cmp > 0)
                    low = (short)(mid + 1);
                else
                    high = mid;
            }
            
            // Move the word into the correct position.
            System.arraycopy
            (
                input,dictionary[low],
                input,dictionary[low] + wordSize,
                dictionary[table[5]] - dictionary[low]
            );
            System.arraycopy(buf,0,input,dictionary[low],wordSize);
            
            // Update the dictionary indices.
            for(byte t = (byte)(tableIdx(buf[0]) + 1);t < 6;++t)
                ++table[t];
            final short[] nextDictionary;
            if(table[5] == dictionary.length)
                System.arraycopy(dictionary,0,nextDictionary = new short[table[5] << 1],0,low + 1);
            else
                nextDictionary = dictionary;
            for(short j = table[5];j-- > low;)
                nextDictionary[j + 1] = (short)(dictionary[j] + wordSize);
            dictionary = nextDictionary;
            
            i = (short)(k + 2);
        }
        while(flag);
    }
    System.arraycopy(dictionary,0,dictionary = new short[table[5] + 1],0,table[5] + 1);
    /*
    'dictionary' and 'table' now has the following properties:
        > input[dictionary[k]:dictionary[k+1]] is the k-th word in the dictionary, in ascending
          lexicographical order.
        > All indices in the range dictionary[table[k]:table[k+1]] start with the letter
          {b,g,r,u,w}[k]
    */
    
    while(Character.isWhitespace(input[i]))
        ++i;
    
    /* Initialize Memo */
    // 'K' is a pair of indices into 'input' representing the range '[K::s,K::e)'. A pair
    // of keys are considered identical if both ranges contain the same characters.
    class K
    {
        final short s,e;
        K(final short s,final short e)
        {
            this.s = s;
            this.e = e;
        }
        @Override
        public int hashCode()
        {
            int result = 0;
            for(short i = s;i < e;++i)
                result = 31 * result + input[i];
            return result;
        }
        @Override
        public boolean equals(final Object obj)
        {
            if(!(obj instanceof final K other) || e - s != other.e - other.s)
                return false;
            for(short i = s,j = other.s;i < e;++i,++j)
                if(input[i] != input[j])
                    return false;
            return true;
        }
    }
    final Map<K,Long> memo = new HashMap<>(1 << 14);
    memo.put(new K((short)0,(short)0),1L); // Empty string base case
    
    /*
    The iterative algorithm is functionally equivalent to the following recursive algorithm:
        def helper(str,dict,memo):
            if(str in memo)
                return memo[str]
            o = 0
            for(d in dict)
                if(str[0:d.length] == d)
                    o += helper(str[d.length:],dict,memo)
            return o
        
        def solve(input,dict):
            memo = map<string,int>
            memo[""] = 1
            out = list<int>
            for(str in input)
                out.append(helper(str,dict,memo))
            return out
    */
    short[] stk = new short[1 << 5];
    short outSize = 0;
    long[] out = new long[1 << 5];
    do
    {
        // Find the end of this string.
        short k = i;
        do ++k;
        while(k < input.length && !Character.isWhitespace(input[k]));
        
        if(outSize == out.length)
            System.arraycopy(out,0,out = new long[outSize << 1],0,outSize);
        if(!memo.containsKey(new K(i,k)))
        {
            byte size = 2;
            short o = outSize;
            stk[1] = table[tableIdx(input[stk[0] = i])];
            out[o] = 0;
            while(true)
            {
                size -= 2;
                outer:
                while(stk[size | 1] < table[tableIdx(input[stk[size]]) + 1])
                {
                    // Compare the dictionary string at index (dictionary[stk[size|1]]) to the
                    // sequence starting at index (stk[size]).
                    final short beginSrc = dictionary[stk[size | 1]],
                                  endSrc = dictionary[++stk[size | 1]];
                    short src,dst;
                    for(src = beginSrc,dst = stk[size];src < endSrc & dst < k;++src,++dst)
                        if(input[src] > input[dst])
                            // Current dictionary string is lexicographically greater than the
                            // sequence, which means that there are no more matching dictionary
                            // strings for position stk[size].
                            break outer;
                        else if(input[src] < input[dst])
                            // Current dictionary string is lexicographically lesser than the
                            // sequence, which means that there may be more matching dictionary
                            // strings for position stk[size].
                            continue outer;
                    if(endSrc - beginSrc > k - stk[size])
                        // The strings are the same, but the dictionary string extends past the
                        // sequence and is thus lexicographically greater.
                        break;
                    
                    final K key = new K(dst,k);
                    if(memo.containsKey(key))
                        out[o] += memo.get(key);
                    else
                    {
                        // Push the current variables to the stack and continue the recursion.
                        if(++o == out.length)
                            System.arraycopy(out,0,out = new long[o << 1],0,o);
                        size += 2;
                        if(size == stk.length)
                            System.arraycopy(stk,0,stk = new short[stk.length << 1],0,size);
                        out[o] = 0;
                        stk[size | 1] = table[tableIdx(input[stk[size] = dst])];
                    }
                    
                }
                memo.put(new K(stk[size],k),out[o]);
                if(o > outSize)
                    out[o - 1] += out[o--];
                else
                    // assert size == 0;
                    break;
            }
        }
        else
            out[outSize] = memo.get(new K(i,k));
        ++outSize;
        
        i = k;
        do ++i;
        while(i < input.length && Character.isWhitespace(input[i]));
    }
    while(i < input.length);
    
    System.arraycopy(out,0,out = new long[outSize],0,outSize);
    return out;
}

static void main()
{
    final byte[] input = httpGet("https://adventofcode.com/2024/day/19/input").getBytes(UTF_8);
    final long[] out = solution(input);
    short part1 = 0;
    long part2 = 0;
    for(final long o : out)
    {
        part2 += o;
        if(o != 0)
            ++part1;
    }
    System.out.printf("Part 1: %d\nPart 2: %s\n",part1,part2);
}