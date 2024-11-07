package fr.traqueur.currencies.providers;

import fr.traqueur.currencies.CurrencyProvider;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.math.BigDecimal;

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
    public void deposit(OfflinePlayer player, BigDecimal amount) {
        this.getEconomy().depositPlayer(player, amount.doubleValue());
    }

    @Override
    public void withdraw(OfflinePlayer player, BigDecimal amount) {
        this.getEconomy().withdrawPlayer(player, amount.doubleValue());
    }

    @Override
    public BigDecimal getBalance(OfflinePlayer player) {
        return BigDecimal.valueOf(this.getEconomy().getBalance(player));
    }
}
