package fr.traqueur.currencies.providers;

import fr.traqueur.currencies.CurrencyProvider;
import me.elementalgaming.ElementalTokens.TokenAPI;
import org.bukkit.OfflinePlayer;

public class ElementalTokensProvider implements CurrencyProvider {
    @Override
    public void deposit(OfflinePlayer player, double amount) {
        TokenAPI.addTokens(player.getUniqueId(), (long) amount);
    }

    @Override
    public void withdraw(OfflinePlayer player, double amount) {
        TokenAPI.removeTokens(player.getUniqueId(), (long) amount);
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        return TokenAPI.getTokens(player.getUniqueId());
    }
}
