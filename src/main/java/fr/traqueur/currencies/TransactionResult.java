package fr.traqueur.currencies;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;

public final class TransactionResult {

    public enum Status {

        /**
         * The funds were available and the debit was applied.
         */
        SUCCESS,

        /**
         * The player did not have enough funds. Nothing was debited.
         */
        INSUFFICIENT_FUNDS,

        /**
         * The backend does not implement this operation at all. Nothing was debited.
         * Callers must decide for themselves whether to fall back to a non-atomic
         * balance check followed by a plain withdraw, or to refuse the operation.
         */
        UNSUPPORTED,

        /**
         * The operation could not be completed for any other reason, for example the economy
         * plugin returned an error or the player data could not be loaded.
         *
         * <p>Nothing was debited in the ordinary case. The one exception worth knowing about is a
         * backend that throws <i>after</i> it has already applied the withdrawal, for instance a
         * committed transaction followed by an error on the way back. The library cannot tell that
         * apart from a clean failure, so a caller handing out something valuable should treat a
         * FAILED result as "no goods, and worth logging" rather than as proof the money is
         * untouched.</p>
         */
        FAILED
    }

    private final Status status;
    private final BigDecimal amount;
    private final BigDecimal balance;
    private final String errorMessage;
    private final Guarantee guarantee;

    private TransactionResult(Status status, BigDecimal amount, BigDecimal balance, String errorMessage, Guarantee guarantee) {
        this.status = status;
        this.amount = amount == null ? BigDecimal.ZERO : amount;
        this.balance = balance;
        this.errorMessage = errorMessage;
        this.guarantee = guarantee;
    }

    /**
     * Build a successful result.
     *
     * @param amount    The amount that was debited.
     * @param balance   The resulting balance, or null when it is not known.
     * @param guarantee How strong the promise behind the operation is.
     * @return The result.
     */
    public static TransactionResult success(BigDecimal amount, BigDecimal balance, Guarantee guarantee) {
        return new TransactionResult(Status.SUCCESS, amount, balance, null, guarantee);
    }

    /**
     * Build a result for a player who could not afford the amount. Nothing was debited.
     *
     * @param amount    The amount that was requested.
     * @param balance   The balance that was observed, or null when it is not known.
     * @param guarantee How strong the promise behind the check is.
     * @return The result.
     */
    public static TransactionResult insufficientFunds(BigDecimal amount, BigDecimal balance, Guarantee guarantee) {
        return new TransactionResult(Status.INSUFFICIENT_FUNDS, amount, balance, null, guarantee);
    }

    /**
     * Build a result for a backend that cannot perform this operation. Nothing was debited.
     *
     * @param amount       The amount that was requested.
     * @param errorMessage A human readable explanation.
     * @return The result.
     */
    public static TransactionResult unsupported(BigDecimal amount, String errorMessage) {
        return new TransactionResult(Status.UNSUPPORTED, amount, null, errorMessage, Guarantee.EMULATED);
    }

    /**
     * Build a failed result. Nothing was debited.
     *
     * @param amount       The amount that was requested.
     * @param errorMessage A human readable explanation.
     * @return The result.
     */
    public static TransactionResult failed(BigDecimal amount, String errorMessage) {
        return new TransactionResult(Status.FAILED, amount, null, errorMessage, Guarantee.EMULATED);
    }

    /**
     * @return The outcome of the operation.
     */
    @NotNull
    public Status getStatus() {
        return this.status;
    }

    /**
     * @return True only when the funds were actually debited.
     */
    public boolean isSuccess() {
        return this.status == Status.SUCCESS;
    }

    /**
     * @return How strong the promise behind this operation was. Use
     * {@link Guarantee#isCrossServerSafe()} to decide whether it holds on a network where several
     * servers share one economy.
     */
    @NotNull
    public Guarantee getGuarantee() {
        return this.guarantee;
    }

    /**
     * @return The amount that was requested.
     */
    @NotNull
    public BigDecimal getAmount() {
        return this.amount;
    }

    /**
     * @return The resulting balance, or null when the backend does not report one.
     */
    @Nullable
    public BigDecimal getBalance() {
        return this.balance;
    }

    /**
     * @return A human-readable explanation for a failure, or null.
     */
    @Nullable
    public String getErrorMessage() {
        return this.errorMessage;
    }

    @Override
    public String toString() {
        return "TransactionResult{status=" + this.status
                + ", amount=" + this.amount
                + ", balance=" + this.balance
                + ", guarantee=" + this.guarantee
                + (this.errorMessage == null ? "" : ", error='" + this.errorMessage + "'")
                + '}';
    }
}
