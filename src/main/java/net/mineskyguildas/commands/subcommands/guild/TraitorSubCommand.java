package net.mineskyguildas.commands.subcommands.guild;

import net.mineskyguildas.MineSkyGuildas;
import net.mineskyguildas.commands.subcommands.SubCommand;
import net.mineskyguildas.data.Guilds;
import net.mineskyguildas.data.MemberData;
import net.mineskyguildas.handlers.GuildHandler;
import net.mineskyguildas.handlers.TraitorHandler;
import net.mineskyguildas.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class TraitorSubCommand extends SubCommand {

    @Override
    public String getName() {
        return "traidor";
    }

    @Override
    public String getDescription() {
        return "Expulsa e rotula permanentemente um membro como Traidor Global.";
    }

    @Override
    public String getUsage() {
        return "/clan traidor <jogador>";
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("traitor");
    }

    @Override
    public boolean getAdminCommand() {
        return false;
    }

    @Override
    public void perform(Player player, String[] args) {
        Guilds myGuild = GuildHandler.getGuildByPlayer(player);
        if (myGuild == null) {
            player.sendMessage(Utils.c("&c❌ Você não pertence a nenhuma guilda!"));
            return;
        }

        if (!myGuild.getLeader().equals(player.getUniqueId())) {
            player.sendMessage(Utils.c("&c❌ Apenas o Líder do clã pode designar membros como traidores!"));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(Utils.c("&c❌ Use: /clan traidor <nome do jogador>"));
            return;
        }

        String targetName = args[1];
        Player target = Bukkit.getPlayer(targetName);

        UUID targetUuid;
        String finalName;

        if (target != null) {
            targetUuid = target.getUniqueId();
            finalName = target.getName();
        } else {
            OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(targetName);
            if (!offlineTarget.hasPlayedBefore() && !offlineTarget.isOnline()) {
                player.sendMessage(Utils.c("&c❌ Jogador nunca entrou no servidor!"));
                return;
            }
            targetUuid = offlineTarget.getUniqueId();
            finalName = offlineTarget.getName();
        }

        if (targetUuid.equals(player.getUniqueId())) {
            player.sendMessage(Utils.c("&c❌ Você não pode trair a si mesmo!"));
            return;
        }

        if (!myGuild.getMembers().containsKey(targetUuid)) {
            player.sendMessage(Utils.c("&c❌ Este jogador não pertence ao seu clã!"));
            return;
        }

        MemberData targetMemberData = myGuild.getMemberData(targetUuid);
        if (targetMemberData != null) {
            long joinedAt = targetMemberData.getJoinedAt();
            long elapsed = System.currentTimeMillis() - joinedAt;
            long oneDayMs = 24L * 60 * 60 * 1000;

            if (elapsed < oneDayMs) {
                player.sendMessage(Utils.c("&c❌ Você não pode declarar este jogador como traidor ainda! Membros novos precisam estar no clã por mais tempo."));
                return;
            }
        }

        TraitorHandler traitorManager = MineSkyGuildas.getInstance().getTraitorHandler();
        traitorManager.addTraitor(targetUuid, myGuild.getName());
        MineSkyGuildas.l.info("[Clãs] " + player.getName() + " definou o  " +finalName + " como traidor global. Ex-Membro do clã " + myGuild.getName());
        myGuild.removeMember(targetUuid);
        GuildHandler.saveGuildas();

        if (target != null && target.isOnline()) {
            Location loc = target.getLocation();
            Bukkit.getRegionScheduler().run(MineSkyGuildas.getInstance(), loc, task -> {
                loc.getWorld().strikeLightningEffect(loc);
            });
        }

        Bukkit.broadcastMessage(" ");
        Bukkit.broadcastMessage(Utils.c("&4&lNOVO TRAIDOR NO SERVIDOR!"));
        Bukkit.broadcastMessage(Utils.c("&cO líder do clã &f" + myGuild.getName() + " &cdeclarou o jogador &l"+finalName+ " &ccomo um TRAIDOR GLOBAL! Ele foi expuslo e sua cabeça vale XP!"));
        Bukkit.broadcastMessage(" ");

        Bukkit.getOnlinePlayers().forEach(p -> {
            p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_AMBIENT, 1.0f, 1.0f);
        });
        if (target != null) {
            target.sendMessage(Utils.c("&4\uD83D\uDEE1 &cVocê foi rotulado como TRAIDOR pelo líder do seu antigo clã e foi expulso."));
        }
    }
}