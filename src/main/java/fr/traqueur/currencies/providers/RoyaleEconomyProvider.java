package fr.traqueur.currencies.providers;

import fr.traqueur.currencies.CurrencyProvider;
import me.qKing12.RoyaleEconomy.API.Currency;
import me.qKing12.RoyaleEconomy.API.MultiCurrencyHandler;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;

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
    public void deposit(OfflinePlayer offlinePlayer, BigDecimal amount, String reason) {
        initialize();
        currency.addAmount(offlinePlayer.getUniqueId().toString(), amount.doubleValue());
    }

    @Override
    public void withdraw(OfflinePlayer offlinePlayer, BigDecimal amount, String reason) {
        initialize();
        currency.removeAmount(offlinePlayer.getUniqueId().toString(), amount.doubleValue());
    }

    @Override
    public BigDecimal getBalance(OfflinePlayer offlinePlayer) {
        initialize();
        return BigDecimal.valueOf(currency.getAmount(offlinePlayer.getUniqueId().toString()));
    }
}
