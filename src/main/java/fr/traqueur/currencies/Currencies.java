package fr.traqueur.currencies;

import fr.traqueur.currencies.providers.*;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

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
    REDISECONOMY("RedisEconomy", RedisEconomyProvider.class, true, true),
    /**
     * The currency RoyaleEconomy from the plugin RoyaleEconomy.
     */
    ROYALEECONOMY("RoyaleEconomy", RoyaleEconomyProvider.class, true, true),
    /**
     * The currency ExcellentEconomy from the plugin ExcellentEconomy (new name for CraftEngine)
     */
    EXCELLENTECONOMY("ExcellentEconomy", ExcellentEconomyProvider.class, true, true),
    /**
     * @deprecated Use {@link #EXCELLENTECONOMY} instead. Kept so old code and configs
     *             referencing {@code EXCELLENTEECONOMY} keep working. Will be removed
     *             in a future version.
     */
    @Deprecated
    EXCELLENTEECONOMY("ExcellentEconomy", ExcellentEconomyProvider.class, true, true, EXCELLENTECONOMY)
    ;

    final static String DEFAULT_CURRENCY_NAME = "default";
    private final static String DEFAULT_REASON = "No reason";

    static {
        Updater.checkUpdates();
    }

    private final String name;
    private final Class<? extends CurrencyProvider> providerClass;
    private final boolean autoCreate;
    private final boolean currencySpecific;
    private final Map<String, CurrencyProvider> providers;
    private final Currencies renamedTo;
    private static final Set<String> WARNED_DEPRECATED = new HashSet<>();

    Currencies(String name, Class<? extends CurrencyProvider> providerClass) {
        this(name, providerClass, true, false);
    }

    Currencies(String name, Class<? extends CurrencyProvider> providerClass, boolean autoCreate) {
        this(name, providerClass, autoCreate, false);
    }

    Currencies(String name, Class<? extends CurrencyProvider> providerClass, boolean autoCreate, boolean currencySpecific) {
        this(name, providerClass, autoCreate, currencySpecific, null);
    }

    Currencies(String name, Class<? extends CurrencyProvider> providerClass, boolean autoCreate, boolean currencySpecific, Currencies renamedTo) {
        this.name = name;
        this.providerClass = providerClass;
        this.autoCreate = autoCreate;
        this.providers = new HashMap<>();
        this.currencySpecific = currencySpecific;
        this.renamedTo = renamedTo;
    }

    /**
     * Resolve a currency by its name, redirecting deprecated aliases to their canonical
     * constant while logging a one-time warning.
     *
     * @param name The name of the currency (e.g. {@code "VAULT"}, {@code "EXCELLENTECONOMY"}).
     * @return The canonical currency.
     * @throws IllegalArgumentException if the name is unknown.
     */
    public static Currencies fromName(String name) {
        Currencies currency = Currencies.valueOf(name);
        if (currency.renamedTo != null) {
            if (WARNED_DEPRECATED.add(name)) {
                Bukkit.getLogger().warning("The currency name '" + name + "' is deprecated, use '" + currency.renamedTo.name() + "' instead.");
            }
            return currency.renamedTo;
        }
        return currency;
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
                Constructor<?> constructor = providerClass.getConstructor(Arrays.stream(objects).map(Object::getClass).toArray(Class[]::new));
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
     * @param playerId The UUID of the player to add the money.
     * @param amount   The amount of money to add.
     * @param reason   The reason of the deposit.
     */
    public void deposit(UUID playerId, BigDecimal amount, String reason) {
        this.deposit(playerId, amount, DEFAULT_CURRENCY_NAME, reason);
    }

    /**
     * Remove some money from a player.
     *
     * @param playerId The UUID of the player to remove the money.
     * @param amount   The amount of money to remove.
     * @param reason   The reason of the withdrawal.
     */
    public void withdraw(UUID playerId, BigDecimal amount, String reason) {
        this.withdraw(playerId, amount, DEFAULT_CURRENCY_NAME, reason);
    }

    /**
     * Add some money to a player.
     *
     * @param playerId The UUID of the player to add the money.
     * @param amount   The amount of money to add.
     */
    public void deposit(UUID playerId, BigDecimal amount) {
        this.deposit(playerId, amount, DEFAULT_CURRENCY_NAME, DEFAULT_REASON);
    }

    /**
     * Remove some money from a player.
     *
     * @param playerId The UUID of the player to remove the money.
     * @param amount   The amount of money to remove.
     */
    public void withdraw(UUID playerId, BigDecimal amount) {
        this.withdraw(playerId, amount, DEFAULT_CURRENCY_NAME, DEFAULT_REASON);
    }

    /**
     * Get the balance of a player.
     *
     * @param playerId The UUID of the player to get the balance.
     * @return The balance of the player.
     */
    public BigDecimal getBalance(UUID playerId) {
        return this.getBalance(playerId, DEFAULT_CURRENCY_NAME);
    }

    /**
     * Add some money to a player.
     *
     * @param playerId     The UUID of the player to add the money.
     * @param amount       The amount of money to add.
     * @param currencyName The name of the currency.
     * @param reason       The reason of the deposit.
     */
    public void deposit(UUID playerId, BigDecimal amount, String currencyName, String reason) {
        this.canBeUse(currencyName);
        this.providers.get(currencyName).deposit(playerId, amount, reason);
    }

    /**
     * Remove some money from a player.
     *
     * @param playerId     The UUID of the player to remove the money.
     * @param amount       The amount of money to remove.
     * @param currencyName The name of the currency.
     * @param reason       The reason of the withdrawal.
     */
    public void withdraw(UUID playerId, BigDecimal amount, String currencyName, String reason) {
        this.canBeUse(currencyName);
        this.providers.get(currencyName).withdraw(playerId, amount, reason);
    }

    /**
     * Get the balance of a player.
     *
     * @param playerId     The UUID of the player to get the balance.
     * @param currencyName The name of the currency.
     * @return The balance of the player.
     */
    public BigDecimal getBalance(UUID playerId, String currencyName) {
        this.canBeUse(currencyName);
        return this.providers.get(currencyName).getBalance(playerId);
    }

    /**
     * Remove some money from a player, but only if the player can actually afford it.
     *
     * <p>This is the operation to use for a purchase. Unlike calling {@link #getBalance} and then
     * {@link #withdraw}, nothing can slip in between the check and the debit.</p>
     *
     * @param playerId The UUID of the player to debit.
     * @param amount   The amount to debit, must be strictly positive.
     * @param reason   The reason of the withdrawal.
     * @return The outcome. Nothing is debited unless the status is
     * {@link TransactionResult.Status#SUCCESS}.
     */
    @NotNull
    public TransactionResult withdrawIfSufficient(@NotNull UUID playerId, @NotNull BigDecimal amount, @Nullable String reason) {
        return this.withdrawIfSufficient(playerId, amount, DEFAULT_CURRENCY_NAME, reason);
    }

    /**
     * Remove some money from a player, but only if the player can actually afford it.
     *
     * @param playerId     The UUID of the player to debit.
     * @param amount       The amount to debit, must be strictly positive.
     * @param currencyName The name of the currency.
     * @param reason       The reason of the withdrawal.
     * @return The outcome. Nothing is debited unless the status is
     * {@link TransactionResult.Status#SUCCESS}.
     */
    @NotNull
    public TransactionResult withdrawIfSufficient(@NotNull UUID playerId, @NotNull BigDecimal amount, @NotNull String currencyName, @Nullable String reason) {
        this.canBeUse(currencyName);
        return this.providers.get(currencyName).withdrawIfSufficient(playerId, amount, reason);
    }

    /**
     * Asynchronous variant of {@link #withdrawIfSufficient(UUID, BigDecimal, String, String)}.
     *
     * @param playerId     The UUID of the player to debit.
     * @param amount       The amount to debit, must be strictly positive.
     * @param currencyName The name of the currency.
     * @param reason       The reason of the withdrawal.
     * @return A future completed with the outcome.
     */
    @NotNull
    public CompletableFuture<TransactionResult> withdrawIfSufficientAsync(@NotNull UUID playerId, @NotNull BigDecimal amount, @NotNull String currencyName, @Nullable String reason) {
        this.canBeUse(currencyName);
        return this.providers.get(currencyName).withdrawIfSufficientAsync(playerId, amount, reason);
    }

    /**
     * Returns the provider backing this currency, creating it if necessary.
     *
     * <p>Useful for inspecting a provider's capabilities, and the point
     * {@link CurrencyRegistry#resolve(String, String)} bridges to so that a built-in currency and a
     * custom one can be looked up the same way.</p>
     *
     * @param currencyName The name of the currency.
     * @return The provider.
     */
    @NotNull
    public CurrencyProvider getProvider(@NotNull String currencyName) {
        this.canBeUse(currencyName);
        return this.providers.get(currencyName);
    }

    @NotNull
    public Guarantee getWithdrawGuarantee(@NotNull String currencyName) {
        this.canBeUse(currencyName);
        return this.providers.get(currencyName).getWithdrawGuarantee();
    }

    private void canBeUse(String currencyName) {
        if (this.isDisable()) {
            throw new IllegalStateException("The plugin " + this.name + " is not enable.");
        }
        if (this.autoCreate) {

            if (this.currencySpecific) {
                this.registerProvider(currencyName, currencyName);
            } else {
                this.registerProvider(currencyName);
            }
        } else if (!this.providers.containsKey(currencyName)) {
            String currency = this.name.equalsIgnoreCase(DEFAULT_CURRENCY_NAME) ? "" : " and for the currency " + name;
            throw new IllegalStateException("You must create the provider for the plugin " + this.name + currency + " before using it.");
        }
    }
}
