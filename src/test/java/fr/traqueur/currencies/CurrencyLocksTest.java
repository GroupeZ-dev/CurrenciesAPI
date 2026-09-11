package fr.traqueur.currencies;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The lock is keyed per provider and player, so it has two properties worth pinning down: unrelated
 * players must never wait on each other, and the entries must not pile up over time.
 */
class CurrencyLocksTest {

    private static final String REASON = "test";

    @Test
    @DisplayName("two different players do not block each other")
    void differentPlayersDoNotBlockEachOther() throws Exception {
        FakeProvider provider = new FakeProvider(0);
        UUID first = UUID.randomUUID();

        CurrencyLocks.Handle held = CurrencyLocks.tryAcquire(provider, first);
        assertNotNull(held, "the first acquisition should succeed");

        try {
            UUID second = UUID.randomUUID();
            CurrencyLocks.Handle other = CurrencyLocks.tryAcquire(provider, second);
            assertNotNull(other, "an unrelated player must not be blocked");
            other.release();
        } finally {
            held.release();
        }
    }

    @Test
    @DisplayName("the same player is serialized, and a timeout means a real conflict")
    void samePlayerIsSerialized() throws Exception {
        FakeProvider provider = new FakeProvider(0);
        UUID player = UUID.randomUUID();

        CurrencyLocks.Handle held = CurrencyLocks.tryAcquire(provider, player);
        assertNotNull(held);

        try {
            ExecutorService pool = Executors.newSingleThreadExecutor();
            try {
                Future<CurrencyLocks.Handle> attempt = pool.submit(() -> CurrencyLocks.tryAcquire(provider, player));
                assertNull(attempt.get(30, TimeUnit.SECONDS), "the same balance must be serialized");
            } finally {
                pool.shutdownNow();
            }
        } finally {
            held.release();
        }
    }

    @Test
    @DisplayName("lock entries are released, so the map does not grow with the number of players")
    void lockEntriesDoNotAccumulate() throws Exception {
        FakeProvider provider = new FakeProvider(0);

        int before = CurrencyLocks.activeLockCount();

        for (int i = 0; i < 2000; i++) {
            UUID player = UUID.randomUUID();
            provider.deposit(player, BigDecimal.TEN, REASON);
            provider.withdrawIfSufficient(player, BigDecimal.ONE, REASON);
        }

        assertEquals(before, CurrencyLocks.activeLockCount(),
                "entries must be removed once no thread holds or waits for them");
    }

    @Test
    @DisplayName("entries are also released when the acquisition times out")
    void timedOutAcquisitionReleasesItsEntry() throws Exception {
        FakeProvider provider = new FakeProvider(0);
        UUID player = UUID.randomUUID();

        int before = CurrencyLocks.activeLockCount();

        CurrencyLocks.Handle held = CurrencyLocks.tryAcquire(provider, player);
        assertNotNull(held);

        ExecutorService pool = Executors.newSingleThreadExecutor();
        try {
            assertNull(pool.submit(() -> CurrencyLocks.tryAcquire(provider, player)).get(30, TimeUnit.SECONDS));
        } finally {
            pool.shutdownNow();
        }

        held.release();

        assertEquals(before, CurrencyLocks.activeLockCount(), "a timed out attempt must not leak an entry");
    }

    @Test
    @DisplayName("concurrent traffic across many players leaves no entries behind")
    void concurrentTrafficLeavesNoEntries() throws Exception {
        FakeProvider provider = new FakeProvider(1);
        int before = CurrencyLocks.activeLockCount();

        int threads = 8;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<?>> futures = new ArrayList<>();

        try {
            for (int i = 0; i < threads; i++) {
                futures.add(pool.submit(() -> {
                    start.await();
                    for (int n = 0; n < 100; n++) {
                        UUID player = UUID.randomUUID();
                        provider.deposit(player, BigDecimal.TEN, REASON);
                        provider.withdrawIfSufficient(player, BigDecimal.ONE, REASON);
                    }
                    return null;
                }));
            }
            start.countDown();
            for (Future<?> future : futures) {
                future.get(60, TimeUnit.SECONDS);
            }
        } finally {
            pool.shutdownNow();
        }

        assertTrue(CurrencyLocks.activeLockCount() <= before,
                "expected no leftover entries, got " + CurrencyLocks.activeLockCount());
    }
}
