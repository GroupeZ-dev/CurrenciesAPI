package fr.traqueur.currencies.providers;

import dev.unnm3d.rediseconomy.api.RedisEconomyAPI;
import dev.unnm3d.rediseconomy.currency.Currency;
import fr.traqueur.currencies.CurrencyProvider;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;

public class RedisEconomyProvider implements CurrencyProvider {

    private final String economyName;

    public RedisEconomyProvider(String economyName) {
        this.economyName = economyName;
    }

    private Currency getCurrency() {
        RedisEconomyAPI api = RedisEconomyAPI.getAPI();
        if (api == null) {
            Bukkit.getLogger().info("RedisEconomyAPI not found!");
            return null;
        }
        Currency currency = api.getCurrencyByName(this.economyName);
        if (currency == null) {
            Bukkit.getLogger().info("Currency " + this.economyName + " not found!");
        }
        return currency;
    }

    @Override
    public void deposit(OfflinePlayer offlinePlayer, BigDecimal amount, String reason) {
        Currency currency = getCurrency();
        if (currency != null) {
            currency.depositPlayer(offlinePlayer, amount.doubleValue());
        }
    }

    @Override
    public void withdraw(OfflinePlayer offlinePlayer, BigDecimal amount, String reason) {
        Currency currency = getCurrency();
        if (currency != null) {
            currency.withdrawPlayer(offlinePlayer, amount.doubleValue());
        }
    }

    @Override
    public BigDecimal getBalance(OfflinePlayer offlinePlayer) {
        Currency currency = getCurrency();
        return currency != null ? BigDecimal.valueOf(currency.getBalance(offlinePlayer)) : BigDecimal.ZERO;
    }
}
