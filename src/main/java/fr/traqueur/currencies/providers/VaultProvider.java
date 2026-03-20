package fr.traqueur.currencies.providers;

import fr.traqueur.currencies.CurrencyProvider;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.math.BigDecimal;
import java.util.UUID;

public class VaultProvider implements CurrencyProvider {

    private Economy economy;

    private Economy getEconomy() {
        if (this.economy == null) {
            RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
            if (economyProvider != null) {
                this.economy = economyProvider.getProvider();
                return this.economy;
            } else {
                throw new NullPointerException("Vault Economy interface not found");
            }
        }
        return this.economy;
    }

    @Override
    public void deposit(UUID playerId, BigDecimal amount, String reason) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerId);
        this.getEconomy().depositPlayer(offlinePlayer, amount.doubleValue());
    }

    @Override
    public void withdraw(UUID playerId, BigDecimal amount, String reason) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerId);
        this.getEconomy().withdrawPlayer(offlinePlayer, amount.doubleValue());
    }

    @Override
    public BigDecimal getBalance(UUID playerId) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerId);
        return BigDecimal.valueOf(this.getEconomy().getBalance(offlinePlayer));
    }
}
