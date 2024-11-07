package fr.traqueur.currencies.providers;

import fr.traqueur.currencies.CurrencyProvider;
import me.elementalgaming.ElementalTokens.TokenAPI;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;

public class ElementalTokensProvider implements CurrencyProvider {
    @Override
    public void deposit(OfflinePlayer player, BigDecimal amount) {
        TokenAPI.addTokens(player.getUniqueId(), amount.longValue());
    }

    @Override
    public void withdraw(OfflinePlayer player, BigDecimal amount) {
        TokenAPI.removeTokens(player.getUniqueId(), amount.longValue());
    }

    @Override
    public BigDecimal getBalance(OfflinePlayer player) {
        return BigDecimal.valueOf(TokenAPI.getTokens(player.getUniqueId()));
    }
}
