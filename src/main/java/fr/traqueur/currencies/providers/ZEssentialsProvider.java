package fr.traqueur.currencies.providers;

import fr.maxlego08.essentials.api.EssentialsPlugin;
import fr.maxlego08.essentials.api.economy.Economy;
import fr.maxlego08.essentials.api.economy.EconomyManager;
import fr.traqueur.currencies.CurrencyProvider;
import org.bukkit.Bukkit;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

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
    public void deposit(UUID playerId, BigDecimal amount, String reason) {
        initialize();
        this.economyManager.deposit(playerId, this.economy, amount, reason);
    }

    @Override
    public void withdraw(UUID playerId, BigDecimal amount, String reason) {
        initialize();
        this.economyManager.withdraw(playerId, this.economy, amount, reason);
    }

    @Override
    public BigDecimal getBalance(UUID playerId) {
        initialize();
        return this.economyManager.getBalance(Bukkit.getOfflinePlayer(playerId), this.economy);
    }
}
