package fr.traqueur.currencies;

import java.math.BigDecimal;
import java.util.UUID;

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

}
