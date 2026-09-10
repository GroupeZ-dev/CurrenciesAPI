package fr.traqueur.currencies.providers;

import com.bencodez.votingplugin.VotingPluginHooks;
import com.bencodez.votingplugin.user.UserManager;
import com.bencodez.votingplugin.user.VotingPluginUser;
import fr.traqueur.currencies.CurrencyArgumentChecks;
import fr.traqueur.currencies.CurrencyProvider;
import fr.traqueur.currencies.TransactionResult;

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

        try {
            if (amount.stripTrailingZeros().scale() > 0) {
                return TransactionResult.failed(amount, "VotingPlugin only supports whole amounts, got " + amount + ".");
            }

            VotingPluginUser user = this.userManager.getVotingPluginUser(playerId);
            if (user.removePoints(amount.intValueExact())) {
                return TransactionResult.nativeSuccess(amount, BigDecimal.valueOf(user.getPoints()));
            }
            return TransactionResult.nativeInsufficientFunds(amount, BigDecimal.valueOf(user.getPoints()));
        } catch (ArithmeticException exception) {
            return TransactionResult.failed(amount, "The amount does not fit in a VotingPlugin integer: " + amount + ".");
        } catch (Exception exception) {
            return TransactionResult.failed(amount, "VotingPlugin threw while withdrawing: " + exception.getMessage());
        }
    }
}
