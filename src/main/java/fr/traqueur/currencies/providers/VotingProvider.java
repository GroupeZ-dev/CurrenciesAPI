package fr.traqueur.currencies.providers;

import com.bencodez.votingplugin.VotingPluginHooks;
import com.bencodez.votingplugin.user.UserManager;
import fr.traqueur.currencies.CurrencyProvider;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;

public class VotingProvider implements CurrencyProvider {

    private final UserManager userManager = VotingPluginHooks.getInstance().getUserManager();

    @Override
    public void deposit(OfflinePlayer offlinePlayer, BigDecimal amount, String reason) {
        this.userManager.getVotingPluginUser(offlinePlayer.getUniqueId()).addPoints(amount.intValue());
    }

    @Override
    public void withdraw(OfflinePlayer offlinePlayer, BigDecimal amount, String reason) {
        this.userManager.getVotingPluginUser(offlinePlayer.getUniqueId()).removePoints(amount.intValue());
    }

    @Override
    public BigDecimal getBalance(OfflinePlayer offlinePlayer) {
        return BigDecimal.valueOf(this.userManager.getVotingPluginUser(offlinePlayer.getUniqueId()).getPoints());
    }
}
