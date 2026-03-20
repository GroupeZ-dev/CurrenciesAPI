package fr.traqueur.currencies.providers;

import fr.traqueur.currencies.CurrencyProvider;
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
}
