package fr.traqueur.currencies.providers;

import fr.traqueur.currencies.CurrencyProvider;
import me.mraxetv.beasttokens.api.BeastTokensAPI;
import org.bukkit.OfflinePlayer;

public class BeastTokenProvider implements CurrencyProvider {
    @Override
    public void deposit(OfflinePlayer player, double amount) {
        if(player.isOnline()) {
            BeastTokensAPI.getTokensManager().addTokens(player.getPlayer(), amount);
        } else {
            BeastTokensAPI.getTokensManager().addTokens(player, amount);
        }
    }

    @Override
    public void withdraw(OfflinePlayer player, double amount) {
        if(player.isOnline()) {
            BeastTokensAPI.getTokensManager().removeTokens(player.getPlayer(), amount);
        } else {
            BeastTokensAPI.getTokensManager().removeTokens(player, amount);
        }
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        return player.isOnline() ? BeastTokensAPI.getTokensManager().getTokens(player.getPlayer()) : BeastTokensAPI.getTokensManager().getTokens(player);
    }
}
