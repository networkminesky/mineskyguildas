package net.mineskyguildas.commands.subcommands.guild;

import net.mineskyguildas.commands.subcommands.SubCommand;
import net.mineskyguildas.data.Guilds;
import net.mineskyguildas.enums.GuildRoles;
import net.mineskyguildas.handlers.GuildHandler;
import net.mineskyguildas.hooks.Vault;
import net.mineskyguildas.utils.Utils;
import org.bukkit.entity.Player;

import java.util.List;

import static net.mineskyguildas.commands.GuildCommand.sendError;

public class BankSubCommand extends SubCommand {
    @Override
    public String getName() {
        return "banco";
    }

    @Override
    public String getDescription() {
        return "Gerenciar banco da guilda";
    }

    @Override
    public String getUsage() {
        return "/guilda banco [dar,sacar]";
    }

    @Override
    public List<String> getAliases() {
        return List.of("bank");
    }

    @Override
    public boolean getAdminCommand() {
        return false;
    }

    @Override
    public void perform(Player player, String[] args) {
        Guilds guild = GuildHandler.getGuildByPlayer(player);
        if (!GuildHandler.hasGuild(player) || guild == null) {
            sendError(player, "&4⚠ &cVocê não pertence a uma guilda.");
            return;
        }


        if (args.length < 3 || args[1].equalsIgnoreCase("saldo")) {
            player.sendMessage(Utils.c("&b\uD83D\uDCB0 &3Banco: &b$" + guild.getBalance()));
            return;
        }

        if (!isInteger(args[2])) {
            sendError(player, "&4⚠ &cVocê precisa indicar um valor numérico.");
            return;
        }

        int value = Integer.parseInt(args[2]);

        if(args[1].equalsIgnoreCase("dar")) {
            if (Vault.withdraw(player, value)) {
                guild.deposit(value);
                GuildHandler.broadcastGuildMessage(guild, "&3\uD83D\uDCB0 &b" + player.getName() + " &3depositou a quantia de &b$" + value + "&3!");
                player.sendMessage(Utils.c("&aVocê deu $" + value + " para a sua guilda."));
            } else {
                sendError(player, "&4⚠ &cVocê não tem dinheiro suficiente para depositar.");
                return;
            }
            return;
        } else if (args[1].equalsIgnoreCase("sacar")) {
            if (!GuildRoles.isLeaders(guild.getRole(player.getUniqueId()))) {
                sendError(player, "&4⚠ &cApenas os &lLÍDERES&r &cda guilda pode sacar dinheiro do banco.");
                return;
            }
            if (guild.withdraw(value)) {
                if (Vault.deposit(player, value)) {
                    guild.withdraw(value);
                    GuildHandler.broadcastGuildMessage(guild, "&3\uD83D\uDCB0 &b" + player.getName() + " &3sacou uma quantia de &b$" + value + "&3!");
                    player.sendMessage(Utils.c("&aVocê sacou $" + value + " da sua guilda."));
                } else {
                    sendError(player, "&4⚠ &cOcorreu um erro ao depositar a sua conta.");
                    return;
                }
            } else {
                sendError(player, "&4⚠ &cA guilda não tem dinheiro suficiente.");
                return;
            }
        }
    }

    private boolean isInteger(String s) {
        if (s.isEmpty()) return false;
        try {
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
