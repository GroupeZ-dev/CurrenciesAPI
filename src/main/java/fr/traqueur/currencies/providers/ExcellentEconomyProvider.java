package fr.traqueur.currencies.providers;

import fr.traqueur.currencies.CurrencyProvider;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import su.nightexpress.excellenteconomy.api.ExcellentEconomyAPI;
import su.nightexpress.excellenteconomy.api.currency.operation.OperationContext;

import java.math.BigDecimal;
import java.util.UUID;

public class ExcellentEconomyProvider implements CurrencyProvider {
    private final ExcellentEconomyAPI api;
    private final String currencyName;

    public ExcellentEconomyProvider(String currencyName) {
        this.currencyName = currencyName;
        RegisteredServiceProvider<ExcellentEconomyAPI> provider = Bukkit.getServer().getServicesManager().getRegistration(ExcellentEconomyAPI.class);
        if (provider == null) {
            throw new IllegalStateException("ExcellentEconomy service not registered");
        }
        this.api = provider.getProvider();
    }

    @Override
    public void deposit(UUID playerId, BigDecimal amount, String reason) {
        OperationContext ctx = OperationContext.custom(reason);
        Player player = Bukkit.getPlayer(playerId);
        if (player != null) {
            this.api.deposit(player, this.currencyName, amount.doubleValue(), ctx);
        } else {
            this.api.depositAsync(playerId, this.currencyName, amount.doubleValue(), ctx);
        }
    }

    @Override
    public void withdraw(UUID playerId, BigDecimal amount, String reason) {
        OperationContext ctx = OperationContext.custom(reason);
        Player player = Bukkit.getPlayer(playerId);
        if (player != null) {
            this.api.withdraw(player, this.currencyName, amount.doubleValue(), ctx);
        } else {
            this.api.withdrawAsync(playerId, this.currencyName, amount.doubleValue(), ctx);
        }
    }

    @Override
    public BigDecimal getBalance(UUID playerId) {
        Player player = Bukkit.getPlayer(playerId);
        double raw = player != null
                ? this.api.getBalance(player, this.currencyName)
                : this.api.getBalanceAsync(playerId, this.currencyName).join();
        return BigDecimal.valueOf(raw);
    }
}
