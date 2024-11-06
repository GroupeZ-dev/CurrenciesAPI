package fr.traqueur.currencies.providers;

import fr.traqueur.currencies.CurrencyProvider;
import me.bukkit.mTokens.Inkzzz.Tokens;
import org.bukkit.OfflinePlayer;

public class MTokenProvider implements CurrencyProvider {
    @Override
    public void deposit(OfflinePlayer player, double amount) {
        if(player.isOnline()) {
            Tokens.getInstance().getAPI().giveTokens(player.getPlayer(), (int) amount);
        }
    }

    @Override
    public void withdraw(OfflinePlayer player, double amount) {
        if(player.isOnline()) {
            Tokens.getInstance().getAPI().takeTokens(player.getPlayer(), (int) amount);
        }
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        return player.isOnline() ? Tokens.getInstance().getAPI().getTokens(player.getPlayer()) : 0;
    }
}
