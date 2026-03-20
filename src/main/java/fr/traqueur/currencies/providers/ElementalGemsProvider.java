package fr.traqueur.currencies.providers;

import fr.traqueur.currencies.CurrencyProvider;
import me.elementalgaming.ElementalGems.GemAPI;

import java.math.BigDecimal;
import java.util.UUID;

public class ElementalGemsProvider implements CurrencyProvider {
    @Override
    public void deposit(UUID playerId, BigDecimal amount, String reason) {
        GemAPI.addGems(playerId, amount.doubleValue());
    }

    @Override
    public void withdraw(UUID playerId, BigDecimal amount, String reason) {
        GemAPI.removeGems(playerId, amount.doubleValue());
    }

    @Override
    public BigDecimal getBalance(UUID playerId) {
        return BigDecimal.valueOf(GemAPI.getGems(playerId));
    }
}
