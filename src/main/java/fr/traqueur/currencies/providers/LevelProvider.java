package fr.traqueur.currencies.providers;

import fr.traqueur.currencies.CurrencyProvider;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;

public class LevelProvider implements CurrencyProvider {

    @Override
    public void deposit(OfflinePlayer offlinePlayer, BigDecimal amount) {
        if (offlinePlayer.isOnline()) {
            int level = offlinePlayer.getPlayer().getLevel();
            offlinePlayer.getPlayer().setLevel(level + amount.intValue());
        }
    }

    @Override
    public void withdraw(OfflinePlayer offlinePlayer, BigDecimal amount) {
        if (offlinePlayer.isOnline()) {
            int level = offlinePlayer.getPlayer().getLevel();
            offlinePlayer.getPlayer().setLevel(level - amount.intValue());
        }
    }

    @Override
    public BigDecimal getBalance(OfflinePlayer offlinePlayer) {
        return BigDecimal.valueOf(offlinePlayer.isOnline() ? offlinePlayer.getPlayer().getLevel() : 0);
    }
}
