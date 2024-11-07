package fr.traqueur.currencies.providers;

import fr.traqueur.currencies.CurrencyProvider;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;

import java.math.BigDecimal;

public class ItemProvider implements CurrencyProvider {

    protected final Plugin plugin;
    private final ItemStack itemStack;

    public ItemProvider(Plugin plugin, ItemStack itemStack) {
        this.plugin = plugin;
        this.itemStack = itemStack;
    }

    @Override
    public void deposit(OfflinePlayer offlinePlayer, BigDecimal amount) {
        if (offlinePlayer.isOnline()) {
            Player player = offlinePlayer.getPlayer();
            removeItems(player, this.itemStack, amount.intValue());
        } else{
            this.plugin.getLogger().severe("Deposit items to " + offlinePlayer.getName() + " but is offline");
        }
    }

    @Override
    public void withdraw(OfflinePlayer offlinePlayer, BigDecimal amount) {
        if (offlinePlayer.isOnline()) {
            Player player = offlinePlayer.getPlayer();
            giveItem(player, amount.intValue(), this.itemStack);
        } else {
            this.plugin.getLogger().severe("Withdraw items from " + offlinePlayer.getName() + " but is offline");
        }
    }

    @Override
    public BigDecimal getBalance(OfflinePlayer offlinePlayer) {
        if (offlinePlayer.isOnline()) {
            Player player = offlinePlayer.getPlayer();
            return BigDecimal.valueOf(getAmount(player, this.itemStack));
        } else return BigDecimal.ZERO;
    }

    protected int getAmount(Player player, ItemStack itemStack) {
        int items = 0;
        for (int slot = 0; slot != 36; slot++) {
            ItemStack currentItemStack = player.getInventory().getItem(slot);
            if (currentItemStack != null && currentItemStack.isSimilar(itemStack))
                items += currentItemStack.getAmount();
        }
        return items;
    }

    protected void removeItems(Player player, ItemStack itemStack, long value) {
        PlayerInventory playerInventory = player.getInventory();

        int item = (int) value;
        int slot = 0;

        // On retire ensuite les items de l'inventaire du joueur
        for (ItemStack is : playerInventory.getContents()) {

            if (is != null && is.isSimilar(itemStack) && item > 0) {

                int currentAmount = is.getAmount() - item;
                item -= is.getAmount();

                if (currentAmount <= 0) {
                    if (slot == 40)
                        playerInventory.setItemInOffHand(null);
                    else
                        playerInventory.removeItem(is);
                } else
                    is.setAmount(currentAmount);
            }
            slot++;
        }
    }

    protected void giveItem(Player player, long value, ItemStack itemStack) {
        itemStack = itemStack.clone();
        if (value > 64) {
            value -= 64;
            itemStack.setAmount(64);
            give(player, itemStack);
            giveItem(player, value, itemStack);
        } else {
            itemStack.setAmount((int) value);
            give(player, itemStack);
        }
    }

    public ItemStack getItemStack(Player player) {
        return itemStack.clone();
    }

    private void give(Player player, ItemStack item) {
        if (hasInventoryFull(player)) player.getWorld().dropItem(player.getLocation(), item);
        else player.getInventory().addItem(item);
    }

    private boolean hasInventoryFull(Player player) {
        int slot = 0;
        PlayerInventory inventory = player.getInventory();
        for (int a = 0; a != 36; a++) {
            ItemStack itemStack = inventory.getContents()[a];
            if (itemStack == null) slot++;
        }
        return slot == 0;
    }
}
