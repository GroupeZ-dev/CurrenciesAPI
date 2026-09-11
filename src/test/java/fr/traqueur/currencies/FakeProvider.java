package fr.traqueur.currencies;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * An in-memory provider used to exercise the emulated {@link CurrencyProvider#withdrawIfSufficient}
 * without a server.
 *
 * <p>{@link #withdraw} deliberately reads the balance, pauses, then writes the result back. That is
 * how a real backend behaves when the write goes over a network, and it is what makes an
 * unsynchronized check-then-withdraw lose updates. Without the pause the race almost never shows up
 * and the test would pass for the wrong reason.</p>
 */
final class FakeProvider implements CurrencyProvider {

    private final Map<UUID, BigDecimal> balances = new ConcurrentHashMap<>();
    private final AtomicInteger withdrawCalls = new AtomicInteger();
    private final long writeDelayMillis;

    FakeProvider(long writeDelayMillis) {
        this.writeDelayMillis = writeDelayMillis;
    }

    @Override
    public void deposit(UUID playerId, BigDecimal amount, String reason) {
        this.balances.merge(playerId, amount, BigDecimal::add);
    }

    @Override
    public void withdraw(UUID playerId, BigDecimal amount, String reason) {
        this.withdrawCalls.incrementAndGet();
        BigDecimal current = this.getBalance(playerId);
        if (this.writeDelayMillis > 0) {
            try {
                Thread.sleep(this.writeDelayMillis);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
        }
        this.balances.put(playerId, current.subtract(amount));
    }

    @Override
    public BigDecimal getBalance(UUID playerId) {
        BigDecimal balance = this.balances.get(playerId);
        return balance == null ? BigDecimal.ZERO : balance;
    }

    /**
     * False so the tests exercise the real asynchronous path instead of the main thread hop, which
     * would need a running server.
     */
    @Override
    public boolean requiresMainThread() {
        return false;
    }

    int getWithdrawCalls() {
        return this.withdrawCalls.get();
    }
}
