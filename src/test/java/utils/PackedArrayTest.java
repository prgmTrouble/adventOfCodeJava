package utils;

import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static testutil.TestUtils.parallelRun;

public class PackedArrayTest
{
    @Test
    public void set()
    {
        record Err(byte bits,long[] seq,int idx) {}
        @SuppressWarnings("unchecked") final Callable<Err>[] tasks = (Callable<Err>[])new Callable[64];
        for(byte bits = 1;bits <= 64;++bits)
        {
            final byte b = bits;
            tasks[bits - 1] = () ->
            {
                final SecureRandom rand = new SecureRandom();
                final long[] exp = new long[64],
                             act = PackedArray.create(b,64),
                             seq = new long[256];
                for(byte s = 0;s < 4;++s)
                    for(byte i = 0;i < 64;++i)
                    {
                        final long r = rand.nextLong();
                        seq[s * i] = r;
                        exp[i] = r & ((1L << b) - 1);
                        PackedArray.set(b,i,act,r);
                        if
                        (
                            (i > 0 && PackedArray.get(b,i - 1,act) != exp[i - 1]) |
                            (i < 63 && PackedArray.get(b,i + 1,act) != exp[i + 1]) |
                            PackedArray.get(b,i,act) != exp[i]
                        )
                            return new Err(b,seq,s * i);
                    }
                return null;
            };
        }
        assertFalse
        (
            parallelRun
            (
                List.of(tasks),
                err ->
                {
                    final String format = String.format(" %%0%dx",(err.bits + 3) / 4);
                    System.out.printf("bits=%d\nidx:%s\nexp:",err.bits," ".repeat(((err.bits + 3) / 4 + 1) * err.idx) + 'v');
                    
                    final long[] exp = new long[64],
                                 act = PackedArray.create(err.bits,64);
                    for(int i = 0;i < err.idx;++i)
                    {
                        exp[i / 4] = err.seq[i] & ((1L << err.bits) - 1);
                        PackedArray.set(err.bits,i,act,i / 4);
                    }
                    
                    for(final long l : exp)
                        System.out.printf(format,l);
                    System.out.print("\nact:");
                    for(byte i = 0;i < 64;++i)
                    {
                        try {System.out.printf(format,PackedArray.get(err.bits,i,act));}
                        catch(final Exception e)
                        {
                            System.out.println();
                            e.printStackTrace();
                            break;
                        }
                    }
                    System.out.println();
                }
            )
        );
    }
    
    @Test
    public void fill()
    {
        record Err(byte bits,byte start,byte end,long[] init,Exception e) {}
        @SuppressWarnings("unchecked") final Callable<Err>[] tasks = (Callable<Err>[])new Callable[64 * ((65 * (65 + 1)) / 2)];
        int t = 0;
        for(byte bits = 1;bits <= 64;++bits)
        {
            final byte b = bits;
            for(byte start = 0;start <= 64;++start)
            {
                final byte s = start;
                for(byte end = start;end <= 64;++end)
                {
                    final byte e = end;
                    tasks[t++] = () ->
                    {
                        final SecureRandom rand = new SecureRandom();
                        final long[] exp = new long[64],
                                     act = PackedArray.create(b,64),
                                     init = new long[64];
                        for(byte retry = 0;retry < 64;++retry)
                        {
                            for(byte i = 0;i < 64;++i)
                            {
                                final long r = rand.nextLong();
                                init[i] = r;
                                exp[i] = r & ((1L << b) - 1);
                            }
                            final long r = rand.nextLong();
                            for(byte i = 0;i < 64;++i)
                                PackedArray.set(b,i,act,init[i]);
                            Arrays.fill(exp,s,e,r & ((1L << b) - 1));
                            try
                            {
                                PackedArray.fill(b,s,e,act,r);
                                for(byte i = 0;i < 64;++i)
                                    if(exp[i] != PackedArray.get(b,i,act))
                                        return new Err(b,s,e,init,null);
                            }
                            catch(final Exception err) {return new Err(b,s,e,init,err);}
                        }
                        return null;
                    };
                }
            }
        }
        assertFalse
        (
            parallelRun
            (
                List.of(tasks),
                err ->
                {
                    final int hexits = (err.bits + 3) / 4;
                    final String format = String.format(" %%0%dx",hexits);
                    final char[] range = new char[(hexits + 1) * 64 + 1];
                    Arrays.fill(range,' ');
                    range[(hexits + 1) * err.start] = 's';
                    range[(hexits + 1) * err.end] = 'e';
                    if(err.start < err.end)
                        Arrays.fill(range,(hexits + 1) * err.start + 1,(hexits + 1) * err.end,'-');
                    
                    final long[] exp = new long[64],
                                 act = PackedArray.create(err.bits,64);
                    for(byte i = 0;i < 64;++i)
                    {
                        exp[i] = err.init[i] & ((1L << err.bits) - 1);
                        PackedArray.set(err.bits,i,act,err.init[i]);
                    }
                    
                    System.out.printf("bits=%d\nidx:%s\nexp:",err.bits,new String(range));
                    for(final long l : exp)
                        System.out.printf(format,l);
                    System.out.print("\nact:");
                    if(err.e == null)
                        for(byte i = 0;i < 64;++i)
                            System.out.printf(format,PackedArray.get(err.bits,i,act));
                    else
                        err.e.printStackTrace();
                    System.out.print("\nini:");
                    for(final long i : err.init)
                        System.out.printf(" %016x",i);
                    System.out.println();
                }
            )
        );
    }
}