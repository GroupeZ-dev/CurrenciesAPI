package fr.traqueur.currencies.providers;

import fr.traqueur.currencies.CurrencyArgumentChecks;
import fr.traqueur.currencies.CurrencyProvider;
import fr.traqueur.currencies.Guarantee;
import fr.traqueur.currencies.TransactionResult;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.UUID;

public class ExperienceProvider implements CurrencyProvider {

    @Override
    public void deposit(UUID playerId, BigDecimal amount, String reason) {
        Player player = Bukkit.getPlayer(playerId);
        if (player != null) {
            BigDecimal totalExperience = BigDecimal.valueOf(this.getTotalExperience(player));
            this.setTotalExperience(player, totalExperience.add(amount).intValue());
        }
    }

    @Override
    public void withdraw(UUID playerId, BigDecimal amount, String reason) {
        Player player = Bukkit.getPlayer(playerId);
        if (player != null) {
            BigDecimal totalExperience = BigDecimal.valueOf(this.getTotalExperience(player));
            BigDecimal newExperience = totalExperience.subtract(amount);
            this.setTotalExperience(player, newExperience.max(BigDecimal.ZERO).intValue());
        }
    }

    @Override
    public BigDecimal getBalance(UUID playerId) {
        Player player = Bukkit.getPlayer(playerId);
        return player != null ? BigDecimal.valueOf(this.getTotalExperience(player)) : BigDecimal.ZERO;
    }

    private void setTotalExperience(Player player, int experience) {
        if (experience < 0) throw new IllegalArgumentException("Experience is negative!");
        player.setExp(0.0F);
        player.setLevel(0);
        player.setTotalExperience(0);
        int currentExperience = experience;
        while (currentExperience > 0) {
            int j = this.getExpAtLevel(player);
            currentExperience -= j;
            if (currentExperience >= 0) {
                player.giveExp(j);
                continue;
            }
            currentExperience += j;
            player.giveExp(currentExperience);
            currentExperience = 0;
        }
    }

    private int getExpAtLevel(Player player) {
        return this.getExpAtLevel(player.getLevel());
    }

    private int getExpAtLevel(int experience) {
        if (experience <= 15) return 2 * experience + 7;
        if (experience <= 30) return 5 * experience - 38;
        return 9 * experience - 158;
    }

    private int getTotalExperience(Player player) {
        int experience = Math.round(this.getExpAtLevel(player) * player.getExp());
        int playerLevel = player.getLevel();
        while (playerLevel > 0) {
            playerLevel--;
            experience += this.getExpAtLevel(playerLevel);
        }
        if (experience < 0) {
            experience = Integer.MAX_VALUE;
        }
        return experience;
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
            return TransactionResult.failed(amount, "Experience can only be taken from an online player.");
        }

        if (amount.stripTrailingZeros().scale() > 0) {
            return TransactionResult.failed(amount, "Experience only supports whole amounts, got " + amount + ".");
        }

        BigDecimal current = BigDecimal.valueOf(this.getTotalExperience(player));
        if (current.compareTo(amount) < 0) {
            return TransactionResult.insufficientFunds(amount, current, Guarantee.NATIVE);
        }

        BigDecimal remaining = current.subtract(amount);
        this.setTotalExperience(player, remaining.intValue());
        return TransactionResult.success(amount, remaining, Guarantee.NATIVE);
    }
}
