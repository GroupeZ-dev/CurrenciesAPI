package fr.traqueur.currencies.providers;

import fr.traqueur.currencies.CurrencyProvider;
import me.elementalgaming.ElementalTokens.TokenAPI;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;

public class ElementalTokensProvider implements CurrencyProvider {
    @Override
    public void deposit(OfflinePlayer offlinePlayer, BigDecimal amount) {
        TokenAPI.addTokens(offlinePlayer.getUniqueId(), amount.longValue());
    }

    @Override
    public void withdraw(OfflinePlayer offlinePlayer, BigDecimal amount) {
        TokenAPI.removeTokens(offlinePlayer.getUniqueId(), amount.longValue());
    }

    @Override
    public BigDecimal getBalance(OfflinePlayer offlinePlayer) {
        return BigDecimal.valueOf(TokenAPI.getTokens(offlinePlayer.getUniqueId()));
    }
}
