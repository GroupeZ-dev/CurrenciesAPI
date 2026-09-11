package fr.traqueur.currencies;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class CurrencyArgumentChecksTest {

    @Test
    @DisplayName("usable arguments report no problem")
    void usableArgumentsReportNoProblem() {
        assertNull(CurrencyArgumentChecks.findProblem(UUID.randomUUID(), BigDecimal.TEN));
    }

    @Test
    @DisplayName("a null player is a problem")
    void nullPlayerIsAProblem() {
        TransactionResult problem = CurrencyArgumentChecks.findProblem(null, BigDecimal.TEN);
        assertNotNull(problem);
        assertSame(TransactionResult.Status.FAILED, problem.getStatus());
    }

    @Test
    @DisplayName("a null amount is a problem")
    void nullAmountIsAProblem() {
        TransactionResult problem = CurrencyArgumentChecks.findProblem(UUID.randomUUID(), null);
        assertNotNull(problem);
        assertSame(TransactionResult.Status.FAILED, problem.getStatus());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-0.01", "-1", "-1000000"})
    @DisplayName("a zero or negative amount is a problem")
    void nonPositiveAmountIsAProblem(String amount) {
        TransactionResult problem = CurrencyArgumentChecks.findProblem(UUID.randomUUID(), new BigDecimal(amount));
        assertNotNull(problem, amount + " should be refused");
        assertSame(TransactionResult.Status.FAILED, problem.getStatus());
    }
}
