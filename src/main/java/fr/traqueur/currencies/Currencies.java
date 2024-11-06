package fr.traqueur.currencies;

import fr.traqueur.currencies.providers.*;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public enum Currencies {

    BEASTTOKENS("BeastTokens", BeastTokenProvider.class),
    VAULT("Vault", VaultProvider.class),
    MTOKENS("MySQL-Tokens", MTokenProvider.class),
    PLAYERPOINTS("PlayerPoints", PlayerPointsProvider.class),
    OPTECO("OptEco", OptEcoProvider.class)
    ;

    private final String name;
    private final Class<? extends CurrencyProvider> providerClass;
    private CurrencyProvider provider;

    Currencies(String name, Class<? extends CurrencyProvider> providerClass) {
        this.name = name;
        this.providerClass = providerClass;
    }

    private boolean isDisable() {
        boolean isDisable = !Bukkit.getPluginManager().isPluginEnabled(this.name);
        if(!isDisable && this.provider == null) {
            try {
                this.provider = this.providerClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return isDisable;
    }

    public void deposit(OfflinePlayer player, double amount) {
        if(this.isDisable()) {
            throw new IllegalStateException("The plugin " + this.name + " is not enable.");
        }
        this.provider.deposit(player, amount);
    }

    public void withdraw(OfflinePlayer player, double amount) {
        if(this.isDisable()) {
            throw new IllegalStateException("The plugin " + this.name + " is not enable.");
        }
        this.provider.withdraw(player, amount);
    }

    public double getBalance(OfflinePlayer player) {
        if(this.isDisable()) {
            throw new IllegalStateException("The plugin " + this.name + " is not enable.");
        }
        return this.provider.getBalance(player);
    }

}
