package fr.traqueur.currencies.providers;

import fr.maxlego08.menu.api.InventoryManager;
import fr.maxlego08.menu.api.MenuItemStack;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.math.BigDecimal;
import java.util.UUID;

public class ZMenuItemProvider extends ItemProvider {

    private final MenuItemStack menuItemStack;

    public ZMenuItemProvider(Plugin plugin, File file, String path) {
        super(plugin, null);
        InventoryManager inventoryManager = plugin.getServer().getServicesManager().getRegistration(InventoryManager.class).getProvider();
        this.menuItemStack = inventoryManager.loadItemStack(YamlConfiguration.loadConfiguration(file), path, file);
    }

    public ZMenuItemProvider(Plugin plugin, MenuItemStack menuItemStack) {
        super(plugin, null);
        this.menuItemStack = menuItemStack;
    }

    @Override
    public BigDecimal getBalance(UUID playerId) {
        Player player = Bukkit.getPlayer(playerId);
        if (player != null) {
            return BigDecimal.valueOf(getAmount(player, this.menuItemStack.build(player)));
        } else return BigDecimal.ZERO;
    }

    @Override
    public void deposit(UUID playerId, BigDecimal amount, String reason) {
        Player player = Bukkit.getPlayer(playerId);
        if (player != null) {
            giveItem(player, amount.intValue(), this.menuItemStack.build(player));
        } else {
            this.plugin.getLogger().severe("Deposit items to " + playerId + " but is offline");
        }
    }

    @Override
    public void withdraw(UUID playerId, BigDecimal amount, String reason) {
        Player player = Bukkit.getPlayer(playerId);
        if (player != null) {
            removeItems(player, this.menuItemStack.build(player), amount.intValue());
        } else {
            this.plugin.getLogger().severe("Withdraw items from " + playerId + " but is offline");
        }
    }

    @Override
    public ItemStack getItemStack(Player player) {
        return this.menuItemStack.build(player);
    }
}
