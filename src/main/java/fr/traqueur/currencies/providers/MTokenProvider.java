package fr.traqueur.currencies.providers;

import fr.traqueur.currencies.CurrencyProvider;
import me.bukkit.mTokens.Inkzzz.Tokens;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;

public class MTokenProvider implements CurrencyProvider {
    @Override
    public void deposit(OfflinePlayer offlinePlayer, BigDecimal amount) {
        if (offlinePlayer.isOnline()) {
            Tokens.getInstance().getAPI().giveTokens(offlinePlayer.getPlayer(), amount.intValue());
        }
    }

    @Override
    public void withdraw(OfflinePlayer offlinePlayer, BigDecimal amount) {
        if (offlinePlayer.isOnline()) {
            Tokens.getInstance().getAPI().takeTokens(offlinePlayer.getPlayer(), amount.intValue());
        }
    }

    @Override
    public BigDecimal getBalance(OfflinePlayer offlinePlayer) {
        return offlinePlayer.isOnline() ? BigDecimal.valueOf(Tokens.getInstance().getAPI().getTokens(offlinePlayer.getPlayer())) : BigDecimal.ZERO;
    }
}
