package fr.traqueur.currencies.providers;

import fr.traqueur.currencies.CurrencyProvider;
import org.bukkit.OfflinePlayer;
import su.nightexpress.coinsengine.api.CoinsEngineAPI;
import su.nightexpress.coinsengine.api.currency.Currency;
import su.nightexpress.coinsengine.data.impl.CoinsUser;

import java.math.BigDecimal;

public class CoinsEngineProvider implements CurrencyProvider {

    private final Currency currency;

    public CoinsEngineProvider(String currencyName) {
        this.currency = CoinsEngineAPI.getCurrency(currencyName);
    }

    @Override
    public void deposit(OfflinePlayer offlinePlayer, BigDecimal amount) {
        CoinsUser user = CoinsEngineAPI.getUserData(offlinePlayer.getUniqueId());
        user.addBalance(this.currency, amount.doubleValue());
        CoinsEngineAPI.getUserManager().save(user);
    }

    @Override
    public void withdraw(OfflinePlayer offlinePlayer, BigDecimal amount) {
        CoinsUser user = CoinsEngineAPI.getUserData(offlinePlayer.getUniqueId());
        user.removeBalance(this.currency, amount.doubleValue());
        CoinsEngineAPI.getUserManager().save(user);
    }

    @Override
    public BigDecimal getBalance(OfflinePlayer offlinePlayer) {
        CoinsUser user = CoinsEngineAPI.getUserData(offlinePlayer.getUniqueId());
        return BigDecimal.valueOf(user == null ? 0 : (long) user.getBalance(this.currency));
    }
}
