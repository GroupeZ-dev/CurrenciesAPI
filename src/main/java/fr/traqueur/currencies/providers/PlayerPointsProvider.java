package fr.traqueur.currencies.providers;

import fr.traqueur.currencies.CurrencyProvider;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerPointsProvider implements CurrencyProvider {

    private PlayerPointsAPI playerPointsAPI;

    private PlayerPointsAPI getAPI() {
        if(this.playerPointsAPI == null) {
            PlayerPoints playerPoints = JavaPlugin.getPlugin(PlayerPoints.class);
            this.playerPointsAPI = playerPoints.getAPI();
        }

        return this.playerPointsAPI;
    }

    @Override
    public void deposit(OfflinePlayer player, double amount) {
        this.getAPI().give(player.getUniqueId(), (int) amount);
    }

    @Override
    public void withdraw(OfflinePlayer player, double amount) {
        this.getAPI().take(player.getUniqueId(), (int) amount);
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        return this.getAPI().look(player.getUniqueId());
    }
}
