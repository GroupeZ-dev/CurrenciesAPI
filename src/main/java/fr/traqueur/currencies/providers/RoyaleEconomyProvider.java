package fr.traqueur.currencies.providers;

import fr.traqueur.currencies.CurrencyProvider;
import me.qKing12.RoyaleEconomy.API.Currency;
import me.qKing12.RoyaleEconomy.API.MultiCurrencyHandler;

import java.math.BigDecimal;
import java.util.UUID;

public class RoyaleEconomyProvider implements CurrencyProvider {

    private Currency currency;
    private final String currencyId;

    public RoyaleEconomyProvider(String currencyId) {
        this.currencyId = currencyId;
    }

    @SuppressWarnings("ConstantConditions") // findCurrencyById always returns null in the API only
    private void initialize() {
        if (currency != null)
            return;

        if (MultiCurrencyHandler.getCurrencies() == null)
            throw new NullPointerException("RoyaleEconomy multi-currency not enabled.");

        currency = MultiCurrencyHandler.findCurrencyById(currencyId);
        if (currency == null)
            throw new NullPointerException("RoyaleEconomy currency " + currencyId + " not found");
    }

    @Override
    public void deposit(UUID playerId, BigDecimal amount, String reason) {
        initialize();
        currency.addAmount(playerId.toString(), amount.doubleValue());
    }

    @Override
    public void withdraw(UUID playerId, BigDecimal amount, String reason) {
        initialize();
        currency.removeAmount(playerId.toString(), amount.doubleValue());
    }

    @Override
    public BigDecimal getBalance(UUID playerId) {
        initialize();
        return BigDecimal.valueOf(currency.getAmount(playerId.toString()));
    }
}
