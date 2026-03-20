package fr.traqueur.currencies.providers;

import com.willfp.ecobits.currencies.Currencies;
import com.willfp.ecobits.currencies.Currency;
import com.willfp.ecobits.currencies.CurrencyUtils;
import fr.traqueur.currencies.CurrencyProvider;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;
import java.util.UUID;

public class EcoBitProvider implements CurrencyProvider {

    private Currency currency;
    private final String currencyName;

    public EcoBitProvider(String currencyName) {
        this.currencyName = currencyName;
    }

    private void initialize() {
        if (currency == null) {
            this.currency = Currencies.getByID(currencyName);
        }
    }

    @Override
    public void deposit(UUID playerId, BigDecimal amount, String reason) {
        initialize();
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerId);
        CurrencyUtils.adjustBalance(offlinePlayer, currency, amount);
    }

    @Override
    public void withdraw(UUID playerId, BigDecimal amount, String reason) {
        initialize();
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerId);
        CurrencyUtils.adjustBalance(offlinePlayer, currency, amount.negate());
    }

    @Override
    public BigDecimal getBalance(UUID playerId) {
        initialize();
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerId);
        return CurrencyUtils.getBalance(offlinePlayer, currency);
    }
}
