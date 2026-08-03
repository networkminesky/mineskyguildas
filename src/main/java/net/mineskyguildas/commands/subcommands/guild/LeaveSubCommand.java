package net.mineskyguildas.commands.subcommands.guild;

import net.mineskyguildas.MineSkyGuildas;
import net.mineskyguildas.api.events.playerevents.PlayerLeaveGuildEvent;
import net.mineskyguildas.commands.subcommands.SubCommand;
import net.mineskyguildas.data.Guilds;
import net.mineskyguildas.enums.GuildRoles;
import net.mineskyguildas.handlers.GuildHandler;
import net.mineskyguildas.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.List;

import static net.mineskyguildas.commands.GuildCommand.sendError;
import static net.mineskyguildas.utils.Utils.confirmAction;

public class LeaveSubCommand extends SubCommand {
    private final MineSkyGuildas plugin = MineSkyGuildas.getInstance();

    @Override
    public String getName() {
        return "abandonar";
    }

    @Override
    public String getDescription() {
        return "Abandonar seu clã atual";
    }

    @Override
    public String getUsage() {
        return "/clan abandonar";
    }

    @Override
    public List<String> getAliases() {
        return List.of("sair");
    }

    @Override
    public boolean getAdminCommand() {
        return false;
    }

    @Override
    public void perform(Player player, String[] args) {
        if (!GuildHandler.hasGuild(player)) {
            sendError(player, "&4⚠ &cVocê não pertence a nenhum clã no momento.");
            return;
        }

        Guilds guild = GuildHandler.getGuildByPlayer(player.getUniqueId());
        if (guild.getRole(player.getUniqueId()) == GuildRoles.LEADER) {
            sendError(player, "&4⚠ &cVocê é o líder do clã. Para sair, finalize o clã usando &f/clan acabar");
            return;
        }

        confirmAction(player, "confirmar a saída do clã", () -> {
            PlayerLeaveGuildEvent event = new PlayerLeaveGuildEvent(player, guild);
            plugin.getServer().getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                player.sendMessage((event.CancelledMessage == null? Utils.c("&c⚠ Ops! A saída do clã foi interrompida pela API.") : event.CancelledMessage));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO,1, 1);
                return;
            }
            GuildHandler.broadcastGuildMessage(guild, "&c⛔ &4" + player.getName() + " &csaiu do clã.");
            GuildHandler.removeMember(player, guild);
            MineSkyGuildas.l.info("[Clãs] " + player.getName() + " saiu do clã " + guild.getName());
            player.sendMessage(Utils.c("&a✅ Você saiu do clã."));
            if (MineSkyGuildas.getInstance().getWarHandler().isGuildInActiveWar(guild.getId())) {
                MineSkyGuildas.getInstance().getTraitorHandler().addTraitor(player.getUniqueId(), guild.getName());

                final Location location = player.getLocation();
                location.getWorld().strikeLightningEffect(location);

                Bukkit.broadcastMessage(" ");
                Bukkit.broadcastMessage(Utils.c("&4&lNOVO TRAIDOR NO SERVIDOR!"));
                Bukkit.broadcastMessage(Utils.c("&f" + player.getName() + " &cabandonou o clã &f"+guild.getName()+ " &cem plena guerra e tornou-se um TRAIDOR GLOBAL!"));
                Bukkit.broadcastMessage(" ");

                Bukkit.getOnlinePlayers().forEach(p -> {
                    p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_AMBIENT, 1.0f, 1.0f);
                });

                player.sendMessage(Utils.c("&cVocê abandonou o clã em combate. Agora você é um traidor e sua cabeça vale muito XP!"));
            }
        });
    }
}
