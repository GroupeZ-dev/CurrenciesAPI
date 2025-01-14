package fr.traqueur.testplugin;

import fr.traqueur.currencies.Currencies;
import fr.traqueur.currencies.CurrencyProvider;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public final class TestPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        //Create a provider by itself
        CurrencyProvider provider = Currencies.ITEM.createProvider(this, new ItemStack(Material.DIAMOND));
        CurrencyProvider provider1 = Currencies.LEVEL.createProvider();
        CurrencyProvider provider2 = Currencies.EXPERIENCE.createProvider();

        //Create a provider by the registerProvider method to use directly in the API

        //But not necessary to use the registerProvider method, for ITEM, LEVEL and EXPERIENCE currencies, it's already done
        Currencies.ITEM.registerProvider("diamond", new ItemStack(Material.DIAMOND));
        Currencies.LEVEL.registerProvider("level");
        Currencies.EXPERIENCE.registerProvider("experience");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
