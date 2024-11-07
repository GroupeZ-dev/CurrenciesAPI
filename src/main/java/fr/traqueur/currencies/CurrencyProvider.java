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
     * @param player The player to deposit the money.
     * @param amount The amount of currency to deposit.
     */
    void deposit(OfflinePlayer player, BigDecimal amount);

    /**
     * Withdraw a certain amount of currency from a player.
     *
     * @param player The player to withdraw the money.
     * @param amount The amount of currency to withdraw.
     */
    void withdraw(OfflinePlayer player, BigDecimal amount);

    /**
     * Get the balance of a player.
     *
     * @param player The player to get the balance.
     * @return The balance of the player.
     */
    BigDecimal getBalance(OfflinePlayer player);

}
