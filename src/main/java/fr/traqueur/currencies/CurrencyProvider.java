package fr.traqueur.currencies;

import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;

/**
 * Interface used to interact with a currency provider.
 *
 * @author Traqueur
 */
public interface CurrencyProvider {

    /**
     * Deposit a certain amount of currency to a player.
     *
     * @param offlinePlayer The player to deposit the money.
     * @param amount The amount of currency to deposit.
     */
    void deposit(OfflinePlayer offlinePlayer, BigDecimal amount);

    /**
     * Withdraw a certain amount of currency from a player.
     *
     * @param offlinePlayer The player to withdraw the money.
     * @param amount The amount of currency to withdraw.
     */
    void withdraw(OfflinePlayer offlinePlayer, BigDecimal amount);

    /**
     * Get the balance of a player.
     *
     * @param offlinePlayer The player to get the balance.
     * @return The balance of the player.
     */
    BigDecimal getBalance(OfflinePlayer offlinePlayer);

}
