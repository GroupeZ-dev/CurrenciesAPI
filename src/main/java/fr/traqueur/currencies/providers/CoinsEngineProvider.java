package fr.traqueur.currencies.providers;

import fr.traqueur.currencies.CurrencyProvider;
import org.bukkit.OfflinePlayer;
import su.nightexpress.coinsengine.api.CoinsEngineAPI;

import java.math.BigDecimal;

public class CoinsEngineProvider implements CurrencyProvider {

    private final String currencyName;

    public CoinsEngineProvider(String currencyName) {
        this.currencyName = currencyName;
    }

    @Override
    public void deposit(OfflinePlayer offlinePlayer, BigDecimal amount, String reason) {
        CoinsEngineAPI.addBalance(offlinePlayer.getUniqueId(), this.currencyName, amount.doubleValue());
    }

    @Override
    public void withdraw(OfflinePlayer offlinePlayer, BigDecimal amount, String reason) {
        CoinsEngineAPI.removeBalance(offlinePlayer.getUniqueId(), this.currencyName, amount.doubleValue());
    }

    @Override
    public BigDecimal getBalance(OfflinePlayer offlinePlayer) {
        return BigDecimal.valueOf(CoinsEngineAPI.getBalance(offlinePlayer.getUniqueId(), this.currencyName));
    }
}
