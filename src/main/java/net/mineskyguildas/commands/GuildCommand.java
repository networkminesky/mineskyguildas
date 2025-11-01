package net.mineskyguildas.commands;

import net.mineskyguildas.MineSkyGuildas;
import net.mineskyguildas.commands.subcommands.SubCommand;
import net.mineskyguildas.commands.subcommands.guild.*;
import net.mineskyguildas.commands.subcommands.guild.admin.*;
import net.mineskyguildas.gui.GuildMenu;
import net.mineskyguildas.utils.Utils;
import org.bukkit.Sound;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class GuildCommand implements CommandExecutor {

    private static final Set<SubCommand> subCommands = new HashSet<>();

    public GuildCommand(MineSkyGuildas plugin) {
        Arrays.asList(
                new AcceptSubCommand(),
                new AllySubCommand(),
                new AnnouncerSubCommand(),
                new BankSubCommand(),
                new BannerSubCommand(),
                new BaseSubCommand(),
                new CoordsSubCommand(),
                new CreateSubCommand(),
                new DemoteSubCommand(),
                new DisbandSubCommand(),
                new EditSubCommand(),
                new FriendlyFireSubCommand(),
                new InviteSubCommand(),
                new KickSubCommand(),
                new LeaveSubCommand(),
                new ListSubCommand(),
                new MembersSubCommand(),
                new NoticeboardSubCommand(),
                new PromoteSubCommand(),
                new ReagroupSubCommand(),
                new RejectSubCommand(),
                new RivalSubCommand(),
                new BankAdminSubCommand(),
                new DemoteAdminSubCommand(),
                new ForceJoinSubCommand(),
                new FriendlyFireAdminSubCommand(),
                new PromoteAdminSubCommand(),
                new ReloadSubCommand(),
                new SpySubCommand()
        ).forEach(this::register);
    }

    private void register(SubCommand subCommand) {
        subCommands.add(subCommand);
    }

    private Optional<SubCommand> getSubCommand(String name) {
        return subCommands.stream().filter(cmd -> cmd.matches(name)).findFirst();
    }

    public static void commandList(CommandSender s) {
        s.sendMessage(Utils.c("&9&lMineSkyGuildas &7v" + MineSkyGuildas.getInstance().getDescription().getVersion()));
        s.sendMessage(Utils.c("&8----------------------------------------"));
        subCommands.stream()
                .filter(sub -> !sub.getAdminCommand() || s.hasPermission("mineskyguildas.admin"))
                .forEach(sub -> s.sendMessage(Utils.c("&b" + sub.getUsage() + " &8- &7" + sub.getDescription())));
        s.sendMessage(Utils.c("&8----------------------------------------"));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender s, @NotNull Command cmd, @NotNull String lbl, @NotNull String[] args) {
        if (!(s instanceof Player player)) {
            s.sendMessage(Utils.c("&c❌ Este comando só pode ser executado por jogadores."));
            return true;
        }

        if (args.length == 0) {
            GuildMenu.openMainMenu(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("ajuda")) {
            commandList(s);
            return true;
        }

        if (args[0].equalsIgnoreCase("admin")) {
            if (!player.hasPermission("mineskyguildas.admin")) {
                sendError(player, "&c❌ Você não tem permissão para usar comandos de admin.");
                return true;
            }

            if (args.length < 2) {
                sendError(player, "&c❌ Use: /guilda admin <subcomando>");
                return true;
            }

            String adminCmdName = args[1];
            Optional<SubCommand> adminSub = getSubCommand(adminCmdName)
                    .filter(SubCommand::getAdminCommand);

            if (adminSub.isEmpty()) {
                sendError(player, "&c❌ Subcomando admin inválido.");
                return true;
            }

            adminSub.get().perform(player, Arrays.copyOfRange(args, 1, args.length));
            return true;
        }

        getSubCommand(args[0]).ifPresentOrElse(sub -> {
            if (sub.getAdminCommand()) return;
            sub.perform(player, args);
        }, () -> sendError(player, "&c❌ Comando inválido. Use &f/guilda ajuda &cpara ver os comandos disponíveis."));

        return true;
    }

    public static void sendError(Player player, String message) {
        player.sendMessage(Utils.c(message));
        player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
    }
}
