package fr.traqueur.currencies.providers;

import fr.traqueur.currencies.CurrencyArgumentChecks;
import fr.traqueur.currencies.CurrencyProvider;
import fr.traqueur.currencies.Guarantee;
import fr.traqueur.currencies.TransactionResult;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.math.BigDecimal;
import java.util.UUID;

public class VaultProvider implements CurrencyProvider {

    private Economy economy;

    private Economy getEconomy() {
        if (this.economy == null) {
            RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
            if (economyProvider != null) {
                this.economy = economyProvider.getProvider();
                return this.economy;
            } else {
                throw new NullPointerException("Vault Economy interface not found");
            }
        }
        return this.economy;
    }

    @Override
    public void deposit(UUID playerId, BigDecimal amount, String reason) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerId);
        this.getEconomy().depositPlayer(offlinePlayer, amount.doubleValue());
    }

    @Override
    public void withdraw(UUID playerId, BigDecimal amount, String reason) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerId);
        this.getEconomy().withdrawPlayer(offlinePlayer, amount.doubleValue());
    }

    @Override
    public BigDecimal getBalance(UUID playerId) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerId);
        return BigDecimal.valueOf(this.getEconomy().getBalance(offlinePlayer));
    }

    @Override
    public Guarantee getWithdrawGuarantee() {
        return Guarantee.DELEGATED;
    }

    @Override
    public TransactionResult withdrawIfSufficient(UUID playerId, BigDecimal amount, String reason) {
        TransactionResult invalid = CurrencyArgumentChecks.findProblem(playerId, amount);
        if (invalid != null) {
            return invalid;
        }

        try {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerId);
            Economy vaultEconomy = this.getEconomy();

            EconomyResponse response = vaultEconomy.withdrawPlayer(offlinePlayer, amount.doubleValue());
            if (response == null) {
                return TransactionResult.failed(amount, "The Vault economy returned no response.");
            }

            if (response.type == EconomyResponse.ResponseType.SUCCESS) {
                return TransactionResult.success(amount, BigDecimal.valueOf(response.balance), Guarantee.DELEGATED);
            }

            if (response.type == EconomyResponse.ResponseType.NOT_IMPLEMENTED) {
                return TransactionResult.unsupported(amount, "The Vault economy does not implement withdrawPlayer.");
            }

            BigDecimal balance = BigDecimal.valueOf(vaultEconomy.getBalance(offlinePlayer));
            if (balance.compareTo(amount) < 0) {
                return TransactionResult.insufficientFunds(amount, balance, Guarantee.DELEGATED);
            }

            return TransactionResult.failed(amount, response.errorMessage == null
                    ? "The Vault economy refused the withdrawal."
                    : response.errorMessage);
        } catch (Exception exception) {
            return TransactionResult.failed(amount, "The Vault economy threw while withdrawing: " + exception.getMessage());
        }
    }
}
