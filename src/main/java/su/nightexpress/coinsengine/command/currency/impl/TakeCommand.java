package su.nightexpress.coinsengine.command.currency.impl;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.coinsengine.CoinsEnginePlugin;
import su.nightexpress.coinsengine.Placeholders;
import su.nightexpress.coinsengine.api.currency.Currency;
import su.nightexpress.coinsengine.command.CommandFlags;
import su.nightexpress.coinsengine.command.currency.CurrencySubCommand;
import su.nightexpress.coinsengine.config.Lang;
import su.nightexpress.coinsengine.config.Perms;
import su.nightexpress.coinsengine.util.CoinsUtils;
import su.nightexpress.nightcore.command.CommandResult;
import su.nightexpress.nightcore.util.Players;

import java.util.Arrays;
import java.util.List;

public class TakeCommand extends CurrencySubCommand {

    public TakeCommand(@NotNull CoinsEnginePlugin plugin, @NotNull Currency currency) {
        super(plugin, currency, new String[]{"take"}, Perms.COMMAND_CURRENCY_TAKE);
        this.setDescription(Lang.COMMAND_CURRENCY_TAKE_DESC);
        this.setUsage(Lang.COMMAND_CURRENCY_TAKE_USAGE);
        this.addFlag(CommandFlags.SILENT, CommandFlags.NO_SAVE);
    }

    @Override
    @NotNull
    public List<String> getTab(@NotNull Player player, int arg, @NotNull String[] args) {
        if (arg == 1) {
            return Players.playerNames(player);
        }
        if (arg == 2) {
            return Arrays.asList("1", "10", "50", "100");
        }
        return super.getTab(player, arg, args);
    }

    @Override
    protected void onExecute(@NotNull CommandSender sender, @NotNull CommandResult result) {
        if (result.length() < 3) {
            this.errorUsage(sender);
            return;
        }

        double amount = CoinsUtils.getAmountFromInput(result.getArg(2));
        if (amount <= 0D) return;

        this.plugin.getUserManager().getUserDataAndPerformAsync(result.getArg(1), user -> {
            if (user == null) {
                this.errorPlayer(sender);
                return;
            }

            user.removeBalance(this.currency, amount);

            if (!result.hasFlag(CommandFlags.NO_SAVE)) {
                this.plugin.getUserManager().saveAsync(user);
            }

            this.plugin.getCoinsLogger().logTake(user, currency, amount, sender);

            Lang.COMMAND_CURRENCY_TAKE_DONE.getMessage()
                .replace(currency.replacePlaceholders())
                .replace(Placeholders.PLAYER_NAME, user.getName())
                .replace(Placeholders.GENERIC_AMOUNT, currency.format(amount))
                .replace(Placeholders.GENERIC_BALANCE, currency.format(user.getBalance(currency)))
                .send(sender);

            Player target = user.getPlayer();
            if (!result.hasFlag(CommandFlags.SILENT) && target != null) {
                Lang.COMMAND_CURRENCY_TAKE_NOTIFY.getMessage()
                    .replace(currency.replacePlaceholders())
                    .replace(Placeholders.GENERIC_AMOUNT, currency.format(amount))
                    .replace(Placeholders.GENERIC_BALANCE, currency.format(user.getBalance(currency)))
                    .send(target);
            }
        });
    }
}
