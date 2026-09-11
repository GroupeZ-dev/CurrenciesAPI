package fr.traqueur.currencies;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionResultTest {

    @Test
    @DisplayName("success is the only status that reports isSuccess")
    void onlySuccessIsSuccessful() {
        assertTrue(TransactionResult.success(BigDecimal.TEN, BigDecimal.ONE, Guarantee.NATIVE).isSuccess());
        assertFalse(TransactionResult.insufficientFunds(BigDecimal.TEN, BigDecimal.ONE, Guarantee.NATIVE).isSuccess());
        assertFalse(TransactionResult.unsupported(BigDecimal.TEN, "nope").isSuccess());
        assertFalse(TransactionResult.failed(BigDecimal.TEN, "boom").isSuccess());
    }

    @Test
    @DisplayName("the guarantee is carried through unchanged")
    void guaranteeIsCarriedThrough() {
        for (Guarantee guarantee : Guarantee.values()) {
            assertSame(guarantee, TransactionResult.success(BigDecimal.TEN, null, guarantee).getGuarantee());
            assertSame(guarantee, TransactionResult.insufficientFunds(BigDecimal.TEN, null, guarantee).getGuarantee());
        }
    }

    @Test
    @DisplayName("only NATIVE is cross-server safe")
    void onlyNativeIsCrossServerSafe() {
        assertTrue(Guarantee.NATIVE.isCrossServerSafe());
        assertFalse(Guarantee.DELEGATED.isCrossServerSafe(), "delegated does not promise indivisibility");
        assertFalse(Guarantee.EMULATED.isCrossServerSafe(), "emulated only covers this JVM");
    }

    @Test
    @DisplayName("a null amount is normalised to zero rather than kept null")
    void nullAmountBecomesZero() {
        assertEquals(0, TransactionResult.failed(null, "boom").getAmount().compareTo(BigDecimal.ZERO));
    }

    @Test
    @DisplayName("an unreported balance stays null instead of being invented")
    void unreportedBalanceStaysNull() {
        assertNull(TransactionResult.success(BigDecimal.TEN, null, Guarantee.NATIVE).getBalance());
        assertNull(TransactionResult.failed(BigDecimal.TEN, "boom").getBalance());
    }

    @Test
    @DisplayName("failure statuses carry an explanation")
    void failuresCarryAnExplanation() {
        assertNotNull(TransactionResult.unsupported(BigDecimal.TEN, "not implemented").getErrorMessage());
        assertNotNull(TransactionResult.failed(BigDecimal.TEN, "boom").getErrorMessage());
        assertNull(TransactionResult.success(BigDecimal.TEN, null, Guarantee.NATIVE).getErrorMessage());
    }
}
