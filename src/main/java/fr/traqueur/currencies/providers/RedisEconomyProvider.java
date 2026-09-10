package fr.traqueur.currencies.providers;

import dev.unnm3d.rediseconomy.api.RedisEconomyAPI;
import dev.unnm3d.rediseconomy.currency.Currency;
import fr.traqueur.currencies.CurrencyArgumentChecks;
import fr.traqueur.currencies.CurrencyProvider;
import fr.traqueur.currencies.TransactionResult;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;
import java.util.UUID;

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
    public void deposit(UUID playerId, BigDecimal amount, String reason) {
        Currency currency = getCurrency();
        if (currency != null) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerId);
            currency.depositPlayer(offlinePlayer, amount.doubleValue());
        }
    }

    @Override
    public void withdraw(UUID playerId, BigDecimal amount, String reason) {
        Currency currency = getCurrency();
        if (currency != null) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerId);
            currency.withdrawPlayer(offlinePlayer, amount.doubleValue());
        }
    }

    @Override
    public BigDecimal getBalance(UUID playerId) {
        Currency currency = getCurrency();
        if (currency != null) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerId);
            return BigDecimal.valueOf(currency.getBalance(offlinePlayer));
        }
        return BigDecimal.ZERO;
    }

    @Override
    public boolean hasNativeConditionalWithdraw() {
        return true;
    }

    @Override
    public TransactionResult withdrawIfSufficient(UUID playerId, BigDecimal amount, String reason) {
        TransactionResult invalid = CurrencyArgumentChecks.findProblem(playerId, amount);
        if (invalid != null) {
            return invalid;
        }

        try {
            Currency currency = this.getCurrency();
            if (currency == null) {
                return TransactionResult.failed(amount, "The RedisEconomy currency " + this.economyName + " was not found.");
            }

            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerId);
            EconomyResponse response = currency.withdrawPlayer(offlinePlayer, amount.doubleValue());
            if (response == null) {
                return TransactionResult.failed(amount, "RedisEconomy returned no response.");
            }

            if (response.type == EconomyResponse.ResponseType.SUCCESS) {
                return TransactionResult.nativeSuccess(amount, BigDecimal.valueOf(response.balance));
            }

            if (response.type == EconomyResponse.ResponseType.NOT_IMPLEMENTED) {
                return TransactionResult.unsupported(amount, "RedisEconomy does not implement withdrawPlayer.");
            }

            if (!currency.has(playerId, amount.doubleValue())) {
                return TransactionResult.nativeInsufficientFunds(amount, BigDecimal.valueOf(currency.getBalance(playerId)));
            }

            return TransactionResult.failed(amount, response.errorMessage == null
                    ? "RedisEconomy refused the withdrawal."
                    : response.errorMessage);
        } catch (Exception exception) {
            return TransactionResult.failed(amount, "RedisEconomy threw while withdrawing: " + exception.getMessage());
        }
    }

    @Override
    public boolean requiresMainThread() {
        return false;
    }
}
