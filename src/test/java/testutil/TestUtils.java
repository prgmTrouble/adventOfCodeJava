package testutil;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import static java.util.concurrent.Executors.newFixedThreadPool;

public final class TestUtils
{
    private TestUtils() {}
    
    public static final int PROCESSORS = Runtime.getRuntime().availableProcessors();
    
    public static <T> boolean parallelRun(final List<Callable<T>> tasks,final Consumer<T> output)
    {
        try(final ExecutorService pool = newFixedThreadPool(PROCESSORS))
        {
            final List<Future<T>> errors = pool.invokeAll(tasks);
            pool.shutdown();
            boolean hasError = false;
            for(final Future<T> err : errors)
                try
                {
                    final T e = err.get();
                    if(e != null)
                    {
                        hasError = true;
                        output.accept(e);
                    }
                }
                catch(final ExecutionException e)
                {
                    hasError = true;
                    e.printStackTrace();
                }
            return hasError;
        }
        catch(final InterruptedException e) {throw new RuntimeException(e);}
    }
}