package utils;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static testutil.TestUtils.parallelRun;

public class BitSetTest
{
    static final byte ARR_SIZE = 8;
    static final short N_BITS = Integer.SIZE * ARR_SIZE;
    
    static BigInteger repeat(byte value)
    {
        final byte[] b = new byte[N_BITS / 8];
        Arrays.fill(b,value);
        return new BigInteger(1,b);
    }
    static final BigInteger[] PATTERNS =
    {
        BigInteger.ZERO,
        repeat((byte)0x01),repeat((byte)0x02),repeat((byte)0x04),repeat((byte)0x08),
        repeat((byte)0x10),repeat((byte)0x20),repeat((byte)0x40),repeat((byte)0x80),
        repeat((byte)0x11),repeat((byte)0x22),repeat((byte)0x33),repeat((byte)0x44),
        repeat((byte)0x55),repeat((byte)0x66),repeat((byte)0x77),repeat((byte)0x88),
        repeat((byte)0x99),repeat((byte)0xAA),repeat((byte)0xBB),repeat((byte)0xCC),
        repeat((byte)0xDD),repeat((byte)0xEE),repeat((byte)0xFF),
    };
    static void toIntArray(final BigInteger bigint,final int[] arr)
    {
        final byte[] b = bigint.toByteArray();
        Arrays.fill(arr,0);
        final byte sgn = (byte)(b.length - (bigint.bitLength() + 7) / 8);
        byte i,j;
        for(i = (byte)(b.length - 1),j = 0;i - 3 >= sgn;i -= 4,++j)
        {
            arr[j] =
                (b[i] & 0xFF) |
                ((b[i - 1] & 0xFF) << 8) |
                ((b[i - 2] & 0xFF) << 16) |
                ((b[i - 3] & 0xFF) << 24);
        }
        if(i >= sgn)
            arr[j] |= b[i] & 0xFF;
        if(i - 1 >= sgn)
            arr[j] |= (b[i - 1] & 0xFF) << 8;
        if(i - 2 >= sgn)
            arr[j] |= (b[i - 2] & 0xFF) << 16;
    }
    
    static void printArr(final int[] p)
    {
        for(byte i = ARR_SIZE;i-- > 0;)
        {
            String b = Integer.toBinaryString(p[i]);
            System.out.print(" ");
            System.out.print("0".repeat(32 - b.length()));
            System.out.print(b);
        }
        System.out.println();
    }
    static void printIndex(final String name,final char c,final int i)
    {
        System.out.printf("%s:%s%c\n",name," ".repeat(index(i)),c);
    }
    static void printRange(final int start,final int end)
    {
        System.out.printf("s: %d,e: %d\n",start,end);
        final int spE = index(end),spS = index(start);
        final char[] buf = new char[spE + spS + 1];
        Arrays.fill(buf,' ');
        buf[spE] = 'e';
        buf[spS] = 's';
        if(spE + 1 < spS)
            Arrays.fill(buf,spE + 1,spS,'-');
        System.out.print("ind:");
        System.out.println(buf);
    }
    static void printShift(final int start,final int end,final int amount,final boolean parity)
    {
        System.out.printf("a: %d,s: %d,e: %d\n",amount,start,end);
        final char[] buf = new char[N_BITS + (N_BITS >>> 5)];
        Arrays.fill(buf,' ');
        final int spE = index(end),
                  spS = index(start),
                  spA = index((parity? start : end) - amount);
        if(parity)
            Arrays.fill(buf,spA + 1,spS,'<');
        else
            Arrays.fill(buf,spE + 1,spA,'>');
        buf[spA] = 'v';
        buf[spE] = 'e';
        buf[spS] = 's';
        System.out.print("ind:");
        System.out.println(buf);
    }
    static int[] input(final String name,final BigInteger data)
    {
        final int[] d = new int[ARR_SIZE];
        toIntArray(data,d);
        System.out.printf("%s:",name);
        printArr(d);
        return d;
    }
    static int[] input(final BigInteger data) {return input("in ",data);}
    static void expected(final BigInteger p)
    {
        final int[] d = new int[ARR_SIZE];
        toIntArray(p,d);
        System.out.print("exp:");
        printArr(d);
    }
    static void expectedIndex(final int i) {printIndex("exp",'^',i);}
    static void actual(final int[] data)
    {
        System.out.print("act:");
        printArr(data);
    }
    static void actualIndex(final int i) {printIndex("act",'^',i);}
    static void actualException(final Exception e)
    {
        System.out.println("act:");
        e.printStackTrace();
    }
    static int index(final int i)
    {
        final int I = N_BITS - i - 1;
        return I > 0? 33 * (I >>> 5) + (I & 0b11111) + 1 : 0;
    }
    
    static BigInteger mask(final int start,final int end)
    {
        return BigInteger
            .ONE
            .shiftLeft(end)
            .subtract(BigInteger.ONE)
            .andNot
            (
                BigInteger
                    .ONE
                    .shiftLeft(start)
                    .subtract(BigInteger.ONE)
            );
    }
    static BigInteger not(final BigInteger i)
    {
        return i.xor(BigInteger.ONE.shiftLeft(N_BITS).subtract(BigInteger.ONE));
    }
    static BigInteger maskedLsh(final BigInteger i,final BigInteger m,final int amount)
    {
        return i
            .andNot(m)
            .or
            (
                i
                    .and(m)
                    .shiftLeft(amount)
                    .and(m)
            );
    }
    
    static final ThreadLocal<int[][]> DATA = ThreadLocal.withInitial(() -> new int[5][ARR_SIZE]);
    
    static <T> void exec(final Function<BigInteger,Callable<T>> testFactory,final Consumer<T> output)
    {
        @SuppressWarnings("unchecked")
        final Callable<T>[] tasks = (Callable<T>[])new Callable[PATTERNS.length + 32];
        byte p;
        for(p = 0;p < PATTERNS.length;++p)
            tasks[p] = testFactory.apply(PATTERNS[p]);
        final SecureRandom rand = new SecureRandom();
        for(;p < tasks.length;++p)
            tasks[p] = testFactory.apply(new BigInteger(N_BITS,rand));
        assertFalse(parallelRun(Arrays.asList(tasks),output));
    }
    
    static <T> void exec2D(final BiFunction<BigInteger,BigInteger,Callable<T>> testFactory,final Consumer<T> output)
    {
        @SuppressWarnings("unchecked")
        final Callable<T>[] tasks = (Callable<T>[])new Callable[2 * PATTERNS.length * (PATTERNS.length - 1) + 32 * 4];
        short p = 0;
        for(byte a = 0;a < PATTERNS.length - 1;++a)
            for(byte b = (byte)(a + 1);b < PATTERNS.length;++b)
            {
                tasks[p++] = testFactory.apply(PATTERNS[a],PATTERNS[a]);
                tasks[p++] = testFactory.apply(PATTERNS[a],PATTERNS[b]);
                tasks[p++] = testFactory.apply(PATTERNS[b],PATTERNS[a]);
                tasks[p++] = testFactory.apply(PATTERNS[b],PATTERNS[b]);
            }
        final SecureRandom rand = new SecureRandom();
        for(byte q = 0;q < 32;++q)
        {
            final BigInteger a = new BigInteger(N_BITS,rand),
                             b = new BigInteger(N_BITS,rand);
            tasks[p++] = testFactory.apply(a,a);
            tasks[p++] = testFactory.apply(a,b);
            tasks[p++] = testFactory.apply(b,a);
            tasks[p++] = testFactory.apply(b,b);
        }
        assertFalse(parallelRun(Arrays.asList(tasks),output));
    }
    
    @Test
    public void test()
    {
        record Err(BigInteger data,int index) {}
        exec
        (
            p -> () ->
            {
                final int[] data = DATA.get()[0];
                toIntArray(p,data);
                for(int i = 0;i < N_BITS;++i)
                {
                    final boolean act;
                    try {act = BitSet.test(data,i);}
                    catch(final Exception e) {return new Err(p,i);}
                    if(act != p.testBit(i))
                        return new Err(p,i);
                }
                return null;
            },
            e ->
            {
                printIndex("ind",'v',e.index);
                final int[] data = input(e.data);
                System.out.printf("exp:%d\n",e.data.testBit(e.index)? 1 : 0);
                try {System.out.printf("act:%d\n",BitSet.test(data,e.index)? 1 : 0);}
                catch(final Exception ex) {actualException(ex);}
            }
        );
    }
    
    @FunctionalInterface interface BitSetMapOpSingle {void accept(int[] set,long pos);}
    @FunctionalInterface interface BigIntMapOpSingle {BigInteger apply(BigInteger i,int pos);}
    static void mapOp_single(final BitSetMapOpSingle func,final BigIntMapOpSingle control)
    {
        record Err(BigInteger data,int index) {}
        exec
        (
            p -> () ->
            {
                final int[][] data = DATA.get();
                toIntArray(p,data[0]);
                for(int i = 0;i < N_BITS;++i)
                {
                    byte parity = (byte)((i & 1) << 1);
                    System.arraycopy(data[0],0,data[1 + parity],0,ARR_SIZE);
                    try {func.accept(data[1 + parity],i);}
                    catch(final Exception ex) {return new Err(p,i);}
                    toIntArray(control.apply(p,i),data[2 + parity]);
                    if(!Arrays.equals(data[1 + parity],data[2 + parity]))
                        return new Err(p,i);
                }
                return null;
            },
            e ->
            {
                printIndex("ind",'v',e.index);
                final int[] data = input(e.data);
                expected(control.apply(e.data,e.index));
                try
                {
                    func.accept(data,e.index);
                    actual(data);
                }
                catch(final Exception ex) {actualException(ex);}
            }
        );
    }
    @FunctionalInterface interface BitSetMapOpRange {void accept(int[] set,long start,long end);}
    @FunctionalInterface interface BigIntMapOpRange {BigInteger apply(BigInteger i,BigInteger m);}
    static void mapOp_range(final BitSetMapOpRange func,final BigIntMapOpRange control)
    {
        record Err(BigInteger data,int start,int end) {}
        exec
        (
            p -> () ->
            {
                final int[][] data = DATA.get();
                toIntArray(p,data[0]);
                byte parity = 0;
                for(int i = 0;i < N_BITS;++i)
                    for(int j = i;j <= N_BITS;++j)
                    {
                        System.arraycopy(data[0],0,data[1 + parity],0,ARR_SIZE);
                        try {func.accept(data[1 + parity],i,j);}
                        catch(final Exception ex) {return new Err(p,i,j);}
                        toIntArray(control.apply(p,mask(i,j)),data[2 + parity]);
                        if(!Arrays.equals(data[1 + parity],data[2 + parity]))
                            return new Err(p,i,j);
                        parity ^= 2;
                    }
                return null;
            },
            e ->
            {
                printRange(e.start,e.end);
                final int[] data = input(e.data);
                expected(control.apply(e.data,mask(e.start,e.end)));
                try
                {
                    func.accept(data,e.start,e.end);
                    actual(data);
                }
                catch(final Exception ex) {actualException(ex);}
            }
        );
    }
    
    @Test public void set_single() {mapOp_single(BitSet::set,BigInteger::setBit);}
    @Test public void set_range() {mapOp_range(BitSet::set,BigInteger::or);}
    
    @Test public void unset_single() {mapOp_single(BitSet::unset,BigInteger::clearBit);}
    @Test public void unset_range() {mapOp_range(BitSet::unset,BigInteger::andNot);}
    
    @Test public void toggle_single() {mapOp_single(BitSet::toggle,BigInteger::flipBit);}
    @Test public void toggle_range() {mapOp_range(BitSet::toggle,BigInteger::xor);}
    
    @Test
    public void select_foreign()
    {
        record Err(BigInteger data,int index) {}
        exec
        (
            p -> () ->
            {
                final int[][] data = DATA.get();
                toIntArray(p,data[0]);
                for(int i = 0;i < N_BITS;++i)
                {
                    byte parity = (byte)((i & 1) << 1);
                    try {BitSet.select(data[1 + parity],data[0],i);}
                    catch(final Exception ex) {return new Err(p,i);}
                    toIntArray(p.and(mask(i,i + 1)),data[2 + parity]);
                    if(!Arrays.equals(data[1 + parity],data[2 + parity]))
                        return new Err(p,i);
                }
                return null;
            },
            e ->
            {
                printIndex("ind",'v',e.index);
                final int[] data = input(e.data);
                expected(e.data.and(mask(e.index,e.index + 1)));
                try
                {
                    final int[] act = new int[ARR_SIZE];
                    Arrays.fill(act,0xCCCCCCCC);
                    BitSet.select(act,data,e.index);
                    actual(act);
                }
                catch(final Exception ex) {actualException(ex);}
            }
        );
    }
    
    @Test
    public void select_self()
    {
        record Err(BigInteger data,int index) {}
        exec
        (
            p -> () ->
            {
                final int[][] data = DATA.get();
                toIntArray(p,data[0]);
                for(int i = 0;i < N_BITS;++i)
                {
                    byte parity = (byte)((i & 1) << 1);
                    System.arraycopy(data[0],0,data[1 + parity],0,ARR_SIZE);
                    try {BitSet.select(data[1 + parity],i);}
                    catch(final Exception ex) {return new Err(p,i);}
                    toIntArray(p.and(mask(i,i + 1)),data[2 + parity]);
                    if(!Arrays.equals(data[1 + parity],data[2 + parity]))
                        return new Err(p,i);
                }
                return null;
            },
            e ->
            {
                printIndex("ind",'v',e.index);
                final int[] data = input(e.data);
                expected(e.data.and(mask(e.index,e.index + 1)));
                try
                {
                    BitSet.select(data,e.index);
                    actual(data);
                }
                catch(final Exception ex) {actualException(ex);}
            }
        );
    }
    
    @FunctionalInterface interface BitSetMaskForeign {void mask(int[] a,int[] b,int start,int end);}
    @FunctionalInterface interface BitSetMaskSelf {void mask(int[] set,int start,int end);}
    @FunctionalInterface interface BigIntMask {BigInteger mask(BigInteger p,BigInteger m);}
    static void mask_foreign(final BitSetMaskForeign func,final BigIntMask control)
    {
        record Err(BigInteger data,int start,int end) {}
        exec
        (
            p -> () ->
            {
                final int[][] data = DATA.get();
                toIntArray(p,data[0]);
                byte parity = 0;
                for(int i = 0;i < N_BITS;++i)
                    for(int j = i;j <= N_BITS;++j)
                    {
                        try {func.mask(data[1 + parity],data[0],i,j);}
                        catch(final Exception ex) {return new Err(p,i,j);}
                        toIntArray(control.mask(p,mask(i,j)),data[2 + parity]);
                        if(!Arrays.equals(data[1 + parity],data[2 + parity]))
                            return new Err(p,i,j);
                        parity ^= 2;
                    }
                return null;
            },
            e ->
            {
                printRange(e.start,e.end);
                final int[] data = input(e.data);
                expected(control.mask(e.data,mask(e.start,e.end)));
                try
                {
                    final int[] act = new int[ARR_SIZE];
                    Arrays.fill(act,0xCCCCCCCC);
                    func.mask(act,data,e.start,e.end);
                    actual(act);
                }
                catch(final Exception ex) {actualException(ex);}
            }
        );
    }
    static void mask_self(final BitSetMaskSelf func,final BigIntMask control)
    {
        record Err(BigInteger data,int start,int end) {}
        exec
        (
            p -> () ->
            {
                final int[][] data = DATA.get();
                toIntArray(p,data[0]);
                byte parity = 0;
                for(int i = 0;i < N_BITS;++i)
                    for(int j = i;j <= N_BITS;++j)
                    {
                        System.arraycopy(data[0],0,data[1 + parity],0,ARR_SIZE);
                        try {func.mask(data[1 + parity],i,j);}
                        catch(final Exception ex) {return new Err(p,i,j);}
                        toIntArray(control.mask(p,mask(i,j)),data[2 + parity]);
                        if(!Arrays.equals(data[1 + parity],data[2 + parity]))
                            return new Err(p,i,j);
                        parity ^= 2;
                    }
                return null;
            },
            e ->
            {
                printRange(e.start,e.end);
                final int[] data = input(e.data);
                expected(control.mask(e.data,mask(e.start,e.end)));
                try
                {
                    func.mask(data,e.start,e.end);
                    actual(data);
                }
                catch(final Exception ex) {actualException(ex);}
            }
        );
    }
    
    @Test public void mask_foreign() {mask_foreign(BitSet::mask,BigInteger::and);}
    @Test public void mask_self() {mask_self(BitSet::mask,BigInteger::and);}
    
    @Test public void inverseMask_foreign() {mask_foreign(BitSet::inverseMask,(i,m) -> i.and(not(m)));}
    @Test public void inverseMask_self() {mask_self(BitSet::inverseMask,(i,m) -> i.and(not(m)));}
    
    @Test
    public void not_foreign()
    {
        exec
        (
            p -> () ->
            {
                final int[][] data = DATA.get();
                toIntArray(p,data[0]);
                try {BitSet.not(data[1],data[0]);}
                catch(final Exception ex) {return p;}
                toIntArray(not(p),data[2]);
                if(!Arrays.equals(data[1],data[2]))
                    return p;
                return null;
            },
            p ->
            {
                final int[] data = input(p);
                expected(not(p));
                try
                {
                    final int[] act = new int[ARR_SIZE];
                    Arrays.fill(act,0xCCCCCCCC);
                    BitSet.not(act,data);
                    actual(act);
                }
                catch(final Exception ex) {actualException(ex);}
            }
        );
    }
    
    @Test
    public void not_self()
    {
        exec
        (
            p -> () ->
            {
                final int[][] data = DATA.get();
                toIntArray(p,data[0]);
                try {BitSet.not(data[0]);}
                catch(final Exception e) {return p;}
                toIntArray(not(p),data[1]);
                if(!Arrays.equals(data[0],data[1]))
                    return p;
                return null;
            },
            p ->
            {
                final int[] data = input(p);
                expected(not(p));
                try
                {
                    BitSet.not(data);
                    actual(data);
                }
                catch(final Exception ex) {actualException(ex);}
            }
        );
    }
    
    @FunctionalInterface interface ForeignBitSetConsumer {void accept(int[] a,int[] b,int[] c);}
    @FunctionalInterface interface SelfBitSetConsumer {void accept(int[] a,int[] b);}
    @FunctionalInterface interface BigIntFunction {BigInteger apply(BigInteger a,BigInteger b);}
    static void binaryOperation_foreign(final ForeignBitSetConsumer func,final BigIntFunction control)
    {
        record Err(BigInteger a,BigInteger b) {}
        exec2D
        (
            (p,q) -> () ->
            {
                final int[][] data = DATA.get();
                toIntArray(p,data[0]);
                toIntArray(q,data[1]);
                Arrays.fill(data[2],0xCCCCCCCC);
                try {func.accept(data[2],data[0],data[1]);}
                catch(final Exception ex) {return new Err(p,q);}
                toIntArray(control.apply(p,q),data[3]);
                if(!Arrays.equals(data[2],data[3]))
                    return new Err(p,q);
                return null;
            },
            e ->
            {
                final int[] a = input("b  ",e.a),
                            b = input("c  ",e.b);
                expected(control.apply(e.a,e.b));
                try
                {
                    final int[] c = new int[ARR_SIZE];
                    Arrays.fill(c,0xCCCCCCCC);
                    func.accept(c,a,b);
                    actual(c);
                }
                catch(final Exception ex) {actualException(ex);}
            }
        );
    }
    static void binaryOperation_self(final SelfBitSetConsumer func,final BigIntFunction control)
    {
        record Err(BigInteger a,BigInteger b) {}
        exec2D
        (
            (p,q) -> () ->
            {
                final int[][] data = DATA.get();
                toIntArray(p,data[0]);
                toIntArray(q,data[1]);
                try {func.accept(data[0],data[1]);}
                catch(final Exception ex) {return new Err(p,q);}
                toIntArray(control.apply(p,q),data[2]);
                if(!Arrays.equals(data[0],data[2]))
                    return new Err(p,q);
                return null;
            },
            e ->
            {
                final int[] a = input("a  ",e.a),
                            b = input("b  ",e.b);
                expected(control.apply(e.a,e.b));
                try
                {
                    func.accept(a,b);
                    actual(a);
                }
                catch(final Exception ex) {actualException(ex);}
            }
        );
    }
    
    @Test public void and_foreign() {binaryOperation_foreign(BitSet::and,BigInteger::and);}
    @Test public void and_self() {binaryOperation_self(BitSet::and,BigInteger::and);}
    
    @Test public void andNot_foreign() {binaryOperation_foreign(BitSet::andNot,BigInteger::andNot);}
    @Test public void andNot_self() {binaryOperation_self(BitSet::andNot,BigInteger::andNot);}
    
    @Test public void or_foreign() {binaryOperation_foreign(BitSet::or,BigInteger::or);}
    @Test public void or_self() {binaryOperation_self(BitSet::or,BigInteger::or);}
    
    @Test public void xor_foreign() {binaryOperation_foreign(BitSet::xor,BigInteger::xor);}
    @Test public void xor_self() {binaryOperation_self(BitSet::xor,BigInteger::xor);}
    
    @FunctionalInterface interface BitSetFindAll {long find(int[] set);}
    @FunctionalInterface interface BigIntFind {int find(BigInteger set);}
    static void find_all(final BitSetFindAll func,final BigIntFind control)
    {
        exec
        (
            p -> () ->
            {
                final int[] data = DATA.get()[0];
                toIntArray(p,data);
                final long act;
                try {act = func.find(data);}
                catch(final Exception ex) {return p;}
                if(act != control.find(p))
                    return p;
                return null;
            },
            p ->
            {
                final int[] data = input(p);
                expectedIndex(control.find(p));
                try {actualIndex((int)func.find(data));}
                catch(final Exception ex) {actualException(ex);}
            }
        );
    }
    @FunctionalInterface interface BitSetFindRange {long find(int[] set,final long start,final long end);}
    static void find_range(final BitSetFindRange func,final BigIntFind control)
    {
        record Err(BigInteger data,int start,int end) {}
        exec
        (
            p -> () ->
            {
                final int[] data = DATA.get()[0];
                toIntArray(p,data);
                for(int i = 0;i < N_BITS - 1;++i)
                    for(int j = i;j <= N_BITS;++j)
                    {
                        final long act;
                        try {act = func.find(data,i,j);}
                        catch(final Exception ex) {return new Err(p,i,j);}
                        if(act != control.find(p.and(mask(i,j))))
                            return new Err(p,i,j);
                    }
                return null;
            },
            e ->
            {
                printRange(e.start,e.end);
                final int[] data = input(e.data);
                expectedIndex(control.find(e.data.and(mask(e.start,e.end))));
                try {actualIndex((int)func.find(data,e.start,e.end));}
                catch(final Exception ex) {actualException(ex);}
            }
        );
    }
    
    @Test public void firstSetBit_all() {find_all(BitSet::firstSetBit,BigInteger::getLowestSetBit);}
    @Test public void firstSetBit_range() {find_range(BitSet::firstSetBit,BigInteger::getLowestSetBit);}
    
    @Test public void firstUnsetBit_all() {find_all(BitSet::firstUnsetBit,i -> not(i).getLowestSetBit());}
    @Test public void firstUnsetBit_range() {find_all(BitSet::firstUnsetBit,i -> not(i).getLowestSetBit());}
    
    @Test public void lastSetBit_all() {find_all(BitSet::lastSetBit,i -> i.bitLength() - 1);}
    @Test public void lastSetBit_range() {find_all(BitSet::lastSetBit,i -> i.bitLength() - 1);}
    
    @Test public void lastUnsetBit_all() {find_all(BitSet::lastUnsetBit,i -> not(i).bitLength() - 1);}
    @Test public void lastUnsetBit_range() {find_all(BitSet::lastUnsetBit,i -> not(i).bitLength() - 1);}
    
    @FunctionalInterface interface BitSetShiftAll {void shift(int[] set,long amount);}
    @FunctionalInterface interface BigIntShiftAll {BigInteger shift(BigInteger data,int amount);}
    static void shift_all(final BitSetShiftAll func,final BigIntShiftAll control,final boolean parity)
    {
        record Err(BigInteger data,int amount) {}
        exec
        (
            p -> () ->
            {
                final int[][] data = DATA.get();
                toIntArray(p,data[0]);
                byte offset = 0;
                for(int i = 0;i < N_BITS + 32;++i)
                {
                    System.arraycopy(data[0],0,data[1 + offset],0,ARR_SIZE);
                    try {func.shift(data[1 + offset],i);}
                    catch(final Exception ex) {return new Err(p,i);}
                    toIntArray(control.shift(p,i),data[2 + offset]);
                    if(!Arrays.equals(data[1 + offset],data[2 + offset]))
                        return new Err(p,i);
                    offset ^= 2;
                }
                return null;
            },
            e ->
            {
                printShift(0,N_BITS,e.amount,parity);
                final int[] data = input(e.data);
                expected(control.shift(e.data,e.amount));
                try
                {
                    func.shift(data,e.amount);
                    actual(data);
                }
                catch(final Exception ex) {actualException(ex);}
            }
        );
    }
    @FunctionalInterface interface BitSetShiftRange {void shift(int[] set,long start,long end,long amount);}
    @FunctionalInterface interface BigIntShiftRange {BigInteger shift(BigInteger data,int start,int end,int amount);}
    static void shift_range(final BitSetShiftRange func,final BigIntShiftRange control,final boolean parity)
    {
        record Err(BigInteger data,int start,int end,int amount) {}
        exec
        (
            p -> () ->
            {
                final int[][] data = DATA.get();
                toIntArray(p,data[0]);
                byte offset = 0;
                for(int s = 0;s < N_BITS - 1;++s)
                    for(int e = s;e <= N_BITS;++e)
                        for(int a = 0;a < e - s + 32;++a)
                        {
                            System.arraycopy(data[0],0,data[1 + offset],0,ARR_SIZE);
                            try {func.shift(data[1 + offset],s,e,a);}
                            catch(final Exception ex) {return new Err(p,s,e,a);}
                            toIntArray(control.shift(p,s,e,a),data[2 + offset]);
                            if(!Arrays.equals(data[1 + offset],data[2 + offset]))
                                return new Err(p,s,e,a);
                            offset ^= 2;
                        }
                return null;
            },
            e ->
            {
                printShift(e.start,e.end,e.amount,parity);
                final int[] data = input(e.data);
                expected(control.shift(e.data,e.start,e.end,e.amount));
                try
                {
                    func.shift(data,e.start,e.end,e.amount);
                    actual(data);
                }
                catch(final Exception ex) {actualException(ex);}
            }
        );
    }
    
    @Test public void lsh_all() {final BigInteger m = mask(0,N_BITS); shift_all(BitSet::lsh,(i,a) -> maskedLsh(i,m,a),true);}
    @Test public void lsh_range() {shift_range(BitSet::lsh,(i,s,e,a) -> maskedLsh(i,mask(s,e),a),true);}
    
    @Test public void rsh_all() {final BigInteger m = mask(0,N_BITS); shift_all(BitSet::rsh,(i,a) -> maskedLsh(i,m,-a),false);}
    @Test public void rsh_range() {shift_range(BitSet::rsh,(i,s,e,a) -> maskedLsh(i,mask(s,e),-a),false);}
    
    @Test
    public void superset()
    {
        record Err(BigInteger a,BigInteger b) {}
        exec2D
        (
            (a,b) -> () ->
            {
                final int[][] data = DATA.get();
                toIntArray(a,data[0]);
                toIntArray(b,data[1]);
                final boolean act;
                try {act = BitSet.superset(data[0],data[1]);}
                catch(final Exception ex) {return new Err(a,b);}
                if(act != a.and(b).equals(b))
                    return new Err(a,b);
                return null;
            },
            e ->
            {
                final int[] a = input("a  ",e.a),
                            b = input("b  ",e.b);
                System.out.printf("exp:%c\n",e.a.and(e.b).equals(e.b)? 't':'f');
                try {System.out.printf("act:%c\n",BitSet.superset(a,b)? 't':'f');}
                catch(final Exception ex) {actualException(ex);}
            }
        );
    }
}