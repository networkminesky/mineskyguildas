package net.mineskyguildas.commands.subcommands.guild.admin;

import net.mineskyguildas.commands.subcommands.SubCommand;
import net.mineskyguildas.data.Guilds;
import net.mineskyguildas.handlers.GuildHandler;
import net.mineskyguildas.utils.Utils;
import org.bukkit.entity.Player;

import java.util.List;

import static net.mineskyguildas.commands.GuildCommand.sendError;

public class BankAdminSubCommand extends SubCommand {
    @Override
    public String getName() {
        return "banco-admin";
    }

    @Override
    public String getDescription() {
        return "Gerenciar banco da guilda";
    }

    @Override
    public String getUsage() {
        return "/guilda admin banco-admin <dar,tirar,resetar,saldo,setar> <tag> [quantidade]";
    }

    @Override
    public List<String> getAliases() {
        return List.of("bank");
    }

    @Override
    public boolean getAdminCommand() {
        return true;
    }

    @Override
    public void perform(Player player, String[] args) {
        if (args.length < 3) {
            sendError(player, "&4⚠ &cUso: " + getUsage());
            return;
        }

        Guilds guild = GuildHandler.getGuildByTag(args[2]);
        if (guild == null) {
            sendError(player, "&4⚠ &cVocê deve citar uma guilda válida.");
            return;
        }


        if(args[1].equalsIgnoreCase("dar")) {
            if (args.length < 4) {
                sendError(player, "&4⚠ &cUso: " + getUsage());
                return;
            }
            if (!isInteger(args[3])) {
                sendError(player, "&4⚠ &cVocê precisa indicar um valor numérico.");
                return;
            }

            int value = Integer.parseInt(args[3]);
                guild.deposit(value);
                GuildHandler.broadcastGuildMessage(guild, "&b\uD83D\uDCB0 &3Foi depositado uma quantia de &b$" + value + "&3 para a sua guilda!");
                player.sendMessage(Utils.c("&aVocê deu $" + value + " para a guilda " + guild.getName() + "!"));
            return;
        } else if (args[1].equalsIgnoreCase("tirar")) {
            if (args.length < 4) {
                sendError(player, "&4⚠ &cUso: " + getUsage());
                return;
            }
            if (!isInteger(args[3])) {
                sendError(player, "&4⚠ &cVocê precisa indicar um valor numérico.");
                return;
            }

            int value = Integer.parseInt(args[3]);
            if (guild.withdraw(value)) {
                    guild.withdraw(value);
                    GuildHandler.broadcastGuildMessage(guild, "&b\uD83D\uDCB0 &3Foi retirado uma quantia de &b$" + value + "&3 da sua guilda!");
                    player.sendMessage(Utils.c("&aVocê tirou $" + value + " da guilda " + guild.getName() + "!"));
            } else {
                sendError(player, "&4⚠ &cA guilda não tem dinheiro suficiente.");
                return;
            }
        } else if (args[1].equalsIgnoreCase("resetar")) {
            guild.setBalance(0);
            GuildHandler.broadcastGuildMessage(guild, "&b\uD83D\uDCB0 &3O dinheiro da sua guilda foi resetado!");
            player.sendMessage(Utils.c("&aVocê resetou o dinheiro da guilda " + guild.getName() + "!"));
        } else if (args[1].equalsIgnoreCase("setar")) {
            if (args.length < 4) {
                sendError(player, "&4⚠ &cUso: " + getUsage());
                return;
            }
            if (!isInteger(args[3])) {
                sendError(player, "&4⚠ &cVocê precisa indicar um valor numérico.");
                return;
            }

            int value = Integer.parseInt(args[3]);
            guild.setBalance(value);
            GuildHandler.broadcastGuildMessage(guild, "&b\uD83D\uDCB0 &3O dinheiro da sua guilda foi setado em &b$" + value + "!");
            player.sendMessage(Utils.c("&aVocê setou o dinheiro da guilda " + guild.getName() + " em $" + value + "!"));
        } else if (args[1].equalsIgnoreCase("saldo")) {
            player.sendMessage(Utils.c("&b\uD83D\uDCB0 &3Banco da guilda " + guild.getName() + ": &b$" + guild.getBalance()));
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
