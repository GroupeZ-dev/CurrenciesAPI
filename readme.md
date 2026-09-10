# Currencies Library Usage Guide

The CurrenciesAPI is a library that provides an easy way to interact with multiple in-game currency providers within a Bukkit/Spigot Minecraft plugin environment. The `Currencies` class is an enum that facilitates interaction with multiple currency providers within a Bukkit/Spigot Minecraft plugin environment.

## Overview

The `Currencies` enum allows easy management of various in-game currencies like Vault, PlayerPoints, EcoBits, and others. It provides methods to deposit, withdraw, and check balances for each of these currencies.

### Supported Currency Providers

- [BEASTTOKENS](https://www.spigotmc.org/resources/13409/) - `BEASTTOKENS`
- [Vault](https://www.spigotmc.org/resources/34315/) - `VAULT`
- [PlayerPoints](https://www.spigotmc.org/resources/80745/) - `PLAYERPOINTS`
- [ElementalTokens](https://builtbybit.com/resources/16707/) - `ELEMENTALTOKENS`
- [ElementalGems](https://builtbybit.com/resources/14920/) - `ELEMENTALGEMS`
- [Item](https://www.minecraft.net) - `ITEM`
- [Level](https://www.minecraft.net) - `LEVEL`
- [Experience](https://www.minecraft.net) - `EXPERIENCE`
- [zEssentials](https://www.spigotmc.org/resources/118014/) - `ZESSENTIALS`
- [zMenu](https://www.spigotmc.org/resources/110402/) - `ZMENUITEMS`
- [EcoBits](https://www.spigotmc.org/resources/109967/) - `ECOBITS`
- [CoinsEngine](https://www.spigotmc.org/resources/84121/) - `COINSENGINE`
- [ExcellentEconomy](https://modrinth.com/plugin/excellenteconomy) - `EXCELLENTECONOMY`
- [VotingPlugin](https://www.spigotmc.org/resources/15358/) - `VOTINGPLUGIN`
- [RedisEconomy](https://www.spigotmc.org/resources/105965/) - `REDISECONOMY`
- [RoyaleEconomy](https://polymart.org/product/113/royaleeconomy-1-8-1-21) - `ROYALEECONOMY`

Each of these providers is implemented through a specific class extending `CurrencyProvider`.

## Adding Currencies API to Your Project

The CurrenciesAPI is hosted on JitPack, making it easy to include in your project.

### Maven

To add the Currencies API to your project using Maven, add the following to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>groupez-releases</id>
        <name>GroupeZ Repository</name>
        <url>https://repo.groupez.dev/releases</url>
    </repository>
</repositories>

<dependency>
    <groupId>fr.traqueur.currencies</groupId>
    <artifactId>currenciesapi</artifactId>
    <version>1.0.14</version>
</dependency>
```

### Gradle

To add the Currencies API to your project using Gradle, add the following to your `build.gradle`:

```kotlin
repositories {
    maven {
        name = "groupezReleases"
        url = uri("https://repo.groupez.dev/releases")
    }
}

dependencies {
    implementation("fr.traqueur.currencies:currenciesapi:1.0.14")
}
```

### Relocating the API

It is recommended to relocate the Currencies API in your project to avoid potential conflicts with other plugins that might also use this library. You can use a tool like [Shadow](https://imperceptiblethoughts.com/shadow/) to relocate the package to a unique namespace.

## Usage

### Importing the Currencies

Before using the `Currencies` class, ensure that you have imported the relevant classes in your Java code:

```java
import fr.traqueur.currencies.Currencies;
import org.bukkit.OfflinePlayer;
import java.math.BigDecimal;
```

### Accessing a Currency Provider

Each currency is represented as an enum value in `Currencies`. You can access a specific provider by using the enum values:

```java
Currencies currency = Currencies.VAULT;
```

### Resolving a Currency from a String

To resolve a currency from a config value (e.g. a string stored in a YAML file), use `Currencies.fromName(String)` instead of `Enum.valueOf`. It redirects deprecated aliases to their canonical constant and logs a one-time warning, so old configs keep working while new ones use the correct name.

```java
Currencies currency = Currencies.fromName("EXCELLENTECONOMY");
```

Note: `EXCELLENTEECONOMY` is a deprecated alias of `EXCELLENTECONOMY` and is only kept for backward compatibility.

### Creating a Provider Instance

The `createProvider` method should be used to instantiate the provider for the following currencies: `ZMENUITEMS`,`ITEM`,. You must pass the appropriate parameters that match the expected types for each specific provider class.

Here are the parameter types required for each provider:

- **ZMENUITEMS**: `Plugin`, `File`, `String` (The `String` represents the path in the YAML file, and it must end with a `.`)
- **ITEM**: `Plugin`, `ItemStack`

To create a provider instance, call the `createProvider` method with the correct parameter types for the specific currency. For example:

```java
// For ZMenuItemProvider
currency.createProvider(plugin, file, path);

// For ItemProvider
currency.createProvider(plugin, itemStack);

```

### Multi-Currency Management

For APIs that support multiple currencies, CurrenciesAPI handles everything seamlessly. Using the standard methods—deposit, withdraw, and getBalance—you can specify the exact currency you want to interact with, allowing flexible and intuitive currency management across different plugins.

```java
// For item you must register by yourself
Currencies.ITEM.registerProvider("gold", new ItemStack(Material.GOLD);
Currencies.ITEM.getBalance(player, "gold");

//For zEssentials (and CoinsEngine and Ecobits) it's automatic
Currencies.ZESSENTIALS.getBalance(player, "coins");

```

### Safe Purchases: `withdrawIfSufficient`

`withdraw` does **not** check whether the player can afford the amount. Most backends will happily
drive a balance negative or silently clamp it to zero. Checking the balance first and then calling
`withdraw` is not safe either, because anything can happen between the two calls: a second click, a
second server, or an economy plugin that commits its writes asynchronously. That gap is a
double-spend.

Use `withdrawIfSufficient` for anything that is paying for something. It performs the check and the
debit as one operation and tells you what happened:

```java
TransactionResult result = Currencies.VAULT.withdrawIfSufficient(
        playerId, new BigDecimal("1000"), "Shop purchase");

switch (result.getStatus()) {
    case SUCCESS:
        // The money is gone. Only now hand over the goods.
        break;
    case INSUFFICIENT_FUNDS:
        player.sendMessage("You cannot afford this.");
        break;
    case UNSUPPORTED:
        // The backend cannot do this at all. Nothing was debited.
        break;
    case FAILED:
        // Something went wrong. Nothing was debited.
        break;
}
```

**Nothing is ever debited unless the status is `SUCCESS`.** That is the only case where you should
hand out the goods.

An asynchronous variant is available and never completes exceptionally, failures come back through
the result:

```java
Currencies.VAULT.withdrawIfSufficientAsync(playerId, amount, "default", "Shop purchase")
        .thenAccept(result -> { /* ... */ });
```

### Atomicity Per Backend

Some backends can refuse a withdrawal themselves, others cannot. When a backend cannot, the library
emulates the operation by locking around a balance read and a withdraw. That stops one server racing
itself, but it **cannot** stop a second server acting on the same shared economy.

Ask before you rely on it, either up front or from the result:

```java
if (!Currencies.VAULT.hasNativeConditionalWithdraw("default")) {
    getLogger().warning("This currency cannot guarantee atomic purchases across servers.");
}

result.isBackendGuaranteed(); // false when the library had to emulate the operation
```

| Currency | Atomic | Notes |
| --- | --- | --- |
| `VAULT` | yes | As atomic as the underlying economy plugin |
| `PLAYERPOINTS` | yes | `take` refuses when the balance is too low |
| `ZESSENTIALS` | yes | `withdraw` reports the outcome |
| `REDISECONOMY` | yes | Validated in Redis, so it holds across servers |
| `EXCELLENTECONOMY` | yes | Native async operation with a result |
| `VOTINGPLUGIN` | yes | `removePoints` reports the outcome |
| `ITEM`, `ZMENUITEMS` | yes | Player inventory, main thread only |
| `LEVEL`, `EXPERIENCE` | yes | Player state, main thread only |
| `COINSENGINE` | no | Its boolean means "currency found", not "could afford" |
| `ECOBITS` | no | `adjustBalance` returns nothing |
| `BEASTTOKENS` | no | `removeTokens` returns nothing |
| `ROYALEECONOMY` | no | `removeBalance` returns nothing |
| `ELEMENTALTOKENS`, `ELEMENTALGEMS` | no | `removeTokens` / `removeGems` return nothing |

If you run a network where several servers share one economy database, only the currencies marked
atomic are safe against cross-server double spends. For the others the fix has to come from the
economy plugin itself.

### Custom Economies

`Currencies` is an enum, so it cannot be extended. To plug in your own economy, implement
`CurrencyProvider` and register the instance:

```java
public class MyGemsProvider implements CurrencyProvider {
    public void deposit(UUID playerId, BigDecimal amount, String reason) { /* ... */ }
    public void withdraw(UUID playerId, BigDecimal amount, String reason) { /* ... */ }
    public BigDecimal getBalance(UUID playerId) { /* ... */ }
}

CurrencyRegistry.register("my_gems", new MyGemsProvider());

TransactionResult result = CurrencyRegistry.withdrawIfSufficient(
        "my_gems", playerId, BigDecimal.TEN, "Shop purchase");
```

When you override `withdrawIfSufficient`, build the result with the factory that matches who made
the decision: `TransactionResult.nativeSuccess(...)` / `nativeInsufficientFunds(...)` when your
backend checked the funds itself, or `emulatedSuccess(...)` / `emulatedInsufficientFunds(...)` when
you checked them yourself. `unsupported(...)` and `failed(...)` cover the rest. That is what
`isBackendGuaranteed()` reports back to the caller.

To look a registered currency up, `CurrencyRegistry.require(name)` throws when there is none and
`CurrencyRegistry.find(name)` returns null. Use `registerOrReplace(...)` to deliberately swap an
implementation, for example on a config reload.

Those three methods are all you have to write. Everything else has a default implementation, so an
existing provider keeps working unchanged. Two optional overrides are worth knowing about:

- `hasNativeConditionalWithdraw()` and `withdrawIfSufficient(...)`: override both when your backend can
  refuse a withdrawal itself. You get a real guarantee instead of the emulated one. Call
  `CurrencyArgumentChecks.findProblem(playerId, amount)` first so your implementation rejects the same bad
  inputs as every other provider.
- `requiresMainThread()`: **defaults to `true`**, because most Bukkit APIs are not thread safe.
  Override it to return `false` only if your backend is documented as safe for concurrent access.
  Leaving it `true` means `withdrawIfSufficientAsync` hops back to the main thread for you.

### Asynchronous Access and `CurrenciesAPI.init`

Scheduling work back onto the main server thread needs a plugin instance. If you intend to use the
asynchronous API with a main-thread-bound currency, call this once in `onEnable`:

```java
CurrenciesAPI.init(this);
```

Without it, an asynchronous call on such a currency returns a `FAILED` result explaining what is
missing, rather than touching player state from the wrong thread.

### Example Usage

Here is a more complete example of how to use the `Currencies` class within a Minecraft plugin. In this example, we create an economy instance with `zEssentials` and provide a command that allows players to choose between `Vault` and `zEssentials` to deposit or withdraw an amount.

```java
package com.example.myplugin;

import fr.traqueur.currencies.Currencies;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.Plugin;
import java.math.BigDecimal;

public class MyPlugin extends JavaPlugin {
    private Currencies selectedCurrency;

    @Override
    public void onEnable() {
        // Create zEssentials economy provider
        Currencies.ZESSENTIALS.createProvider("coin");

        // Set default economy to Vault
        selectedCurrency = Currencies.VAULT;

        // Register command
        this.getCommand("setEconomy").setExecutor(new EconomyCommand());
    }

    public class EconomyCommand implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("This command can only be used by players.");
                return true;
            }

            Player player = (Player) sender;

            if (args.length < 2) {
                player.sendMessage("Usage: /setEconomy <vault|zessentials> <amount>");
                return true;
            }

            String economyName = args[0].toLowerCase();
            BigDecimal amount;

            try {
                amount = new BigDecimal(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage("Invalid amount. Please enter a valid number.");
                return true;
            }

            switch (economyName) {
                case "vault":
                    selectedCurrency = Currencies.VAULT;
                    break;
                case "zessentials":
                    selectedCurrency = Currencies.ZESSENTIALS;
                    break;
                default:
                    player.sendMessage("Invalid economy. Please choose either 'vault' or 'zessentials'.");
                    return true;
            }

            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player.getUniqueId());

            // Deposit the specified amount using the selected currency
            selectedCurrency.deposit(offlinePlayer, amount);
            player.sendMessage("Deposited " + amount + " to your " + selectedCurrency.name() + " account.");

            // Get and display the new balance
            BigDecimal balance = selectedCurrency.getBalance(offlinePlayer);
            player.sendMessage("Your new balance is: " + balance);

            return true;
        }
    }
}

```