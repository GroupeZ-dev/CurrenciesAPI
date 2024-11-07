package fr.traqueur.currencies.providers;

import fr.traqueur.currencies.CurrencyProvider;
import me.elementalgaming.ElementalGems.GemAPI;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;

public class ElementalGemsProvider implements CurrencyProvider {
    @Override
    public void deposit(OfflinePlayer offlinePlayer, BigDecimal amount) {
        GemAPI.addGems(offlinePlayer.getUniqueId(), amount.doubleValue());
    }

    @Override
    public void withdraw(OfflinePlayer offlinePlayer, BigDecimal amount) {
        GemAPI.removeGems(offlinePlayer.getUniqueId(), amount.doubleValue());
    }

    @Override
    public BigDecimal getBalance(OfflinePlayer offlinePlayer) {
        return BigDecimal.valueOf(GemAPI.getGems(offlinePlayer.getUniqueId()));
    }
}
