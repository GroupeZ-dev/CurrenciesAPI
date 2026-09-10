package fr.traqueur.currencies;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for currencies that are not one of the built in {@link Currencies} constants.
 *
 * <pre>{@code
 * CurrencyRegistry.register("my_gems", new MyGemsProvider());
 *
 * TransactionResult result = CurrencyRegistry.require("my_gems")
 *         .withdrawIfSufficient(playerId, BigDecimal.TEN, "Shop purchase");
 * if (result.isSuccess()) {
 *     // give the goods
 * }
 * }</pre>
 *
 * <p>A custom provider only has to implement {@code deposit}, {@code withdraw} and
 * {@code getBalance}. It inherits an emulated {@link CurrencyProvider#withdrawIfSufficient} which
 * is serialized inside this JVM. If the backend can refuse a withdrawal itself, override
 * {@code withdrawIfSufficient} and {@code hasNativeConditionalWithdraw} to get a real guarantee. If the
 * backend is safe to use off the main server thread, also override
 * {@link CurrencyProvider#requiresMainThread()} to return false.</p>
 */
public final class CurrencyRegistry {

    private static final Map<String, CurrencyProvider> PROVIDERS = new ConcurrentHashMap<String, CurrencyProvider>();

    private CurrencyRegistry() {
    }

    /**
     * Register a custom provider under a name.
     *
     * @param name     The name used to look the provider up. Case-insensitive.
     * @param provider The provider instance.
     * @throws IllegalArgumentException  if the name or the provider is null or the name is blank.
     * @throws IllegalStateException     if a different provider is already registered under this name.
     */
    public static void register(String name, CurrencyProvider provider) {
        String key = normalize(name);
        if (provider == null) {
            throw new IllegalArgumentException("The provider cannot be null.");
        }

        CurrencyProvider existing = PROVIDERS.putIfAbsent(key, provider);
        if (existing != null && existing != provider) {
            throw new IllegalStateException("A different provider is already registered for the currency " + name + ".");
        }
    }

    /**
     * Register a custom provider, replacing any provider already registered under this name.
     *
     * @param name     The name used to look the provider up. Case-insensitive.
     * @param provider The provider instance.
     * @return The provider that was previously registered, or null.
     */
    public static CurrencyProvider registerOrReplace(String name, CurrencyProvider provider) {
        String key = normalize(name);
        if (provider == null) {
            throw new IllegalArgumentException("The provider cannot be null.");
        }
        return PROVIDERS.put(key, provider);
    }

    /**
     * Remove a registered provider.
     *
     * @param name The name it was registered under.
     * @return The removed provider, or null when nothing was registered.
     */
    public static CurrencyProvider unregister(String name) {
        return PROVIDERS.remove(normalize(name));
    }

    /**
     * Look a registered provider up, failing when there is none.
     *
     * @param name The name it was registered under.
     * @return The provider, never null.
     * @throws IllegalStateException if nothing is registered under this name.
     */
    @NotNull
    public static CurrencyProvider require(String name) {
        CurrencyProvider provider = find(name);
        if (provider == null) {
            throw new IllegalStateException("No custom currency is registered under the name " + name
                    + ". Register one with CurrencyRegistry.register(name, provider) first.");
        }
        return provider;
    }

    /**
     * Look a registered provider up, returning null when there is none.
     *
     * @param name The name it was registered under.
     * @return The provider, or null.
     */
    @Nullable
    public static CurrencyProvider find(String name) {
        return PROVIDERS.get(normalize(name));
    }

    /**
     * @param name The name to look up.
     * @return True when a provider is registered under this name.
     */
    public static boolean isRegistered(String name) {
        return name != null && PROVIDERS.containsKey(normalize(name));
    }

    /**
     * @return The names of every registered custom currency. The returned set is a snapshot.
     */
    @NotNull
    public static Set<String> getRegisteredNames() {
        return Collections.unmodifiableSet(new java.util.HashSet<>(PROVIDERS.keySet()));
    }

    /**
     * Debit a registered custom currency, but only if the funds are available.
     *
     * @param name     The name the provider was registered under.
     * @param playerId The UUID of the player to debit.
     * @param amount   The amount to debit, must be strictly positive.
     * @param reason   The reason of the withdrawal.
     * @return The outcome. Nothing is debited unless the status is
     * {@link TransactionResult.Status#SUCCESS}.
     */
    public static TransactionResult withdrawIfSufficient(String name, UUID playerId, BigDecimal amount, String reason) {
        return require(name).withdrawIfSufficient(playerId, amount, reason);
    }

    /**
     * Asynchronous variant of {@link #withdrawIfSufficient(String, UUID, BigDecimal, String)}.
     *
     * @param name     The name the provider was registered under.
     * @param playerId The UUID of the player to debit.
     * @param amount   The amount to debit, must be strictly positive.
     * @param reason   The reason of the withdrawal.
     * @return A future completed with the outcome.
     */
    public static CompletableFuture<TransactionResult> withdrawIfSufficientAsync(String name, UUID playerId, BigDecimal amount, String reason) {
        return require(name).withdrawIfSufficientAsync(playerId, amount, reason);
    }

    private static String normalize(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("The currency name cannot be null or blank.");
        }
        return name.trim().toLowerCase(java.util.Locale.ROOT);
    }
}
