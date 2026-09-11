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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the emulated conditional withdrawal, which is the whole point of this feature: a provider
 * that cannot refuse a withdrawal itself must still not allow a double spend on one server.
 */
class WithdrawIfSufficientTest {

    private static final String REASON = "test";

    @Test
    @DisplayName("concurrent purchases cannot overspend the balance")
    void concurrentPurchasesCannotOverspend() throws Exception {
        FakeProvider provider = new FakeProvider(30);
        UUID player = UUID.randomUUID();
        provider.deposit(player, new BigDecimal("1000"), REASON);

        int threads = 8;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<TransactionResult>> futures = new ArrayList<>();

        try {
            for (int i = 0; i < threads; i++) {
                futures.add(pool.submit(() -> {
                    start.await();
                    return provider.withdrawIfSufficient(player, new BigDecimal("1000"), REASON);
                }));
            }
            start.countDown();

            int successes = 0;
            for (Future<TransactionResult> future : futures) {
                if (future.get(30, TimeUnit.SECONDS).isSuccess()) {
                    successes++;
                }
            }

            assertEquals(1, successes, "exactly one purchase should succeed");
            assertEquals(0, provider.getBalance(player).compareTo(BigDecimal.ZERO), "balance should be exactly zero");
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    @DisplayName("an unsynchronised check then withdraw does overspend, so the test above is meaningful")
    void unsynchronisedPatternOverspends() throws Exception {
        FakeProvider provider = new FakeProvider(30);
        UUID player = UUID.randomUUID();
        provider.deposit(player, new BigDecimal("1000"), REASON);

        int threads = 8;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Boolean>> futures = new ArrayList<>();

        try {
            for (int i = 0; i < threads; i++) {
                futures.add(pool.submit(() -> {
                    start.await();
                    if (provider.getBalance(player).compareTo(new BigDecimal("1000")) >= 0) {
                        provider.withdraw(player, new BigDecimal("1000"), REASON);
                        return true;
                    }
                    return false;
                }));
            }
            start.countDown();

            int successes = 0;
            for (Future<Boolean> future : futures) {
                if (future.get(30, TimeUnit.SECONDS)) {
                    successes++;
                }
            }

            assertTrue(successes > 1, "the unsynchronised pattern is expected to overspend, got " + successes);
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    @DisplayName("insufficient funds never reaches the backend")
    void insufficientFundsNeverDebits() {
        FakeProvider provider = new FakeProvider(0);
        UUID player = UUID.randomUUID();
        provider.deposit(player, new BigDecimal("5"), REASON);

        TransactionResult result = provider.withdrawIfSufficient(player, BigDecimal.TEN, REASON);

        assertSame(TransactionResult.Status.INSUFFICIENT_FUNDS, result.getStatus());
        assertEquals(0, provider.getWithdrawCalls(), "withdraw must not be called when the funds are short");
        assertEquals(0, provider.getBalance(player).compareTo(new BigDecimal("5")), "balance must be untouched");
    }

    @Test
    @DisplayName("a negative amount is refused instead of crediting the player")
    void negativeAmountIsRefused() {
        FakeProvider provider = new FakeProvider(0);
        UUID player = UUID.randomUUID();
        provider.deposit(player, new BigDecimal("100"), REASON);

        TransactionResult result = provider.withdrawIfSufficient(player, new BigDecimal("-50"), REASON);

        assertSame(TransactionResult.Status.FAILED, result.getStatus());
        assertEquals(0, provider.getWithdrawCalls(), "a negative withdrawal must never reach the backend");
        assertEquals(0, provider.getBalance(player).compareTo(new BigDecimal("100")), "balance must be untouched");
    }

    @Test
    @DisplayName("the emulated path reports EMULATED, not a guarantee it cannot make")
    void emulatedPathIsHonest() {
        FakeProvider provider = new FakeProvider(0);
        UUID player = UUID.randomUUID();
        provider.deposit(player, new BigDecimal("100"), REASON);

        TransactionResult result = provider.withdrawIfSufficient(player, BigDecimal.TEN, REASON);

        assertTrue(result.isSuccess());
        assertSame(Guarantee.EMULATED, result.getGuarantee());
        assertSame(Guarantee.EMULATED, provider.getWithdrawGuarantee());
        assertFalse(result.getGuarantee().isCrossServerSafe(), "an emulated result is not cross-server safe");
        assertNotEquals(Guarantee.NATIVE, result.getGuarantee());
    }

    @Test
    @DisplayName("the asynchronous variant completes with a result rather than throwing")
    void asyncCompletesWithResult() throws Exception {
        FakeProvider provider = new FakeProvider(0);
        UUID player = UUID.randomUUID();
        provider.deposit(player, new BigDecimal("100"), REASON);

        TransactionResult result = provider.withdrawIfSufficientAsync(player, new BigDecimal("40"), REASON)
                .get(30, TimeUnit.SECONDS);

        assertTrue(result.isSuccess());
        assertEquals(0, provider.getBalance(player).compareTo(new BigDecimal("60")));
    }
}
