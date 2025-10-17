package net.mineskyguildas.commands.subcommands.guild;

import net.mineskyguildas.commands.subcommands.SubCommand;
import net.mineskyguildas.data.Guilds;
import net.mineskyguildas.enums.GuildRoles;
import net.mineskyguildas.handlers.GuildHandler;
import net.mineskyguildas.utils.Utils;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.EnumSet;
import java.util.List;

import static net.mineskyguildas.commands.GuildCommand.sendError;

public class BannerSubCommand extends SubCommand {
    @Override
    public String getName() {
        return "estandarte";
    }

    @Override
    public String getDescription() {
        return "Alterar o estandarte da guilda";
    }

    @Override
    public String getUsage() {
        return "/guilda estandarte (segurando estandarte na mão)";
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
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!item.getType().name().endsWith("_BANNER")) {
            sendError(player,"&cSegure um estandarte na mão principal para alterar o da sua guilda.");
            return;
        }
        Guilds guild = GuildHandler.getGuildByPlayer(player.getUniqueId());
        if (guild == null) {
            sendError(player,"&cVocê ainda não pertence a nenhuma guilda.");
            return;
        }

        GuildRoles cargo = guild.getRole(player.getUniqueId());
        if (!EnumSet.of(GuildRoles.LEADER, GuildRoles.SUB_LEADER).contains(cargo)) {
            sendError(player, "&cSomente líderes ou sub-líderes têm autoridade para mudar o estandarte da guilda.");
            return;
        }

        GuildHandler.setBanner(item, guild);
        player.sendMessage(Utils.c("&aEstandarte alterado! Sua guilda está com nova identidade visual."));
        GuildHandler.broadcastGuildMessage(guild, "&3\uD83C\uDFF4 &b" + player.getName() + " &3atualizou o estandarte da guilda.");
        player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BIT, 1f, 1f);
    }
}
