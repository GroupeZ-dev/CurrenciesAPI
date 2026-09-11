package fr.traqueur.currencies.providers;

import fr.traqueur.currencies.CurrencyArgumentChecks;
import fr.traqueur.currencies.CurrencyProvider;
import fr.traqueur.currencies.Guarantee;
import fr.traqueur.currencies.TransactionResult;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import su.nightexpress.excellenteconomy.api.ExcellentEconomyAPI;
import su.nightexpress.excellenteconomy.api.currency.operation.OperationContext;
import su.nightexpress.excellenteconomy.api.currency.operation.OperationResult;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.UUID;

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
    public void deposit(UUID playerId, BigDecimal amount, String reason) {
        OperationContext ctx = OperationContext.custom(reason);
        Player player = Bukkit.getPlayer(playerId);
        if (player != null) {
            this.api.deposit(player, this.currencyName, amount.doubleValue(), ctx);
        } else {
            this.api.depositAsync(playerId, this.currencyName, amount.doubleValue(), ctx);
        }
    }

    @Override
    public void withdraw(UUID playerId, BigDecimal amount, String reason) {
        OperationContext ctx = OperationContext.custom(reason);
        Player player = Bukkit.getPlayer(playerId);
        if (player != null) {
            this.api.withdraw(player, this.currencyName, amount.doubleValue(), ctx);
        } else {
            this.api.withdrawAsync(playerId, this.currencyName, amount.doubleValue(), ctx);
        }
    }

    @Override
    public BigDecimal getBalance(UUID playerId) {
        Player player = Bukkit.getPlayer(playerId);
        double raw = player != null
                ? this.api.getBalance(player, this.currencyName)
                : this.api.getBalanceAsync(playerId, this.currencyName).join();
        return BigDecimal.valueOf(raw);
    }

    private BigDecimal getBalanceWithoutBukkit(UUID playerId) {
        try {
            Double raw = this.api.getBalanceAsync(playerId, this.currencyName).join();
            return raw == null ? null : BigDecimal.valueOf(raw);
        } catch (Exception exception) {
            return null;
        }
    }

    @Override
    public Guarantee getWithdrawGuarantee() {
        return Guarantee.NATIVE;
    }

    @Override
    public TransactionResult withdrawIfSufficient(UUID playerId, BigDecimal amount, String reason) {
        TransactionResult invalid = CurrencyArgumentChecks.findProblem(playerId, amount);
        if (invalid != null) {
            return invalid;
        }

        try {
            OperationContext ctx = OperationContext.custom(reason);

            OperationResult result = this.api.withdrawAsync(playerId, this.currencyName, amount.doubleValue(), ctx).join();

            if (result != null && result.success()) {
                return TransactionResult.success(amount, null, Guarantee.NATIVE);
            }

            BigDecimal balance = this.getBalanceWithoutBukkit(playerId);
            if (balance != null && balance.compareTo(amount) < 0) {
                return TransactionResult.insufficientFunds(amount, balance, Guarantee.NATIVE);
            }

            return TransactionResult.failed(amount, "ExcellentEconomy refused the withdrawal and the player could afford it, "
                    + "so the currency " + this.currencyName + " may be unknown or the player data may not be loaded.");
        } catch (Exception exception) {
            return TransactionResult.failed(amount, "ExcellentEconomy threw while withdrawing: " + exception.getMessage());
        }
    }

    @Override
    public CompletableFuture<TransactionResult> withdrawIfSufficientAsync(UUID playerId, BigDecimal amount, String reason) {
        TransactionResult invalid = CurrencyArgumentChecks.findProblem(playerId, amount);
        if (invalid != null) {
            return CompletableFuture.completedFuture(invalid);
        }

        try {
            OperationContext ctx = OperationContext.custom(reason);
            return this.api.withdrawAsync(playerId, this.currencyName, amount.doubleValue(), ctx)
                    .handle((result, throwable) -> {
                        if (throwable != null) {
                            return TransactionResult.failed(amount, "ExcellentEconomy threw while withdrawing: " + throwable.getMessage());
                        }
                        if (result != null && result.success()) {
                            return TransactionResult.success(amount, null, Guarantee.NATIVE);
                        }
                        BigDecimal balance = this.getBalanceWithoutBukkit(playerId);
                        if (balance != null && balance.compareTo(amount) < 0) {
                            return TransactionResult.insufficientFunds(amount, balance, Guarantee.NATIVE);
                        }
                        return TransactionResult.failed(amount, "ExcellentEconomy refused the withdrawal and the player could afford it, "
                                + "so the currency " + this.currencyName + " may be unknown or the player data may not be loaded.");
                    });
        } catch (Exception exception) {
            return CompletableFuture.completedFuture(
                    TransactionResult.failed(amount, "ExcellentEconomy threw while withdrawing: " + exception.getMessage()));
        }
    }

    @Override
    public boolean requiresMainThread() {
        return false;
    }
}
