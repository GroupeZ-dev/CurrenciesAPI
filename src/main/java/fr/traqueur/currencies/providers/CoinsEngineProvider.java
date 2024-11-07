package fr.traqueur.currencies.providers;

import fr.traqueur.currencies.CurrencyProvider;
import org.bukkit.OfflinePlayer;
import su.nightexpress.coinsengine.api.CoinsEngineAPI;
import su.nightexpress.coinsengine.api.currency.Currency;

import java.math.BigDecimal;

public class CoinsEngineProvider implements CurrencyProvider {

    private final Currency currency;

    public CoinsEngineProvider(String currencyName) {
        this.currency = CoinsEngineAPI.getCurrency(currencyName);
    }

    @Override
    public void deposit(OfflinePlayer offlinePlayer, BigDecimal amount) {
        CoinsEngineAPI.addBalance(offlinePlayer.getUniqueId(), this.currency, amount.doubleValue());
    }

    @Override
    public void withdraw(OfflinePlayer offlinePlayer, BigDecimal amount) {
        CoinsEngineAPI.removeBalance(offlinePlayer.getUniqueId(), this.currency, amount.doubleValue());
    }

    @Override
    public BigDecimal getBalance(OfflinePlayer offlinePlayer) {
        return BigDecimal.valueOf(CoinsEngineAPI.getBalance(offlinePlayer.getUniqueId(), this.currency));
    }
}
