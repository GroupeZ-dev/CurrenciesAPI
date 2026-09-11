package fr.traqueur.currencies.providers;

import fr.traqueur.currencies.CurrencyArgumentChecks;
import fr.traqueur.currencies.CurrencyProvider;
import fr.traqueur.currencies.Guarantee;
import fr.traqueur.currencies.TransactionResult;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.UUID;

public class LevelProvider implements CurrencyProvider {

    @Override
    public void deposit(UUID playerId, BigDecimal amount, String reason) {
        Player player = Bukkit.getPlayer(playerId);
        if (player != null) {
            int level = player.getLevel();
            player.setLevel(level + amount.intValue());
        }
    }

    @Override
    public void withdraw(UUID playerId, BigDecimal amount, String reason) {
        Player player = Bukkit.getPlayer(playerId);
        if (player != null) {
            int level = player.getLevel();
            player.setLevel(level - amount.intValue());
        }
    }

    @Override
    public BigDecimal getBalance(UUID playerId) {
        Player player = Bukkit.getPlayer(playerId);
        return BigDecimal.valueOf(player != null ? player.getLevel() : 0);
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

        Player player = Bukkit.getPlayer(playerId);
        if (player == null) {
            return TransactionResult.failed(amount, "Levels can only be taken from an online player.");
        }

        if (amount.stripTrailingZeros().scale() > 0) {
            return TransactionResult.failed(amount, "Levels only support whole amounts, got " + amount + ".");
        }

        int cost;
        try {
            cost = amount.intValueExact();
        } catch (ArithmeticException exception) {
            return TransactionResult.failed(amount, "The amount does not fit in an integer level: " + amount + ".");
        }

        int current = player.getLevel();
        if (current < cost) {
            return TransactionResult.insufficientFunds(amount, BigDecimal.valueOf(current), Guarantee.NATIVE);
        }

        player.setLevel(current - cost);
        return TransactionResult.success(amount, BigDecimal.valueOf(current - cost), Guarantee.NATIVE);
    }
}
