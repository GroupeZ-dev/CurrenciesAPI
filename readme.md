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
- [VotingPlugin](https://www.spigotmc.org/resources/15358/) - `VOTINGPLUGIN`
- [RedisEconomy](https://www.spigotmc.org/resources/105965/) - `REDISECONOMY`

Each of these providers is implemented through a specific class extending `CurrencyProvider`.

## Adding Currencies API to Your Project

The CurrenciesAPI is hosted on JitPack, making it easy to include in your project.

### Maven

To add the Currencies API to your project using Maven, add the following to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.Traqueur-dev</groupId>
    <artifactId>CurrenciesAPI</artifactId>
    <version>1.0.5</version>
</dependency>
```

### Gradle

To add the Currencies API to your project using Gradle, add the following to your `build.gradle`:

```gradle
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.Traqueur-dev:CurrenciesAPI:1.0.5'
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

