package fr.traqueur.currencies.providers;

import fr.traqueur.currencies.CurrencyProvider;
import su.nightexpress.coinsengine.api.CoinsEngineAPI;

import java.math.BigDecimal;
import java.util.UUID;

public class CoinsEngineProvider implements CurrencyProvider {

    private final String currencyName;

    public CoinsEngineProvider(String currencyName) {
        this.currencyName = currencyName;
    }

    @Override
    public void deposit(UUID playerId, BigDecimal amount, String reason) {
        CoinsEngineAPI.addBalance(playerId, this.currencyName, amount.doubleValue());
    }

    @Override
    public void withdraw(UUID playerId, BigDecimal amount, String reason) {
        CoinsEngineAPI.removeBalance(playerId, this.currencyName, amount.doubleValue());
    }

    @Override
    public BigDecimal getBalance(UUID playerId) {
        return BigDecimal.valueOf(CoinsEngineAPI.getBalance(playerId, this.currencyName));
    }
}
