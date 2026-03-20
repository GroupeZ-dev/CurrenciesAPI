package fr.traqueur.currencies.providers;

import fr.traqueur.currencies.CurrencyProvider;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.plugin.java.JavaPlugin;

import java.math.BigDecimal;
import java.util.UUID;

public class PlayerPointsProvider implements CurrencyProvider {

    private PlayerPointsAPI playerPointsAPI;

    private PlayerPointsAPI getAPI() {
        if (this.playerPointsAPI == null) {
            PlayerPoints playerPoints = JavaPlugin.getPlugin(PlayerPoints.class);
            this.playerPointsAPI = playerPoints.getAPI();
        }

        return this.playerPointsAPI;
    }

    @Override
    public void deposit(UUID playerId, BigDecimal amount, String reason) {
        this.getAPI().give(playerId, amount.intValue());
    }

    @Override
    public void withdraw(UUID playerId, BigDecimal amount, String reason) {
        this.getAPI().take(playerId, amount.intValue());
    }

    @Override
    public BigDecimal getBalance(UUID playerId) {
        return BigDecimal.valueOf(this.getAPI().look(playerId));
    }
}
