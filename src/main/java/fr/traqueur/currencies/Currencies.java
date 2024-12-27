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
import fr.traqueur.currencies.providers.RedisEconomyProvider;
import fr.traqueur.currencies.providers.VaultProvider;
import fr.traqueur.currencies.providers.VotingProvider;
import fr.traqueur.currencies.providers.ZEssentialsProvider;
import fr.traqueur.currencies.providers.ZMenuItemProvider;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

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
    ITEM("self", ItemProvider.class, false),
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
    ZESSENTIALS("zEssentials", ZEssentialsProvider.class, true, true),
    /**
     * The currency zMenuItems from the plugin zMenu.
     */
    ZMENUITEMS("zMenu", ZMenuItemProvider.class, false),
    /**
     * The currency EcoBits from the plugin EcoBits.
     */
    ECOBITS("EcoBits", EcoBitProvider.class, true, true),
    /**
     * The currency CoinsEngine from the plugin CoinsEngine.
     */
    COINSENGINE("CoinsEngine", CoinsEngineProvider.class, true, true),
    /**
     * The currency VotingPlugin from the plugin VotingPlugin.
     */
    VOTINGPLUGIN("VotingPlugin", VotingProvider.class),
    /**
     * The currency RedisEconomy from the plugin RedisEconomy.
     */
    REDISECONOMY("RedisEconomy", RedisEconomyProvider.class);

    static {
        Updater.checkUpdates();
    }

    private final String name;
    private final Class<? extends CurrencyProvider> providerClass;
    private final boolean autoCreate;
    private final boolean currencySpecific;
    private final Map<String, CurrencyProvider> providers;

    Currencies(String name, Class<? extends CurrencyProvider> providerClass) {
        this(name, providerClass, true, false);
    }

    Currencies(String name, Class<? extends CurrencyProvider> providerClass, boolean autoCreate) {
        this(name, providerClass, autoCreate, false);
    }

    Currencies(String name, Class<? extends CurrencyProvider> providerClass, boolean autoCreate, boolean currencySpecific) {
        this.name = name;
        this.providerClass = providerClass;
        this.autoCreate = autoCreate;
        this.providers = new HashMap<>();
        this.currencySpecific = currencySpecific;
    }

    /**
     * Create a new instance of the currency provider.
     *
     * @param objects The objects to pass to the constructor of the provider.
     */
    public void registerProvider(String name, Object... objects) {
        if (this.providers.containsKey(name)) {
            return;
        }
        CurrencyProvider provider = this.createProvider(objects);
        this.providers.put(name, provider);
    }

    public CurrencyProvider createProvider(Object... objects) {
        CurrencyProvider provider;
        try {
            if (objects.length == 0) {
                provider = this.providerClass.newInstance();
            } else {
                Constructor<?> constructor = this.providerClass.getConstructors()[0];
                provider = (CurrencyProvider) constructor.newInstance(objects);
            }
        } catch (Exception e) {
            throw new RuntimeException("Cannot create the provider for the plugin " + this.name + ".", e);
        }
        return provider;
    }

    /**
     * Check if the plugin is not enabled.
     *
     * @return True if the plugin is not enabled, false otherwise.
     */
    private boolean isDisable() {

        if (this.name.equalsIgnoreCase("self")) {
            return false;
        }

        return !Bukkit.getPluginManager().isPluginEnabled(this.name);
    }

    /**
     * Add some money to a player.
     *
     * @param player The player to add the money.
     * @param amount The amount of money to add.
     * @param reason The reason of the deposit.
     */
    public void deposit(OfflinePlayer player, BigDecimal amount, String reason) {
        this.deposit(player, amount, "default", reason);
    }

    /**
     * Remove some money from a player.
     *
     * @param player The player to remove the money.
     * @param amount The amount of money to remove.
     * @param reason The reason of the withdrawal.
     */
    public void withdraw(OfflinePlayer player, BigDecimal amount, String reason) {
        this.withdraw(player, amount, "default", reason);
    }

    /**
     * Add some money to a player.
     *
     * @param player The player to add the money.
     * @param amount The amount of money to add.
     */
    public void deposit(OfflinePlayer player, BigDecimal amount) {
        this.deposit(player, amount, "default", "No reason");
    }

    /**
     * Remove some money from a player.
     *
     * @param player The player to remove the money.
     * @param amount The amount of money to remove.
     */
    public void withdraw(OfflinePlayer player, BigDecimal amount) {
        this.withdraw(player, amount, "default", "No reason");
    }

    /**
     * Get the balance of a player.
     *
     * @param player The player to get the balance.
     * @return The balance of the player.
     */
    public BigDecimal getBalance(OfflinePlayer player) {
        return getBalance(player, "default");
    }

    /**
     * Add some money to a player.
     *
     * @param player The player to add the money.
     * @param amount The amount of money to add.
     * @param reason The reason of the deposit.
     */
    public void deposit(OfflinePlayer player, BigDecimal amount, String currencyName, String reason) {
        this.canBeUse(currencyName);
        this.providers.get(currencyName).deposit(player, amount, reason);
    }

    /**
     * Remove some money from a player.
     *
     * @param player The player to remove the money.
     * @param amount The amount of money to remove.
     * @param reason The reason of the withdrawal.
     */
    public void withdraw(OfflinePlayer player, BigDecimal amount, String currencyName, String reason) {
        this.canBeUse(currencyName);
        this.providers.get(currencyName).withdraw(player, amount, reason);
    }

    /**
     * Get the balance of a player.
     *
     * @param player The player to get the balance.
     * @return The balance of the player.
     */
    public BigDecimal getBalance(OfflinePlayer player, String currencyName) {
        this.canBeUse(currencyName);
        return this.providers.get(currencyName).getBalance(player);
    }

    private void canBeUse(String currencyName) {
        if (this.isDisable()) {
            throw new IllegalStateException("The plugin " + this.name + " is not enable.");
        }
        if (autoCreate) {

            if (currencySpecific) {
                registerProvider(currencyName, currencyName);
            } else {
                registerProvider(currencyName);
            }
        } else if (!this.providers.containsKey(currencyName)) {
            String currency = name.equalsIgnoreCase("default") ? "" : " and for the currency " + name;
            throw new IllegalStateException("You must create the provider for the plugin " + this.name + currency + " before using it.");
        }
    }
}