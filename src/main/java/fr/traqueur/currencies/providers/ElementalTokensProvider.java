package fr.traqueur.currencies.providers;

import fr.traqueur.currencies.CurrencyProvider;
import me.elementalgaming.ElementalTokens.TokenAPI;

import java.math.BigDecimal;
import java.util.UUID;

public class ElementalTokensProvider implements CurrencyProvider {
    @Override
    public void deposit(UUID playerId, BigDecimal amount, String reason) {
        TokenAPI.addTokens(playerId, amount.longValue());
    }

    @Override
    public void withdraw(UUID playerId, BigDecimal amount, String reason) {
        TokenAPI.removeTokens(playerId, amount.longValue());
    }

    @Override
    public BigDecimal getBalance(UUID playerId) {
        return BigDecimal.valueOf(TokenAPI.getTokens(playerId));
    }
}
