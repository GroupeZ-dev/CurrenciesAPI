package fr.traqueur.currencies.providers;

import fr.maxlego08.essentials.api.EssentialsPlugin;
import fr.maxlego08.essentials.api.economy.Economy;
import fr.maxlego08.essentials.api.economy.EconomyManager;
import fr.traqueur.currencies.CurrencyArgumentChecks;
import fr.traqueur.currencies.CurrencyProvider;
import fr.traqueur.currencies.TransactionResult;
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
        if (this.economyManager == null || this.economy == null) {
            EssentialsPlugin essentialsPlugin = (EssentialsPlugin) Bukkit.getPluginManager().getPlugin("zEssentials");
            assert essentialsPlugin != null : "zEssentials plugin not found";
            this.economyManager = essentialsPlugin.getEconomyManager();
            Optional<Economy> optional = this.economyManager.getEconomy(this.economyName);
            if (optional.isPresent()) {
                this.economy = optional.get();
            } else {
                throw new NullPointerException("ZEssentials economy " + this.economyName + " not found");
            }
        }
    }

    @Override
    public void deposit(UUID playerId, BigDecimal amount, String reason) {
        this.initialize();
        this.economyManager.deposit(playerId, this.economy, amount, reason);
    }

    @Override
    public void withdraw(UUID playerId, BigDecimal amount, String reason) {
        this.initialize();
        this.economyManager.withdraw(playerId, this.economy, amount, reason);
    }

    @Override
    public BigDecimal getBalance(UUID playerId) {
        this.initialize();
        return this.economyManager.getBalance(Bukkit.getOfflinePlayer(playerId), this.economy);
    }

    @Override
    public boolean hasNativeConditionalWithdraw() {
        return true;
    }

    @Override
    public TransactionResult withdrawIfSufficient(UUID playerId, BigDecimal amount, String reason) {
        TransactionResult invalid = CurrencyArgumentChecks.findProblem(playerId, amount);
        if (invalid != null) {
            return invalid;
        }

        try {
            this.initialize();
            if (this.economyManager.withdraw(playerId, this.economy, amount, reason)) {
                return TransactionResult.nativeSuccess(amount, this.getBalance(playerId));
            }
            return TransactionResult.nativeInsufficientFunds(amount, this.getBalance(playerId));
        } catch (Exception exception) {
            return TransactionResult.failed(amount, "zEssentials threw while withdrawing: " + exception.getMessage());
        }
    }
}
