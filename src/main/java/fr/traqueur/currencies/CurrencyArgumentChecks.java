package fr.traqueur.currencies;

import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Argument checks shared by every conditional currency operation.
 *
 * <p>Custom providers that override
 * {@link CurrencyProvider#withdrawIfSufficient(UUID, BigDecimal, String)} should call
 * {@link #findProblem(UUID, BigDecimal)} first, so that every implementation rejects the same
 * nonsensical inputs in the same way.</p>
 */
public final class CurrencyArgumentChecks {

    private CurrencyArgumentChecks() {
    }

    /**
     * Looks for a problem with the arguments to a conditional currency operation.
     *
     * @param playerId The player.
     * @param amount   The requested amount.
     * @return A result describing the problem, or null when the arguments are usable.
     */
    @Nullable
    public static TransactionResult findProblem(@Nullable UUID playerId, @Nullable BigDecimal amount) {
        if (playerId == null) {
            return TransactionResult.failed(amount, "The player UUID cannot be null.");
        }
        if (amount == null) {
            return TransactionResult.failed(BigDecimal.ZERO, "The amount cannot be null.");
        }
        if (amount.signum() <= 0) {
            return TransactionResult.failed(amount, "The amount must be strictly positive, was " + amount + ".");
        }
        return null;
    }
}
