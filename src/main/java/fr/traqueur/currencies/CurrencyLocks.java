package fr.traqueur.currencies;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

final class CurrencyLocks {

    private static final int STRIPES = 1024;
    private static final long LOCK_TIMEOUT_MILLIS = 250L;
    private static final long MAIN_THREAD_LOCK_TIMEOUT_MILLIS = 25L;
    private static final ReentrantLock[] LOCKS = new ReentrantLock[STRIPES];

    static {
        for (int i = 0; i < STRIPES; i++) {
            LOCKS[i] = new ReentrantLock();
        }
    }

    private CurrencyLocks() {
    }

    /**
     * Resolve the lock guarding a given provider and player pair.
     *
     * @param provider The provider performing the operation.
     * @param playerId The player being debited.
     * @return The lock to use.
     */
    static ReentrantLock lockFor(CurrencyProvider provider, UUID playerId) {
        int hash = System.identityHashCode(provider) * 31 + (playerId == null ? 0 : playerId.hashCode());
        hash ^= (hash >>> 16);
        return LOCKS[hash & (STRIPES - 1)];
    }

    /**
     * Try to acquire a lock within the configured timeout.
     *
     * @param lock The lock to acquire.
     * @return True when the lock was acquired and must be released by the caller.
     */
    static boolean tryLock(ReentrantLock lock) {
        long timeout = CurrenciesAPI.isMainThread() ? MAIN_THREAD_LOCK_TIMEOUT_MILLIS : LOCK_TIMEOUT_MILLIS;
        try {
            return lock.tryLock(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
