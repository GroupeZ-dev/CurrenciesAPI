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
     * @param amount        The amount of currency to deposit.
     * @param reason        The reason of the deposit.
     */
    void deposit(OfflinePlayer offlinePlayer, BigDecimal amount, String reason);

    /**
     * Withdraw a certain amount of currency from a player.
     *
     * @param offlinePlayer The player to withdraw the money.
     * @param amount        The amount of currency to withdraw.
     * @param reason        The reason of the withdrawal.
     */
    void withdraw(OfflinePlayer offlinePlayer, BigDecimal amount, String reason);

    /**
     * Get the balance of a player.
     *
     * @param offlinePlayer The player to get the balance.
     * @return The balance of the player.
     */
    BigDecimal getBalance(OfflinePlayer offlinePlayer);

}
