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
    public void deposit(OfflinePlayer offlinePlayer, BigDecimal amount) {
        this.getApi().addPoints(offlinePlayer.getUniqueId(), amount.doubleValue());
    }

    @Override
    public void withdraw(OfflinePlayer offlinePlayer, BigDecimal amount) {
        this.getApi().takePoints(offlinePlayer.getUniqueId(), amount.doubleValue());
    }

    @Override
    public BigDecimal getBalance(OfflinePlayer offlinePlayer) {
        return BigDecimal.valueOf(this.getApi().getPoints(offlinePlayer.getUniqueId()));
    }
}
