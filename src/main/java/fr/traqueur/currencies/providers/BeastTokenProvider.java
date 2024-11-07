package fr.traqueur.currencies.providers;

import fr.traqueur.currencies.CurrencyProvider;
import me.mraxetv.beasttokens.api.BeastTokensAPI;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;

public class BeastTokenProvider implements CurrencyProvider {
    @Override
    public void deposit(OfflinePlayer offlinePlayer, BigDecimal amount) {
        if (offlinePlayer.isOnline()) {
            BeastTokensAPI.getTokensManager().addTokens(offlinePlayer.getPlayer(), amount.doubleValue());
        } else {
            BeastTokensAPI.getTokensManager().addTokens(offlinePlayer, amount.doubleValue());
        }
    }

    @Override
    public void withdraw(OfflinePlayer offlinePlayer, BigDecimal amount) {
        if (offlinePlayer.isOnline()) {
            BeastTokensAPI.getTokensManager().removeTokens(offlinePlayer.getPlayer(), amount.doubleValue());
        } else {
            BeastTokensAPI.getTokensManager().removeTokens(offlinePlayer, amount.doubleValue());
        }
    }

    @Override
    public BigDecimal getBalance(OfflinePlayer offlinePlayer) {
        return BigDecimal.valueOf(offlinePlayer.isOnline() ? BeastTokensAPI.getTokensManager().getTokens(offlinePlayer.getPlayer()) : BeastTokensAPI.getTokensManager().getTokens(offlinePlayer));
    }
}
