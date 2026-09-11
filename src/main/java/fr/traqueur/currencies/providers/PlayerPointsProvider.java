package fr.traqueur.currencies.providers;

import fr.traqueur.currencies.CurrencyArgumentChecks;
import fr.traqueur.currencies.CurrencyProvider;
import fr.traqueur.currencies.Guarantee;
import fr.traqueur.currencies.TransactionResult;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.plugin.java.JavaPlugin;

import java.math.BigDecimal;
import java.util.UUID;

public class PlayerPointsProvider implements CurrencyProvider {

    private PlayerPointsAPI playerPointsAPI;

    private PlayerPointsAPI getAPI() {
        if (this.playerPointsAPI == null) {
            PlayerPoints playerPoints = JavaPlugin.getPlugin(PlayerPoints.class);
            this.playerPointsAPI = playerPoints.getAPI();
        }

        return this.playerPointsAPI;
    }

    @Override
    public void deposit(UUID playerId, BigDecimal amount, String reason) {
        this.getAPI().give(playerId, amount.intValue());
    }

    @Override
    public void withdraw(UUID playerId, BigDecimal amount, String reason) {
        this.getAPI().take(playerId, amount.intValue());
    }

    @Override
    public BigDecimal getBalance(UUID playerId) {
        return BigDecimal.valueOf(this.getAPI().look(playerId));
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
            if (amount.stripTrailingZeros().scale() > 0) {
                return TransactionResult.failed(amount, "PlayerPoints only supports whole amounts, got " + amount + ".");
            }

            int points = amount.intValueExact();
            if (this.getAPI().take(playerId, points)) {
                return TransactionResult.success(amount, BigDecimal.valueOf(this.getAPI().look(playerId)), Guarantee.DELEGATED);
            }

            return TransactionResult.insufficientFunds(amount, BigDecimal.valueOf(this.getAPI().look(playerId)), Guarantee.DELEGATED);
        } catch (ArithmeticException exception) {
            return TransactionResult.failed(amount, "The amount does not fit in a PlayerPoints integer: " + amount + ".");
        } catch (Exception exception) {
            return TransactionResult.failed(amount, "PlayerPoints threw while withdrawing: " + exception.getMessage());
        }
    }
}
