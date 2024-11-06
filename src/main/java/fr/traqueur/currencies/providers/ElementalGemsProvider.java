package fr.traqueur.currencies.providers;

import fr.traqueur.currencies.CurrencyProvider;
import me.elementalgaming.ElementalGems.GemAPI;
import org.bukkit.OfflinePlayer;

public class ElementalGemsProvider implements CurrencyProvider {
    @Override
    public void deposit(OfflinePlayer player, double amount) {
        GemAPI.addGems(player.getUniqueId(), (int) amount);
    }

    @Override
    public void withdraw(OfflinePlayer player, double amount) {
        GemAPI.removeGems(player.getUniqueId(), (int) amount);
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        return GemAPI.getGems(player.getUniqueId());
    }
}
