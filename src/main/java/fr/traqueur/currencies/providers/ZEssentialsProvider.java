package fr.traqueur.currencies.providers;

import fr.maxlego08.essentials.api.EssentialsPlugin;
import fr.maxlego08.essentials.api.economy.Economy;
import fr.maxlego08.essentials.api.economy.EconomyManager;
import fr.traqueur.currencies.CurrencyProvider;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;
import java.util.Optional;

public class ZEssentialsProvider implements CurrencyProvider {

    private final EconomyManager economyManager;
    private final Economy economy;

    public ZEssentialsProvider(String economyName) {
        EssentialsPlugin essentialsPlugin = (EssentialsPlugin) Bukkit.getPluginManager().getPlugin("zEssentials");
        this.economyManager = essentialsPlugin.getEconomyManager();
        Optional<Economy> optional = economyManager.getEconomy(economyName);
        if (optional.isPresent()) {
            this.economy = optional.get();
        } else {
            throw new NullPointerException("ZEssentials economy " + economyName + " not found");
        }
    }

    @Override
    public void deposit(OfflinePlayer offlinePlayer, BigDecimal amount) {
        this.economyManager.deposit(offlinePlayer.getUniqueId(), this.economy, amount);
    }

    @Override
    public void withdraw(OfflinePlayer offlinePlayer, BigDecimal amount) {
        this.economyManager.withdraw(offlinePlayer.getUniqueId(), this.economy, amount);
    }

    @Override
    public BigDecimal getBalance(OfflinePlayer offlinePlayer) {
        return this.economyManager.getBalance(offlinePlayer, this.economy);
    }
}
