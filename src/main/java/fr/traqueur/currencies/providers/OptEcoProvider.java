package fr.traqueur.currencies.providers;

import fr.traqueur.currencies.CurrencyProvider;
import me.playernguyen.opteco.OptEco;
import me.playernguyen.opteco.api.OptEcoAPI;
import me.playernguyen.opteco.api.OptEcoAPIAbstract;
import org.bukkit.OfflinePlayer;

public class OptEcoProvider implements CurrencyProvider {

    private OptEcoAPI api;

    private OptEcoAPI getApi() {
        if (this.api == null) {
            OptEco plugin = OptEco.getInstance();
            this.api = new OptEcoAPIAbstract(plugin);
        }
        return this.api;
    }

    @Override
    public void deposit(OfflinePlayer player, double amount) {
        this.api.addPoints(player.getUniqueId(), amount);
    }

    @Override
    public void withdraw(OfflinePlayer player, double amount) {
        this.api.takePoints(player.getUniqueId(), amount);
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        return this.api.getPoints(player.getUniqueId());
    }
}
