package fr.traqueur.currencies.providers;

import fr.maxlego08.menu.api.InventoryManager;
import fr.maxlego08.menu.api.MenuItemStack;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.math.BigDecimal;

public class ZMenuItemProvider extends ItemProvider {

    private final MenuItemStack menuItemStack;

    public ZMenuItemProvider(Plugin plugin, File file, String path) {
        super(plugin, null);
        InventoryManager inventoryManager = plugin.getServer().getServicesManager().getRegistration(InventoryManager.class).getProvider();
        this.menuItemStack = inventoryManager.loadItemStack(YamlConfiguration.loadConfiguration(file), path, file);
    }

    @Override
    public BigDecimal getBalance(OfflinePlayer offlinePlayer) {
        if (offlinePlayer.isOnline()) {
            Player player = offlinePlayer.getPlayer();
            return BigDecimal.valueOf(getAmount(player, this.menuItemStack.build(player)));
        } else return BigDecimal.ZERO;
    }

    @Override
    public void deposit(OfflinePlayer offlinePlayer, BigDecimal amount, String reason) {
        if (offlinePlayer.isOnline()) {
            Player player = offlinePlayer.getPlayer();
            removeItems(player, this.menuItemStack.build(player), amount.intValue());
        } else {
            this.plugin.getLogger().severe("Deposit items to " + offlinePlayer.getName() + " but is offline");
        }
    }

    @Override
    public void withdraw(OfflinePlayer offlinePlayer, BigDecimal amount, String reason) {
        if (offlinePlayer.isOnline()) {
            Player player = offlinePlayer.getPlayer();
            giveItem(player, amount.intValue(), this.menuItemStack.build(player));
        } else {
            this.plugin.getLogger().severe("Withdraw items from " + offlinePlayer.getName() + " but is offline");
        }
    }

    @Override
    public ItemStack getItemStack(Player player) {
        return this.menuItemStack.build(player);
    }
}
