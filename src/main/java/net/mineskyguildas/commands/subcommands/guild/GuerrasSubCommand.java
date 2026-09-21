package net.mineskyguildas.commands.subcommands.guild;

import net.mineskyguildas.MineSkyGuildas;
import net.mineskyguildas.commands.subcommands.SubCommand;
import net.mineskyguildas.data.Guilds;
import net.mineskyguildas.enums.GuildRoles;
import net.mineskyguildas.handlers.GuildHandler;
import net.mineskyguildas.handlers.WarHandler;
import net.mineskyguildas.hooks.MineSkyVanishHook;
import net.mineskyguildas.utils.Utils;
import net.mineskyguildas.war.WarSession;
import net.mineskyguildas.war.WarState;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class GuerrasSubCommand extends SubCommand {
    @Override
    public String getName() {
        return "guerras";
    }

    @Override
    public String getDescription() {
        return "Veja todas as guerras ativas";
    }

    @Override
    public String getUsage() {
        return "/clan guerras";
    }

    @Override
    public List<String> getAliases() {
        return List.of();
    }

    @Override
    public boolean getAdminCommand() {
        return false;
    }

    @Override
    public void perform(Player player, String[] args) {
        WarHandler warHandler = MineSkyGuildas.getInstance().getWarHandler();
        Map<UUID, WarSession> sessions = warHandler.getActiveSessions();

        List<WarSession> wars = sessions.values().stream()
                .filter(s -> s.getState() == WarState.SCHEDULED
                        || s.getState() == WarState.DECLARED
                        || s.getState() == WarState.ACTIVE)
                .collect(Collectors.toList());

        if (args.length > 1) {
            String filter = args[1].toLowerCase();
            wars = wars.stream()
                    .filter(s -> s.getGuild1().getTag().toLowerCase().contains(filter)
                            || s.getGuild1().getName().toLowerCase().contains(filter)
                            || s.getGuild2().getTag().toLowerCase().contains(filter)
                            || s.getGuild2().getName().toLowerCase().contains(filter))
                    .collect(Collectors.toList());
        }

        if (wars.isEmpty()) {
            player.sendMessage(Utils.c("&c❌ Não há nenhuma guerra agendada ou em andamento no momento."));
            return;
        }

        player.sendMessage(Utils.c("&r\n&4&l══════ ⚔ GUERRAS DO SERVIDOR ⚔ ══════"));

        for (WarSession session : wars) {
            String status = getStatusFormatted(session.getState());

            player.sendMessage(Utils.c("\n&4⚔ &f" + session.getGuild1().getName() + " &8[" + session.getGuild1().getTag() + "&8] "
                    + "&7vs &f" + session.getGuild2().getName() + " &8[" + session.getGuild2().getTag() + "&8] "
                    + status));

            renderSideInfo(player, session.getGuild1(), session.getGuild1Supporters(), "&9");

            renderSideInfo(player, session.getGuild2(), session.getGuild2Supporters(), "&c");

            if (session.getState() == WarState.ACTIVE) {
                Player leader1 = Bukkit.getPlayer(session.getGuild1().getLeader());
                if (leader1 == null || !leader1.isOnline() || MineSkyVanishHook.isPlayerVanished(leader1)) {
                    player.sendMessage(Utils.c("  &4⚠ &cLíder de &f" + session.getGuild1().getName()
                            + " &cOFFLINE! Derrota em &e" + session.getLeader1OfflineTimeLeft() + "s"));
                }

                Player leader2 = Bukkit.getPlayer(session.getGuild2().getLeader());
                if (leader2 == null || !leader2.isOnline() || MineSkyVanishHook.isPlayerVanished(leader2)) {
                    player.sendMessage(Utils.c("  &4⚠ &cLíder de &f" + session.getGuild2().getName()
                            + " &cOFFLINE! Derrota em &e" + session.getLeader2OfflineTimeLeft() + "s"));
                }
            }
        }

        player.sendMessage(Utils.c("&4&l═════════════════════════════════════\n"));
    }

    private void renderSideInfo(Player sender, Guilds mainGuild, Set<String> supportersIds, String color) {
        List<String> mainOnline = getOnlineMemberTags(mainGuild);
        int totalSideOnline = mainOnline.size();

        Map<Guilds, List<String>> supportersMap = new LinkedHashMap<>();
        if (supportersIds != null) {
            for (String suppId : supportersIds) {
                Guilds supporter = GuildHandler.getGuildByID(suppId);
                if (supporter != null) {
                    List<String> suppOnline = getOnlineMemberTags(supporter);
                    supportersMap.put(supporter, suppOnline);
                    totalSideOnline += suppOnline.size();
                }
            }
        }

        sender.sendMessage(Utils.c(" " + color + "🛡 &l" + mainGuild.getName() + " &8(&a" + totalSideOnline + " online no exército&8):"));

        if (mainOnline.isEmpty()) {
            sender.sendMessage(Utils.c("   &7Membros: &c(Nenhum online)"));
        } else {
            sender.sendMessage(Utils.c("   &7Membros: " + String.join("&7, ", mainOnline)));
        }

        if (!supportersMap.isEmpty()) {
            for (Map.Entry<Guilds, List<String>> entry : supportersMap.entrySet()) {
                Guilds supporter = entry.getKey();
                List<String> suppOnline = entry.getValue();

                if (suppOnline.isEmpty()) {
                    sender.sendMessage(Utils.c("   &eAliado [" + supporter.getName() + "]: &c(Nenhum online)"));
                } else {
                    sender.sendMessage(Utils.c("   &eAliado [" + supporter.getName() + "]: " + String.join("&7, ", suppOnline)));
                }
            }
        }
    }

    private List<String> getOnlineMemberTags(Guilds guild) {
        List<String> list = new ArrayList<>();
        if (guild == null || guild.getMembers() == null) return list;

        for (UUID uuid : guild.getMembers().keySet()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) {
                if (MineSkyVanishHook.isPlayerVanished(p)) {
                    continue;
                }

                boolean isLeader = uuid.equals(guild.getLeader());
                boolean isSub = guild.getRole(uuid) == GuildRoles.SUB_LEADER;

                if (isLeader) {
                    list.add("&c★ " + p.getName());
                } else if (isSub) {
                    list.add("&6☆ " + p.getName());
                } else {
                    list.add("&f" + p.getName());
                }
            }
        }
        return list;
    }

    private String getStatusFormatted(WarState state) {
        switch (state) {
            case SCHEDULED:
                return "&8[&e📅 AGENDADA&8]";
            case DECLARED:
                return "&8[&6⚔ DECLARADA&8]";
            case ACTIVE:
                return "&8[&a🔥 EM COMBATE&8]";
            case PENDING:
                return "&8[&7⏳ PENDENTE&8]";
            default:
                return "&8[&7FINALIZADA&8]";
        }
    }
}