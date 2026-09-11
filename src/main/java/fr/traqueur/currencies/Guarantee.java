package fr.traqueur.currencies;

/**
 * How strong the promise behind a conditional currency operation actually is.
 *
 * <p>A two state boolean was not enough to describe the backends honestly. Some of them validate
 * the funds inside their own shared storage, some merely report an outcome without promising the
 * check and the debit were indivisible, and for the rest this library has to emulate the operation.
 * Collapsing the middle case into "guaranteed" told callers they were safe against a cross server
 * double spend when they were not.</p>
 */
public enum Guarantee {

    /**
     * The backend applied the check and the debit as one indivisible operation, inside storage that
     * every server shares. This is the only level that is safe against a second server acting on
     * the same balance at the same time.
     */
    NATIVE,

    /**
     * The backend reported whether the withdrawal succeeded, but does not promise that the check
     * and the debit were indivisible.
     *
     * <p>Vault is the clearest example: {@code withdrawPlayer} returns a response, but Vault is an
     * abstraction over whichever economy plugin is installed, and most of them do a plain read,
     * modify and write. The result is trustworthy for a single request, and it is better than an
     * emulated check because the decision was made by the thing that owns the money, but it is not
     * a cross server guarantee.</p>
     */
    DELEGATED,

    /**
     * This library performed the check and the debit itself, serialized under a lock.
     *
     * <p>Protects one server against racing itself. A second server sharing the same economy does
     * not see that lock.</p>
     */
    EMULATED;

    /**
     * Whether this level is safe when several servers share one economy.
     *
     * @return True only for {@link #NATIVE}.
     */
    public boolean isCrossServerSafe() {
        return this == NATIVE;
    }
}
