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

    private EconomyManager economyManager;
    private Economy economy;
    private final String economyName;

    public ZEssentialsProvider(String economyName) {
        this.economyName = economyName;
    }

    private void initialize() {
        if (economyManager == null || economy == null) {
            EssentialsPlugin essentialsPlugin = (EssentialsPlugin) Bukkit.getPluginManager().getPlugin("zEssentials");
            this.economyManager = essentialsPlugin.getEconomyManager();
            Optional<Economy> optional = economyManager.getEconomy(economyName);
            if (optional.isPresent()) {
                this.economy = optional.get();
            } else {
                throw new NullPointerException("ZEssentials economy " + economyName + " not found");
            }
        }
    }

    @Override
    public void deposit(OfflinePlayer offlinePlayer, BigDecimal amount, String reason) {
        initialize();
        this.economyManager.deposit(offlinePlayer.getUniqueId(), this.economy, amount, reason);
    }

    @Override
    public void withdraw(OfflinePlayer offlinePlayer, BigDecimal amount, String reason) {
        initialize();
        this.economyManager.withdraw(offlinePlayer.getUniqueId(), this.economy, amount, reason);
    }

    @Override
    public BigDecimal getBalance(OfflinePlayer offlinePlayer) {
        initialize();
        return this.economyManager.getBalance(offlinePlayer, this.economy);
    }
}
