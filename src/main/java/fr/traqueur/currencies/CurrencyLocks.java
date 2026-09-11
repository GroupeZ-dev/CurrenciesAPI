package fr.traqueur.currencies;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * One lock per provider and player pair, used to serialize emulated conditional operations.
 *
 * <p>Backends that cannot perform an atomic "debit only if the funds are there" have to be emulated
 * as a balance read followed by a withdraw. Two threads running that sequence at the same time for
 * the same player can both observe the same balance and both debit, which is the classic double
 * spend. Holding a lock for the duration of the sequence removes that window.</p>
 *
 * <p>The lock is keyed on the exact pair being operated on, so two unrelated players never wait on
 * each other and a timeout genuinely means there was a competing operation on that same balance.
 * An earlier version used a fixed set of striped locks, which was cheap but let independent players
 * collide, producing a failed purchase whose message claimed a conflict that had not happened.</p>
 *
 * <p>The map does not grow without bound despite having no fixed size: each entry is reference
 * counted and removed as soon as the last holder or waiter is done with it, so its size tracks the
 * number of operations currently in flight rather than the number of players ever seen.</p>
 *
 * <p><b>This only protects against concurrency inside this JVM.</b> When several servers share one
 * economy database, a lock held here is invisible to the other servers. That limitation is why an
 * emulated result reports {@link Guarantee#EMULATED}.</p>
 */
final class CurrencyLocks {

    private static final long LOCK_TIMEOUT_MILLIS = 250L;
    private static final long MAIN_THREAD_LOCK_TIMEOUT_MILLIS = 25L;

    private static final Map<Key, CountedLock> LOCKS = new ConcurrentHashMap<>();

    private CurrencyLocks() {
    }

    /**
     * Takes the lock guarding one provider and player pair.
     *
     * @param provider The provider performing the operation.
     * @param playerId The player being debited.
     * @return A handle that must be released in a finally block, or null when the lock could not be
     * taken in time. Null means no operation was performed.
     */
    static Handle tryAcquire(CurrencyProvider provider, UUID playerId) {
        Key key = new Key(System.identityHashCode(provider), playerId);

        CountedLock entry = LOCKS.compute(key, (k, existing) -> {
            CountedLock counted = existing == null ? new CountedLock() : existing;
            counted.references++;
            return counted;
        });

        long timeout = CurrenciesAPI.isMainThread() ? MAIN_THREAD_LOCK_TIMEOUT_MILLIS : LOCK_TIMEOUT_MILLIS;
        boolean acquired;
        try {
            acquired = entry.lock.tryLock(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            acquired = false;
        }

        if (!acquired) {
            release(key);
            return null;
        }

        return new Handle(key, entry);
    }

    /**
     * Drops one reference to an entry, removing it once nobody is using it any more.
     */
    private static void release(Key key) {
        LOCKS.compute(key, (k, existing) -> {
            if (existing == null) {
                return null;
            }
            existing.references--;
            return existing.references <= 0 ? null : existing;
        });
    }

    /**
     * Number of live lock entries. For tests, to prove entries do not accumulate.
     */
    static int activeLockCount() {
        return LOCKS.size();
    }

    /**
     * A held lock. Release it in a finally block.
     */
    static final class Handle {

        private final Key key;
        private final CountedLock entry;

        private Handle(Key key, CountedLock entry) {
            this.key = key;
            this.entry = entry;
        }

        void release() {
            this.entry.lock.unlock();
            CurrencyLocks.release(this.key);
        }
    }

    private static final class CountedLock {

        private final ReentrantLock lock = new ReentrantLock();

        private int references;
    }

    /**
     * Identifies one provider and player pair.
     *
     * <p>The provider is identified by its identity hash rather than by equality, because two
     * distinct provider instances for the same economy are genuinely separate paths to the same
     * money only by coincidence, and providers do not define equals.</p>
     */
    private static final class Key {

        private final int providerIdentity;
        private final UUID playerId;

        private Key(int providerIdentity, UUID playerId) {
            this.providerIdentity = providerIdentity;
            this.playerId = playerId;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof Key)) {
                return false;
            }
            Key key = (Key) other;
            return this.providerIdentity == key.providerIdentity
                    && (Objects.equals(this.playerId, key.playerId));
        }

        @Override
        public int hashCode() {
            return this.providerIdentity * 31 + (this.playerId == null ? 0 : this.playerId.hashCode());
        }
    }
}
