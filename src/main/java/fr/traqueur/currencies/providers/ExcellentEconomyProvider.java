package fr.traqueur.currencies.providers;

import fr.traqueur.currencies.CurrencyProvider;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import su.nightexpress.excellenteconomy.api.ExcellentEconomyAPI;
import su.nightexpress.excellenteconomy.api.currency.operation.OperationContext;

import java.math.BigDecimal;

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
    public void deposit(OfflinePlayer player, BigDecimal amount, String reason) {
        OperationContext ctx = OperationContext.custom(reason);
        if (isOnline(player)) {
            this.api.deposit(asOnline(player), this.currencyName, amount.doubleValue(), ctx);
        } else {
            this.api.depositAsync(player.getUniqueId(), this.currencyName, amount.doubleValue(), ctx);
        }
    }

    @Override
    public void withdraw(OfflinePlayer player, BigDecimal amount, String reason) {
        OperationContext ctx = OperationContext.custom(reason);
        if (isOnline(player)) {
            this.api.withdraw(asOnline(player), this.currencyName, amount.doubleValue(), ctx);
        } else {
            this.api.withdrawAsync(player.getUniqueId(), this.currencyName, amount.doubleValue(), ctx);
        }
    }

    @Override
    public BigDecimal getBalance(OfflinePlayer player) {
        double raw = isOnline(player)
                ? this.api.getBalance(asOnline(player), this.currencyName)
                : this.api.getBalanceAsync(player.getUniqueId(), this.currencyName).join();
        return BigDecimal.valueOf(raw);
    }

    private boolean isOnline(OfflinePlayer player) {
        return player instanceof Player;
    }

    private Player asOnline(OfflinePlayer player) {
        return (Player) player;
    }
}