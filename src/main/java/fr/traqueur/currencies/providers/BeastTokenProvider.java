package fr.traqueur.currencies.providers;

import fr.traqueur.currencies.CurrencyProvider;
import me.mraxetv.beasttokens.api.BeastTokensAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;
import java.util.UUID;

public class BeastTokenProvider implements CurrencyProvider {
    @Override
    public void deposit(UUID playerId, BigDecimal amount, String reason) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerId);
        if (offlinePlayer.isOnline()) {
            BeastTokensAPI.getTokensManager().addTokens(offlinePlayer.getPlayer(), amount.doubleValue());
        } else {
            BeastTokensAPI.getTokensManager().addTokens(offlinePlayer, amount.doubleValue());
        }
    }

    @Override
    public void withdraw(UUID playerId, BigDecimal amount, String reason) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerId);
        if (offlinePlayer.isOnline()) {
            BeastTokensAPI.getTokensManager().removeTokens(offlinePlayer.getPlayer(), amount.doubleValue());
        } else {
            BeastTokensAPI.getTokensManager().removeTokens(offlinePlayer, amount.doubleValue());
        }
    }

    @Override
    public BigDecimal getBalance(UUID playerId) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerId);
        return BigDecimal.valueOf(offlinePlayer.isOnline() ? BeastTokensAPI.getTokensManager().getTokens(offlinePlayer.getPlayer()) : BeastTokensAPI.getTokensManager().getTokens(offlinePlayer));
    }
}
