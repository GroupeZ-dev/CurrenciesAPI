package fr.traqueur.currencies;

import fr.traqueur.currencies.providers.BeastTokenProvider;
import fr.traqueur.currencies.providers.CoinsEngineProvider;
import fr.traqueur.currencies.providers.EcoBitProvider;
import fr.traqueur.currencies.providers.ElementalGemsProvider;
import fr.traqueur.currencies.providers.ElementalTokensProvider;
import fr.traqueur.currencies.providers.ExperienceProvider;
import fr.traqueur.currencies.providers.ItemProvider;
import fr.traqueur.currencies.providers.LevelProvider;
import fr.traqueur.currencies.providers.PlayerPointsProvider;
import fr.traqueur.currencies.providers.VaultProvider;
import fr.traqueur.currencies.providers.VotingProvider;
import fr.traqueur.currencies.providers.ZEssentialsProvider;
import fr.traqueur.currencies.providers.ZMenuItemProvider;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;

/**
 * The list of all the currencies that can be used in the plugin.
 *
 * @author MatieuVER
 */
public enum Currencies {

    /**
     * The currency BeastTokens from the plugin BeastTokens.
     */
    BEASTTOKENS("BeastTokens", BeastTokenProvider.class),
    /**
     * The currency Vault from the plugin Vault.
     */
    VAULT("Vault", VaultProvider.class),
    /**
     * The currency PlayerPoints from the plugin PlayerPoints.
     */
    PLAYERPOINTS("PlayerPoints", PlayerPointsProvider.class),
    /**
     * The currency ElementalTokens from the plugin ElementalTokens.
     */
    ELEMENTALTOKENS("ElementalTokens", ElementalTokensProvider.class),
    /**
     * The currency ElementalGems from the plugin ElementalGems.
     */
    ELEMENTALGEMS("ElementalGems", ElementalGemsProvider.class),
    /**
     * The currency Item from the plugin itself.
     */
    ITEM("self", ItemProvider.class),
    /**
     * The currency Level from the plugin itself.
     */
    LEVEL("self", LevelProvider.class),
    /**
     * The currency Experience from the plugin itself.
     */
    EXPERIENCE("self", ExperienceProvider.class),
    /**
     * The currency zEssentials from the plugin zEssentials.
     */
    ZESSENTIALS("zEssentials", ZEssentialsProvider.class),
    /**
     * The currency zMenuItems from the plugin zMenu.
     */
    ZMENUITEMS("zMenu", ZMenuItemProvider.class),
    /**
     * The currency EcoBits from the plugin EcoBits.
     */
    ECOBITS("EcoBits", EcoBitProvider.class),
    /**
     * The currency CoinsEngine from the plugin CoinsEngine.
     */
    COINSENGINE("CoinsEngine", CoinsEngineProvider.class),
    /**
     * The currency VotingPlugin from the plugin VotingPlugin.
     */
    VOTINGPLUGIN("VotingPlugin", VotingProvider.class);

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

    /**
     * Create a new instance of the currency provider.
     *
     * @param objects The objects to pass to the constructor of the provider.
     */
    public void createProvider(Object... objects) {
        if (this.provider == null) {

            if (objects.length == 0) {
                try {
                    this.provider = this.providerClass.newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

            Constructor<?> constructor = this.providerClass.getConstructors()[0];
            try {
                this.provider = (CurrencyProvider) constructor.newInstance(objects);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Check if the plugin is not enabled.
     *
     * @return True if the plugin is not enabled, false otherwise.
     */
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

    /**
     * Add some money to a player.
     *
     * @param player The player to add the money.
     * @param amount The amount of money to add.
     */
    public void deposit(OfflinePlayer player, BigDecimal amount) {
        if (this.isDisable()) {
            throw new IllegalStateException("The plugin " + this.name + " is not enable.");
        }
        this.provider.deposit(player, amount);
    }

    /**
     * Remove some money from a player.
     *
     * @param player The player to remove the money.
     * @param amount The amount of money to remove.
     */
    public void withdraw(OfflinePlayer player, BigDecimal amount) {
        if (this.isDisable()) {
            throw new IllegalStateException("The plugin " + this.name + " is not enable.");
        }
        this.provider.withdraw(player, amount);
    }

    /**
     * Get the balance of a player.
     *
     * @param player The player to get the balance.
     * @return The balance of the player.
     */
    public BigDecimal getBalance(OfflinePlayer player) {
        if (this.isDisable()) {
            throw new IllegalStateException("The plugin " + this.name + " is not enable.");
        }
        return this.provider.getBalance(player);
    }

}