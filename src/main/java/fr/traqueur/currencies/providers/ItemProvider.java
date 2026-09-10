package fr.traqueur.currencies.providers;

import fr.traqueur.currencies.CurrencyArgumentChecks;
import fr.traqueur.currencies.CurrencyProvider;
import fr.traqueur.currencies.TransactionResult;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;

import java.math.BigDecimal;
import java.util.UUID;

public class ItemProvider implements CurrencyProvider {

    protected final Plugin plugin;
    private final ItemStack itemStack;

    public ItemProvider(Plugin plugin, ItemStack itemStack) {
        this.plugin = plugin;
        this.itemStack = itemStack;
    }

    @Override
    public void deposit(UUID playerId, BigDecimal amount, String reason) {
        Player player = Bukkit.getPlayer(playerId);
        if (player != null) {
            this.giveItem(player, amount.intValue(), this.itemStack);
        } else{
            this.plugin.getLogger().severe("Deposit items to " + playerId + " but is offline");
        }
    }

    @Override
    public void withdraw(UUID playerId, BigDecimal amount, String reason) {
        Player player = Bukkit.getPlayer(playerId);
        if (player != null) {
            this.removeItems(player, this.itemStack, amount.intValue());
        } else {
            this.plugin.getLogger().severe("Withdraw items from " + playerId + " but is offline");
        }
    }

    @Override
    public BigDecimal getBalance(UUID playerId) {
        Player player = Bukkit.getPlayer(playerId);
        if (player != null) {
            return BigDecimal.valueOf(this.getAmount(player, this.itemStack));
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
            this.give(player, itemStack);
            this.giveItem(player, value, itemStack);
        } else {
            itemStack.setAmount((int) value);
            this.give(player, itemStack);
        }
    }

    public ItemStack getItemStack(Player player) {
        return itemStack.clone();
    }

    private void give(Player player, ItemStack item) {
        if (this.hasInventoryFull(player)) player.getWorld().dropItem(player.getLocation(), item);
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

    @Override
    public boolean hasNativeConditionalWithdraw() {
        return true;
    }

    @Override
    public TransactionResult withdrawIfSufficient(UUID playerId, BigDecimal amount, String reason) {
        TransactionResult invalid = CurrencyArgumentChecks.findProblem(playerId, amount);
        if (invalid != null) {
            return invalid;
        }

        Player player = Bukkit.getPlayer(playerId);
        if (player == null) {
            return TransactionResult.failed(amount, "Items can only be taken from an online player.");
        }

        if (amount.stripTrailingZeros().scale() > 0) {
            return TransactionResult.failed(amount, "An item currency only supports whole amounts, got " + amount + ".");
        }

        int cost;
        try {
            cost = amount.intValueExact();
        } catch (ArithmeticException exception) {
            return TransactionResult.failed(amount, "The amount does not fit in an integer item count: " + amount + ".");
        }

        ItemStack currencyItem = this.getItemStack(player);
        if (currencyItem == null) {
            return TransactionResult.failed(amount, "The currency item could not be resolved for " + player.getName() + ".");
        }

        int held = this.getAmount(player, currencyItem);
        if (held < cost) {
            return TransactionResult.nativeInsufficientFunds(amount, BigDecimal.valueOf(held));
        }

        this.removeItems(player, currencyItem, cost);
        return TransactionResult.nativeSuccess(amount, BigDecimal.valueOf(held - cost));
    }
}
