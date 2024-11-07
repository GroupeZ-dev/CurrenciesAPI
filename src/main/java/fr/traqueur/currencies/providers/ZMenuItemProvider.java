package fr.traqueur.currencies.providers;

import fr.maxlego08.menu.MenuItemStack;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.math.BigDecimal;

public class ZMenuItemProvider extends ItemProvider {

    private final MenuItemStack menuItemStack;

    public ZMenuItemProvider(Plugin plugin, MenuItemStack menuItemStack) {
        super(plugin, null);
        this.menuItemStack = menuItemStack;
    }

    @Override
    public BigDecimal getBalance(OfflinePlayer offlinePlayer) {
        if (offlinePlayer.isOnline()) {
            Player player = offlinePlayer.getPlayer();
            return BigDecimal.valueOf(getAmount(player, this.menuItemStack.build(player)));
        } else return BigDecimal.ZERO;
    }

    @Override
    public void deposit(OfflinePlayer offlinePlayer, BigDecimal amount) {
        if (offlinePlayer.isOnline()) {
            Player player = offlinePlayer.getPlayer();
            removeItems(player, this.menuItemStack.build(player), amount.intValue());
        } else {
            this.plugin.getLogger().severe("Deposit items to " + offlinePlayer.getName() + " but is offline");
        }
    }

    @Override
    public void withdraw(OfflinePlayer offlinePlayer, BigDecimal amount) {
        if (offlinePlayer.isOnline()) {
            Player player = offlinePlayer.getPlayer();
            giveItem(player, amount.intValue(), this.menuItemStack.build(player));
        } else {
            this.plugin.getLogger().severe("Withdraw items from " + offlinePlayer.getName() + " but is offline");
        }
    }
}
