package fr.traqueur.currencies;

import org.bukkit.OfflinePlayer;

public interface CurrencyProvider {

    void deposit(OfflinePlayer player, double amount);

    void withdraw(OfflinePlayer player, double amount);

    double getBalance(OfflinePlayer player);

}
