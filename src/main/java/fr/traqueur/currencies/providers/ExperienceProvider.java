package fr.traqueur.currencies.providers;

import fr.traqueur.currencies.CurrencyProvider;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.UUID;

public class ExperienceProvider implements CurrencyProvider {

    @Override
    public void deposit(UUID playerId, BigDecimal amount, String reason) {
        Player player = Bukkit.getPlayer(playerId);
        if (player != null) {
            BigDecimal totalExperience = BigDecimal.valueOf(getTotalExperience(player));
            setTotalExperience(player, totalExperience.add(amount).intValue());
        }
    }

    @Override
    public void withdraw(UUID playerId, BigDecimal amount, String reason) {
        Player player = Bukkit.getPlayer(playerId);
        if (player != null) {
            BigDecimal totalExperience = BigDecimal.valueOf(getTotalExperience(player));
            BigDecimal newExperience = totalExperience.subtract(amount);
            setTotalExperience(player, newExperience.max(BigDecimal.ZERO).intValue());
        }
    }

    @Override
    public BigDecimal getBalance(UUID playerId) {
        Player player = Bukkit.getPlayer(playerId);
        return player != null ? BigDecimal.valueOf(getTotalExperience(player)) : BigDecimal.ZERO;
    }

    private void setTotalExperience(Player player, int experience) {
        if (experience < 0) throw new IllegalArgumentException("Experience is negative!");
        player.setExp(0.0F);
        player.setLevel(0);
        player.setTotalExperience(0);
        int currentExperience = experience;
        while (currentExperience > 0) {
            int j = getExpAtLevel(player);
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
        return getExpAtLevel(player.getLevel());
    }

    private int getExpAtLevel(int experience) {
        if (experience <= 15) return 2 * experience + 7;
        if (experience <= 30) return 5 * experience - 38;
        return 9 * experience - 158;
    }

    private int getTotalExperience(Player player) {
        int experience = Math.round(getExpAtLevel(player) * player.getExp());
        int playerLevel = player.getLevel();
        while (playerLevel > 0) {
            playerLevel--;
            experience += getExpAtLevel(playerLevel);
        }
        if (experience < 0) {
            experience = Integer.MAX_VALUE;
        }
        return experience;
    }
}
