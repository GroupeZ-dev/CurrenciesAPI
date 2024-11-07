package fr.traqueur.currencies.providers;

import fr.traqueur.currencies.CurrencyProvider;
import me.bukkit.mTokens.Inkzzz.Tokens;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;

public class MTokenProvider implements CurrencyProvider {
    @Override
    public void deposit(OfflinePlayer player, BigDecimal amount) {
        if (player.isOnline()) {
            Tokens.getInstance().getAPI().giveTokens(player.getPlayer(), amount.intValue());
        }
    }

    @Override
    public void withdraw(OfflinePlayer player, BigDecimal amount) {
        if (player.isOnline()) {
            Tokens.getInstance().getAPI().takeTokens(player.getPlayer(), amount.intValue());
        }
    }

    @Override
    public BigDecimal getBalance(OfflinePlayer player) {
        return player.isOnline() ? BigDecimal.valueOf(Tokens.getInstance().getAPI().getTokens(player.getPlayer())) : BigDecimal.ZERO;
    }
}
