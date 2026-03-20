package fr.traqueur.currencies.providers;

import com.bencodez.votingplugin.VotingPluginHooks;
import com.bencodez.votingplugin.user.UserManager;
import fr.traqueur.currencies.CurrencyProvider;

import java.math.BigDecimal;
import java.util.UUID;

public class VotingProvider implements CurrencyProvider {

    private final UserManager userManager = VotingPluginHooks.getInstance().getUserManager();

    @Override
    public void deposit(UUID playerId, BigDecimal amount, String reason) {
        this.userManager.getVotingPluginUser(playerId).addPoints(amount.intValue());
    }

    @Override
    public void withdraw(UUID playerId, BigDecimal amount, String reason) {
        this.userManager.getVotingPluginUser(playerId).removePoints(amount.intValue());
    }

    @Override
    public BigDecimal getBalance(UUID playerId) {
        return BigDecimal.valueOf(this.userManager.getVotingPluginUser(playerId).getPoints());
    }
}
