package fr.traqueur.currencies;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The thread pool used by the asynchronous currency operations.
 *
 * <p>Deliberately not the common {@link java.util.concurrent.ForkJoinPool}, which is what
 * {@code CompletableFuture.supplyAsync} uses when no executor is given. That pool is shared with
 * everything else in the JVM and sized for CPU bound work, so on a machine with few cores its
 * parallelism can be as low as one. A withdrawal waiting on a SQL or Redis round trip would then
 * block the whole queue, including work that has nothing to do with currencies.</p>
 *
 * <p>The threads are daemons, so they never hold the server open on shutdown, and they are named so
 * that a thread dump from a user reporting lag is actually readable.</p>
 */
final class CurrencyExecutor {
    private static final int THREADS = Math.max(2, Math.min(8, Runtime.getRuntime().availableProcessors()));

    private static final Executor EXECUTOR = Executors.newFixedThreadPool(THREADS, new ThreadFactory() {

        private final AtomicInteger counter = new AtomicInteger();

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, "CurrenciesAPI-async-" + this.counter.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        }
    });

    private CurrencyExecutor() {
    }

    /**
     * @return The executor for asynchronous currency operations.
     */
    static Executor get() {
        return EXECUTOR;
    }
}
