package fr.traqueur.currencies;

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
         * The operation could not be completed for any other reason, for example the
         * economy plugin returned an error or the player data could not be loaded.
         * Nothing was debited.
         */
        FAILED
    }

    private final Status status;
    private final BigDecimal amount;
    private final BigDecimal balance;
    private final String errorMessage;
    private final boolean backendGuaranteed;

    private TransactionResult(Status status, BigDecimal amount, BigDecimal balance, String errorMessage, boolean backendGuaranteed) {
        this.status = status;
        this.amount = amount == null ? BigDecimal.ZERO : amount;
        this.balance = balance;
        this.errorMessage = errorMessage;
        this.backendGuaranteed = backendGuaranteed;
    }

    /**
     * Build a successful result for a backend that applied the check and the debit itself.
     *
     * <p>Use this from a provider that overrides
     * {@link CurrencyProvider#withdrawIfSufficient(java.util.UUID, BigDecimal, String)} because its
     * backend can refuse a withdrawal on its own.</p>
     *
     * @param amount  The amount that was debited.
     * @param balance The resulting balance, or null when the backend does not report it.
     * @return The result.
     */
    public static TransactionResult nativeSuccess(BigDecimal amount, BigDecimal balance) {
        return new TransactionResult(Status.SUCCESS, amount, balance, null, true);
    }

    /**
     * Build a successful result for an operation the library emulated with a balance read followed
     * by a withdraw.
     *
     * @param amount  The amount that was debited.
     * @param balance The resulting balance, or null when it is not known.
     * @return The result.
     */
    public static TransactionResult emulatedSuccess(BigDecimal amount, BigDecimal balance) {
        return new TransactionResult(Status.SUCCESS, amount, balance, null, false);
    }

    /**
     * Build an insufficient funds result for a backend that made the decision itself. Nothing was
     * debited.
     *
     * @param amount  The amount that was requested.
     * @param balance The balance that was observed, or null when it is not known.
     * @return The result.
     */
    public static TransactionResult nativeInsufficientFunds(BigDecimal amount, BigDecimal balance) {
        return new TransactionResult(Status.INSUFFICIENT_FUNDS, amount, balance, null, true);
    }

    /**
     * Build an insufficient funds result for a check the library performed itself. Nothing was
     * debited.
     *
     * @param amount  The amount that was requested.
     * @param balance The balance that was observed, or null when it is not known.
     * @return The result.
     */
    public static TransactionResult emulatedInsufficientFunds(BigDecimal amount, BigDecimal balance) {
        return new TransactionResult(Status.INSUFFICIENT_FUNDS, amount, balance, null, false);
    }

    /**
     * Build a result for a backend that cannot perform this operation. Nothing was debited.
     *
     * @param amount       The amount that was requested.
     * @param errorMessage A human readable explanation.
     * @return The result.
     */
    public static TransactionResult unsupported(BigDecimal amount, String errorMessage) {
        return new TransactionResult(Status.UNSUPPORTED, amount, null, errorMessage, false);
    }

    /**
     * Build a failed result. Nothing was debited.
     *
     * @param amount       The amount that was requested.
     * @param errorMessage A human readable explanation.
     * @return The result.
     */
    public static TransactionResult failed(BigDecimal amount, String errorMessage) {
        return new TransactionResult(Status.FAILED, amount, null, errorMessage, false);
    }

    /**
     * @return The outcome of the operation.
     */
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
     * @return True when the backend guaranteed that the check and the debit were indivisible.
     * False means the library emulated the operation and it is only safe against concurrent
     * access from inside this server.
     */
    public boolean isBackendGuaranteed() {
        return this.backendGuaranteed;
    }

    /**
     * @return The amount that was requested.
     */
    public BigDecimal getAmount() {
        return this.amount;
    }

    /**
     * @return The resulting balance, or null when the backend does not report one.
     */
    public BigDecimal getBalance() {
        return this.balance;
    }

    /**
     * @return A human-readable explanation for a failure, or null.
     */
    public String getErrorMessage() {
        return this.errorMessage;
    }

    @Override
    public String toString() {
        return "TransactionResult{status=" + this.status
                + ", amount=" + this.amount
                + ", balance=" + this.balance
                + ", backendGuaranteed=" + this.backendGuaranteed
                + (this.errorMessage == null ? "" : ", error='" + this.errorMessage + "'")
                + '}';
    }
}
