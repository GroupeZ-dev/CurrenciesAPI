package fr.traqueur.currencies;

import org.bukkit.Bukkit;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Interface used to interact with a currency provider.
 *
 * @author Traqueur
 */
public interface CurrencyProvider {

    /**
     * Deposit a certain amount of currency to a player.
     *
     * @param playerId The UUID of the player to deposit the money.
     * @param amount   The amount of currency to deposit.
     * @param reason   The reason of the deposit.
     */
    void deposit(UUID playerId, BigDecimal amount, String reason);

    /**
     * Withdraw a certain amount of currency from a player.
     *
     * @param playerId The UUID of the player to withdraw the money.
     * @param amount   The amount of currency to withdraw.
     * @param reason   The reason of the withdrawal.
     */
    void withdraw(UUID playerId, BigDecimal amount, String reason);

    /**
     * Get the balance of a player.
     *
     * @param playerId The UUID of the player to get the balance.
     * @return The balance of the player.
     */
    BigDecimal getBalance(UUID playerId);

    /**
     * Whether this provider can check the balance and apply the debit as one indivisible
     * operation.
     *
     * <p>When this returns false, {@link #withdrawIfSufficient} still works but is emulated by
     * the library: the sequence is serialized inside this JVM, which stops one server racing
     * itself, but it cannot stop a second server acting on the same shared economy.</p>
     *
     * @return True when the backend itself guarantees the operation.
     */
    default boolean hasNativeConditionalWithdraw() {
        return false;
    }

    /**
     * Whether this provider must be used from the main server thread.
     *
     * <p><b>This defaults to true on purpose.</b> Most Bukkit plugin APIs are not thread safe,
     * and a provider that reads or writes live player state, such as inventory contents or
     * experience levels, will corrupt that state if it is touched from another thread. Assuming
     * the unsafe case by default means an unknown third party provider is never called off the
     * main thread by accident.</p>
     *
     * <p>Override this to return false only when the backend is documented as safe for
     * concurrent access, for example one that talks to Redis or to its own thread safe storage.
     * Doing so lets {@link #withdrawIfSufficientAsync} keep the work off the main thread.</p>
     *
     * @return True when every call has to happen on the main server thread.
     */
    default boolean requiresMainThread() {
        return true;
    }

    /**
     * Debit a player, but only if the funds are actually available.
     *
     * <p>This is the operation to use for a purchase. Unlike a balance check followed by a
     * separate {@link #withdraw}, nothing can slip between the two steps.</p>
     *
     * <p>The default implementation emulates the operation by reading the balance and then
     * withdrawing, with the whole sequence held under a lock so that two threads cannot both
     * observe the same balance and both debit. The returned result reports
     * {@link TransactionResult#isBackendGuaranteed()} as false to make that limitation visible.</p>
     *
     * @param playerId The UUID of the player to debit.
     * @param amount   The amount to debit, must be strictly positive.
     * @param reason   The reason of the withdrawal.
     * @return The outcome. Nothing is debited unless the status is
     * {@link TransactionResult.Status#SUCCESS}.
     */
    default TransactionResult withdrawIfSufficient(UUID playerId, BigDecimal amount, String reason) {
        TransactionResult invalid = CurrencyArgumentChecks.findProblem(playerId, amount);
        if (invalid != null) {
            return invalid;
        }

        ReentrantLock lock = CurrencyLocks.lockFor(this, playerId);
        if (!CurrencyLocks.tryLock(lock)) {
            return TransactionResult.failed(amount, "Timed out waiting for a concurrent operation on the same balance.");
        }

        try {
            BigDecimal balance = this.getBalance(playerId);
            if (balance == null) {
                balance = BigDecimal.ZERO;
            }

            if (balance.compareTo(amount) < 0) {
                return TransactionResult.emulatedInsufficientFunds(amount, balance);
            }

            this.withdraw(playerId, amount, reason);
            return TransactionResult.emulatedSuccess(amount, balance.subtract(amount));
        } catch (Exception exception) {
            return TransactionResult.failed(amount, "The backend threw while withdrawing: " + exception.getMessage());
        } finally {
            lock.unlock();
        }
    }

    /**
     * Asynchronous variant of {@link #withdrawIfSufficient}.
     *
     * <p>For a provider that must run on the main server thread, the work is scheduled back onto
     * it, which requires {@link CurrenciesAPI#init(org.bukkit.plugin.Plugin)} to have been called.
     * Without it the returned result is a failure rather than an unsafe off thread call.</p>
     *
     * @param playerId The UUID of the player to debit.
     * @param amount   The amount to debit, must be strictly positive.
     * @param reason   The reason of the withdrawal.
     * @return A future completed with the outcome. The future itself never completes
     * exceptionally, failures are reported through the result.
     */
    default CompletableFuture<TransactionResult> withdrawIfSufficientAsync(UUID playerId, BigDecimal amount, String reason) {
        TransactionResult invalid = CurrencyArgumentChecks.findProblem(playerId, amount);
        if (invalid != null) {
            return CompletableFuture.completedFuture(invalid);
        }

        if (!this.requiresMainThread()) {
            return CompletableFuture.supplyAsync(() -> CurrencyProvider.this.withdrawIfSufficient(playerId, amount, reason));
        }

        if (CurrenciesAPI.isMainThread()) {
            return CompletableFuture.completedFuture(this.withdrawIfSufficient(playerId, amount, reason));
        }

        if (CurrenciesAPI.getPlugin() == null) {
            return CompletableFuture.completedFuture(TransactionResult.failed(amount,
                    "This currency must be used on the main server thread. Call CurrenciesAPI.init(plugin) "
                            + "to enable asynchronous access to it."));
        }

        final CompletableFuture<TransactionResult> future = new CompletableFuture<TransactionResult>();
        Bukkit.getScheduler().runTask(CurrenciesAPI.getPlugin(), () -> {
            try {
                future.complete(CurrencyProvider.this.withdrawIfSufficient(playerId, amount, reason));
            } catch (Exception exception) {
                future.complete(TransactionResult.failed(amount, "The backend threw while withdrawing: " + exception.getMessage()));
            }
        });
        return future;
    }
}
