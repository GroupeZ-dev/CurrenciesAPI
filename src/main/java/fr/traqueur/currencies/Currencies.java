package fr.traqueur.currencies;

import fr.traqueur.currencies.providers.BeastTokenProvider;
import fr.traqueur.currencies.providers.CoinsEngineProvider;
import fr.traqueur.currencies.providers.EcoBitProvider;
import fr.traqueur.currencies.providers.ElementalGemsProvider;
import fr.traqueur.currencies.providers.ElementalTokensProvider;
import fr.traqueur.currencies.providers.ExperienceProvider;
import fr.traqueur.currencies.providers.ItemProvider;
import fr.traqueur.currencies.providers.LevelProvider;
import fr.traqueur.currencies.providers.MTokenProvider;
import fr.traqueur.currencies.providers.OptEcoProvider;
import fr.traqueur.currencies.providers.PlayerPointsProvider;
import fr.traqueur.currencies.providers.VaultProvider;
import fr.traqueur.currencies.providers.ZEssentialsProvider;
import fr.traqueur.currencies.providers.ZMenuItemProvider;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;

public enum Currencies {

    BEASTTOKENS("BeastTokens", BeastTokenProvider.class),
    VAULT("Vault", VaultProvider.class),
    MTOKENS("MySQL-Tokens", MTokenProvider.class),
    PLAYERPOINTS("PlayerPoints", PlayerPointsProvider.class),
    OPTECO("OptEco", OptEcoProvider.class),
    ELEMENTALTOKENS("ElementalTokens", ElementalTokensProvider.class),
    ELEMENTALGEMS("ElementalGems", ElementalGemsProvider.class),
    ITEM("self", ItemProvider.class),
    LEVEL("self", LevelProvider.class),
    EXPERIENCE("self", ExperienceProvider.class),
    ZESSENTIALS("zEssentials", ZEssentialsProvider.class),
    ZMENUITEMS("zMenu", ZMenuItemProvider.class),
    ECOBITS("EcoBits", EcoBitProvider.class),
    COINSENGINE("CoinsEngine", CoinsEngineProvider.class);

    static {
        Updater.checkUpdates();
    }

    private final String name;
    private final Class<? extends CurrencyProvider> providerClass;
    private CurrencyProvider provider;

    Currencies(String name, Class<? extends CurrencyProvider> providerClass) {
        this.name = name;
        this.providerClass = providerClass;
    }

    public void createProvider(Object... objects) {
        if (this.provider == null) {
            Constructor<?> constructor = this.providerClass.getConstructors()[0];
            try {
                this.provider = (CurrencyProvider) constructor.newInstance(objects);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isDisable() {

        if (this.name.equalsIgnoreCase("self")) {
            createProvider();
            return false;
        }

        boolean isDisable = !Bukkit.getPluginManager().isPluginEnabled(this.name);
        if (!isDisable && this.provider == null) {
            createProvider();
        }
        return isDisable;
    }

    private void createProvider() {
        if (this.provider == null) {
            try {
                this.provider = this.providerClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public void deposit(OfflinePlayer player, BigDecimal amount) {
        if (this.isDisable()) {
            throw new IllegalStateException("The plugin " + this.name + " is not enable.");
        }
        this.provider.deposit(player, amount);
    }

    public void withdraw(OfflinePlayer player, BigDecimal amount) {
        if (this.isDisable()) {
            throw new IllegalStateException("The plugin " + this.name + " is not enable.");
        }
        this.provider.withdraw(player, amount);
    }

    public BigDecimal getBalance(OfflinePlayer player) {
        if (this.isDisable()) {
            throw new IllegalStateException("The plugin " + this.name + " is not enable.");
        }
        return this.provider.getBalance(player);
    }

}
