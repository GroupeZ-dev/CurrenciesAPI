package fr.traqueur.currencies.providers;

import fr.traqueur.currencies.CurrencyProvider;
import me.playernguyen.opteco.OptEco;
import me.playernguyen.opteco.api.OptEcoAPI;
import me.playernguyen.opteco.api.OptEcoAPIAbstract;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;

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
    public void deposit(OfflinePlayer player, BigDecimal amount) {
        this.api.addPoints(player.getUniqueId(), amount.doubleValue());
    }

    @Override
    public void withdraw(OfflinePlayer player, BigDecimal amount) {
        this.api.takePoints(player.getUniqueId(), amount.doubleValue());
    }

    @Override
    public BigDecimal getBalance(OfflinePlayer player) {
        return BigDecimal.valueOf(this.api.getPoints(player.getUniqueId()));
    }
}
