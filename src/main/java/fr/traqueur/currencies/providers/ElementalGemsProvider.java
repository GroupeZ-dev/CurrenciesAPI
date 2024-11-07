package fr.traqueur.currencies.providers;

import fr.traqueur.currencies.CurrencyProvider;
import me.elementalgaming.ElementalGems.GemAPI;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;

public class ElementalGemsProvider implements CurrencyProvider {
    @Override
    public void deposit(OfflinePlayer player, BigDecimal amount) {
        GemAPI.addGems(player.getUniqueId(), amount.doubleValue());
    }

    @Override
    public void withdraw(OfflinePlayer player, BigDecimal amount) {
        GemAPI.removeGems(player.getUniqueId(), amount.doubleValue());
    }

    @Override
    public BigDecimal getBalance(OfflinePlayer player) {
        return BigDecimal.valueOf(GemAPI.getGems(player.getUniqueId()));
    }
}
