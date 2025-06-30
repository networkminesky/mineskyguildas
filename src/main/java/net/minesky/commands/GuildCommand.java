package net.minesky.commands;

import net.minesky.MineSkyGuildas;
import net.minesky.api.events.*;
import net.minesky.api.events.playerevents.PlayerJoinGuildEvent;
import net.minesky.api.events.playerevents.PlayerLeaveGuildEvent;
import net.minesky.builders.GuildBuilder;
import net.minesky.config.Config;
import net.minesky.enums.GuildRoles;
import net.minesky.gui.GuildCreateMenu;
import net.minesky.handlers.GuildHandler;
import net.minesky.handlers.requests.GuildRequestHandler;
import net.minesky.handlers.requests.GuildRequestType;
import net.minesky.hooks.Vault;
import net.minesky.utils.ChatInputCallback;
import net.minesky.data.Guilds;
import net.minesky.data.Notice;
import net.minesky.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

public class GuildCommand implements CommandExecutor {
    private final MineSkyGuildas plugin;

    public GuildCommand(MineSkyGuildas plugin) {
        this.plugin = plugin;
    }

    void commandList(CommandSender s) {
        s.sendMessage(Utils.BLUE_COLOR+Utils.c("&lMineSkyGuildas v"+ MineSkyGuildas.getInstance().getDescription().getVersion()));
        s.sendMessage(Utils.c(
                Utils.BLUE_COLOR+"/guilda criar &8- &7Criar uma nova guilda.\n"+
                        Utils.BLUE_COLOR+"/guilda acabar &8- &7Deletar sua guilda atual.\n"+
                        Utils.BLUE_COLOR+"/guilda abandonar &8- &7Abandonar sua guilda atual.\n"+
                        Utils.BLUE_COLOR+"/guilda convidar <jogador> &8- &7Convidar um jogador para sua guilda.\n"+
                        Utils.BLUE_COLOR+"/guilda aliado <adicionar/remover> <guilda> &8- &7Gerenciar alianças.\n"+
                        Utils.BLUE_COLOR+"/guilda rival <adicionar/remover> <guilda> &8- &7Gerenciar rivais.\n"+
                        Utils.BLUE_COLOR+"/guilda estandarte (segurando estandarte na mão) &8- &7Alterar o estandarte da guilda.\n"+
                        Utils.BLUE_COLOR+"/guilda menu &8- &7Abrir o menu principal da guilda.\n"+
                        Utils.BLUE_COLOR+"/guilda reload &8- &7Recarregar as configurações do plugin &c(não recomendado).\n"
        ));
    }
    public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
        if (!(s instanceof Player player)) {
            s.sendMessage(Utils.c("&c❌ Este comando só pode ser executado por jogadores."));
            return true;
        }

        GuildHandler handler = plugin.getGuildHandler();

        if (args.length < 1) {
            //GuildCreateMenu.openMainMenu(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "criar" -> createHandler(player, handler);
            case "estandarte" -> bannerHandler(player, handler);
            case "aliado" -> allyHandler(player, handler, args);
            case "rival" -> rivalHandler(player, handler, args);
            case "sair", "abandonar" -> leaveHandler(player, handler);
            case "acabar", "desbandar" -> disbandHandler(player, handler);
            case "aceitar" -> acceptHandler(player, handler);
            case "rejeitar" -> rejectHandler(player, handler);
            case "convidar" -> inviteHandler(player, args, handler);
            case "anunciar" -> annuncerHandler(player, handler, args);
            case "mural" -> noticeboardHandler(player, handler);
            case "ajuda", "help" -> commandList(s);
            default -> {
                player.sendMessage(Utils.c("&c❌ Comando inválido. Use &f/guilda ajuda &cpara ver os comandos disponíveis."));
                player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
            }
        }
        return true;
    }

    private void createHandler(Player player, GuildHandler handler) {
        if (handler.hasGuild(player)) {
            sendError(player, "&c⚠ Você já está ligado a uma guilda. Rompa os laços antes de criar ou buscar nova aliança.");
            return;
        }
        GuildCreateMenu.openMainMenu(player, new GuildBuilder(player.getUniqueId()));
    }

    private void bannerHandler(Player player, GuildHandler handler) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || !item.getType().name().endsWith("_BANNER")) {
            sendError(player,"&cSegure um estandarte na mão principal para alterar o da sua guilda.");
            return;
        }
        Guilds guild = handler.getGuildByPlayer(player.getUniqueId());
        if (guild == null) {
            sendError(player,"&cVocê ainda não pertence a nenhuma guilda.");
            return;
        }

        GuildRoles cargo = guild.getRole(player.getUniqueId());
        if (!EnumSet.of(GuildRoles.LEADER, GuildRoles.SUB_LEADER).contains(cargo)) {
            sendError(player, "&cSomente líderes ou sub-líderes têm autoridade para mudar o estandarte da guilda.");
            return;
        }

        handler.setBanner(item, guild);
        player.sendMessage(Utils.c("&aEstandarte alterado! Sua guilda está com nova identidade visual."));
        handler.broadcastGuildMessage(guild, "&3\uD83C\uDFF4 &b" + player.getName() + " &3atualizou o estandarte da guilda.");
        player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BIT, 1f, 1f);
    }

    private void annuncerHandler(Player player, GuildHandler handler, String[] args) {
        if (!handler.hasGuild(player)) {
            sendError(player, "&4⚠ &cVocê não pertence a nenhuma guilda no momento.");
            return;
        }

        Guilds guild = handler.getGuildByPlayer(player.getUniqueId());
        if (!(GuildRoles.isLeadership(guild.getRole(player.getUniqueId())))) {
            sendError(player, "&4⚠ &cApenas o &lCAPITÕES&r &cda guilda pode anunciar.");
            return;
        }

        if (args[1].isEmpty()) {
            sendError(player, "&4⚠ &cVocê deve indicar uma mensagem para fazer um anúncio.");
            return;
        }

        String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        handler.addNotice(guild, message, player);
        handler.broadcastGuildMessageNoNotice(guild, Utils.c("&b✉ &3" + player.getName() + "&8: &f" + message));
    }

    private void noticeboardHandler(Player player, GuildHandler handler) {
        if (!handler.hasGuild(player)) {
            sendError(player, "&4⚠ &cVocê não pertence a nenhuma guilda no momento.");
            return;
        }

        Guilds guild = handler.getGuildByPlayer(player.getUniqueId());
        List<Notice> notices = guild.getNoticeBoard();

        player.sendMessage(Utils.c("&e✉ &6Mural da Guilda &e" + guild.getName() + ":"));

        if (notices.isEmpty()) {
            player.sendMessage(Utils.c("&7(sem mensagens ainda)"));
            return;
        }

        for (int i = 0; i < notices.size(); i++) {
            String message = notices.get(i).getMessage();
            player.sendMessage(Utils.c("&7" + (i + 1) + ". " + message));
        }
    }

    private void disbandHandler(Player player, GuildHandler handler) {
        if (!handler.hasGuild(player)) {
            sendError(player, "&4⚠ &cVocê não pertence a nenhuma guilda no momento.");
            return;
        }

        Guilds guild = handler.getGuildByPlayer(player.getUniqueId());
        if (!(guild.getRole(player.getUniqueId()) == GuildRoles.LEADER)) {
            sendError(player, "&4⚠ &cApenas o &lLÍDER&r &cda guilda pode desbandar.");
            return;
        }

        confirmAction(player, "desbandar sua guilda", () -> {
            GuildDisbandEvent event = new GuildDisbandEvent(player, guild);
            plugin.getServer().getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                player.sendMessage((event.CancelledMessage == null? Utils.c("&c⚠ Ops! A entrada na guilda foi interrompida pela API.") : event.CancelledMessage));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO,1, 1);
                return;
            }
            Bukkit.broadcastMessage(Utils.c("&4⛔ &cA guilda &f" + guild.getName() + " &cfoi desbandada."));
            handler.deleteGuild(guild.getId());
            player.sendMessage(Utils.c("&2✅ &aGuilda desbandada com sucesso."));
        });
    }

    private void leaveHandler(Player player, GuildHandler handler) {
        if (!handler.hasGuild(player)) {
            sendError(player, "&4⚠ &cVocê não pertence a nenhuma guilda no momento.");
            return;
        }

        Guilds guild = handler.getGuildByPlayer(player.getUniqueId());
        if (guild.getRole(player.getUniqueId()) == GuildRoles.LEADER) {
            sendError(player, "&4⚠ &cVocê é o líder da guilda. Para sair, finalize a guilda usando &f/guildas acabar");
            return;
        }

        confirmAction(player, "confirmar a saída da guilda", () -> {
            PlayerLeaveGuildEvent event = new PlayerLeaveGuildEvent(player, guild);
            plugin.getServer().getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                player.sendMessage((event.CancelledMessage == null? Utils.c("&c⚠ Ops! A saída da guilda foi interrompida pela API.") : event.CancelledMessage));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO,1, 1);
                return;
            }
            handler.broadcastGuildMessage(guild, "&c⛔ &4" + player.getName() + " &csaiu da guilda.");
            handler.removeMember(player, guild);
            player.sendMessage(Utils.c("&a✅ Você saiu da guilda."));
        });
    }

    private void acceptHandler(Player player, GuildHandler handler) {
        UUID playerId = player.getUniqueId();
        Guilds ownGuild = handler.getGuildByPlayer(playerId);
        GuildRequestHandler allyHandler = plugin.getRequestManager().getHandler(GuildRequestType.ALLY);
        GuildRequestHandler rivalHandler = plugin.getRequestManager().getHandler(GuildRequestType.RIVAL);

        boolean hasInvite = plugin.getInviteHandler().hasInvite(playerId);
        boolean hasAllyRequest = allyHandler.hasRequest(ownGuild);
        boolean hasRivalRequest = rivalHandler.hasRequest(ownGuild);

        if (!hasInvite && !hasAllyRequest && !hasRivalRequest) {
            sendError(player, "&c❌ Seu convite ou pedido de aliança/paz expirou.");
            return;
        }

        if (hasAllyRequest) {
            Guilds allyGuild = allyHandler.getRequestGuild(ownGuild);

            GuildAddAllyEvent event = new GuildAddAllyEvent(allyGuild, ownGuild, rivalHandler.getRequester(ownGuild), player);
            plugin.getServer().getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                player.sendMessage((event.CancelledMessage == null ? Utils.c("&c⚠ Ops! A entrada na guilda foi interrompida pela API.") : event.CancelledMessage));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                return;
            }
            player.sendMessage(Utils.c("&2🤝 &aVocê aceitou o pedido de aliança da guilda &2" + allyGuild.getName() + "&a!"));
            handler.addAlly(allyGuild, ownGuild, player);
            allyHandler.removeRequest(ownGuild);
            player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BIT, 1f, 1f);
        }

        if (hasRivalRequest) {
            Guilds g = rivalHandler.getRequestGuild(ownGuild);

            GuildRemoveRivalEvent event = new GuildRemoveRivalEvent(g, ownGuild, rivalHandler.getRequester(ownGuild), player);
            plugin.getServer().getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                player.sendMessage((event.CancelledMessage == null ? Utils.c("&c⚠ Ops! A entrada na guilda foi interrompida pela API.") : event.CancelledMessage));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                return;
            }
            player.sendMessage(Utils.c("&2\uD83C\uDFF4 &aVocê aceitou o pedido de paz da guilda &2" + g.getName() + "&a!"));
            handler.removeRival(g, ownGuild, player);
            rivalHandler.removeRequest(ownGuild);
            player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BIT, 1f, 1f);
        }

        if (hasInvite) {
            if (handler.hasGuild(player)) {
                sendError(player, "&4⚠ &CVocê já faz parte de uma guilda. Saia dela antes de aceitar outro convite.");
                return;
            }

            Guilds guild = plugin.getInviteHandler().getInviteGuild(playerId);
            PlayerJoinGuildEvent event = new PlayerJoinGuildEvent(player, guild);
            plugin.getServer().getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                player.sendMessage((event.CancelledMessage == null ? Utils.c("&c⚠ Ops! A entrada na guilda foi interrompida pela API.") : event.CancelledMessage));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                return;
            }

            handler.addMember(player, guild);
            String msg = "&3🏰 &b" + player.getName() + " &3entrou na guilda &b" + guild.getName() + "&3!";
            Bukkit.broadcastMessage(Utils.c(msg));
            handler.addNotice(guild, Utils.c(msg));
            player.sendMessage(Utils.c("&a✅ Você entrou na guilda &2" + guild.getName() + "&a com sucesso!"));

            plugin.getInviteHandler().removeInvite(playerId);
            player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BIT, 1f, 1f);
        }
    }


    private void rejectHandler(Player player, GuildHandler handler) {
        UUID playerId = player.getUniqueId();
        Guilds ownGuild = handler.getGuildByPlayer(playerId);
        GuildRequestHandler allyHandler = plugin.getRequestManager().getHandler(GuildRequestType.ALLY);
        GuildRequestHandler rivalHandler = plugin.getRequestManager().getHandler(GuildRequestType.RIVAL);

        boolean hasInvite = plugin.getInviteHandler().hasInvite(playerId);
        boolean hasAllyRequest = allyHandler.hasRequest(ownGuild);
        boolean hasRivalRequest = rivalHandler.hasRequest(ownGuild);

        if (!hasInvite && !hasAllyRequest && !hasRivalRequest) {
            sendError(player, "&c❌ Seu convite ou pedido de aliança/paz expirou.");
            return;
        }

        if (hasAllyRequest) {
            Guilds allyGuild = allyHandler.getRequestGuild(ownGuild);

            handler.broadcastGuildMessage(allyGuild, Utils.c("&c✘ O pedido de aliança feito para &4" + ownGuild.getName() + " &cfoi rejeitado."));
            handler.broadcastGuildMessage(ownGuild, Utils.c("&c✘ &4" + player.getName() + " &crejeitou o pedido de aliança da guilda &4" + allyGuild.getName() + "&c."));
            player.sendMessage(Utils.c("&c✘ Você rejeitou o pedido de aliança da guilda &4" + allyGuild.getName() + "&c."));

            allyHandler.removeRequest(ownGuild);
            player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BIT, 1f, 1f);
        }

        if (hasRivalRequest) {
            Guilds g = rivalHandler.getRequestGuild(ownGuild);

            handler.broadcastGuildMessage(g, Utils.c("&c✘ O pedido de paz feito para &4" + ownGuild.getName() + " &cfoi rejeitado."));
            handler.broadcastGuildMessage(ownGuild, Utils.c("&c✘ &4" + player.getName() + " &crejeitou o pedido de paz da guilda &4" + g.getName() + "&c."));
            player.sendMessage(Utils.c("&c✘ Você rejeitou o pedido de paz da guilda &4" + g.getName() + "&c."));

            rivalHandler.removeRequest(ownGuild);
            player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BIT, 1f, 1f);
        }

        if (hasInvite) {
            if (handler.hasGuild(player)) {
                sendError(player, "&4⚠ &CVocê já faz parte de uma guilda. Saia dela antes de rejeitar outro convite.");
                return;
            }

            Guilds guild = plugin.getInviteHandler().getInviteGuild(playerId);
            handler.broadcastGuildMessage(guild, Utils.c("&c✘ &4" + player.getName() + " &crejeitou o convite para entrar na guilda."));
            player.sendMessage(Utils.c("&c✘ Você rejeitou o convite da guilda &4" + guild.getName() + "&c."));

            plugin.getInviteHandler().removeInvite(playerId);
            player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BIT, 1f, 1f);
        }
    }


    private void rivalHandler(Player player, GuildHandler handler, String[] args) {
        if (args.length < 3) {
            sendError(player, "&c❗ Uso correto: &f/guilda rival <adicionar/remover> <guilda>");
            return;
        }

        Guilds guild = handler.getGuildByPlayer(player.getUniqueId());
        if (guild == null) {
            sendError(player, "&c🚫 Você não faz parte de nenhuma guilda.");
            return;
        }

        GuildRoles cargo = guild.getRole(player.getUniqueId());
        if (!EnumSet.of(GuildRoles.LEADER, GuildRoles.SUB_LEADER).contains(cargo)) {
            sendError(player, "&c🔒 Você não tem permissão para declarar rivalidade.");
            return;
        }

        Guilds guildaTarget = handler.getGuildByTag(args[2]);

        if (guildaTarget == null) {
            sendError(player, "&c❌ Essa guilda não existe.");
            return;
        }
        switch (args[1].toLowerCase()) {
            case "adicionar", "add" -> {
                if (guildaTarget.getName().equalsIgnoreCase(guild.getName())) {
                    sendError(player, "&4🤦 &CVocê não pode criar rivalidade com sua própria guilda.");
                    return;
                }

                if (guild.isAlly(guildaTarget)) {
                    sendError(player, "&c⚠ A guilda &f" + guildaTarget.getName() + " &cé sua aliada. Remova a aliança antes de declarar rivalidade.");
                    return;
                }

                if (guild.isRival(guildaTarget)) {
                    sendError(player, "&4😠 &CSua guilda já é rival da &f" + guildaTarget.getName() + ".");
                    return;
                }

                GuildAddRivalEvent event = new GuildAddRivalEvent(guild, guildaTarget, player);
                plugin.getServer().getPluginManager().callEvent(event);
                if (event.isCancelled()) {
                    player.sendMessage((event.CancelledMessage == null ? Utils.c("&c⚠ Ops! A entrada na guilda foi interrompida pela API.") : event.CancelledMessage));
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                    return;
                }
                handler.addRival(guild, guildaTarget, player);
                player.sendMessage(Utils.c("&4⚔ &cVocê declarou rivalidade com a guilda &f" + guildaTarget.getName() + "&c!"));
            }

            case "remover", "remove" -> {
                GuildRequestHandler rivalHandler = plugin.getRequestManager().getHandler(GuildRequestType.RIVAL);

                if (guildaTarget.getName().equalsIgnoreCase(guild.getName())) {
                    sendError(player, "&4🤔 &CVocê não pode remover rivalidade com sua própria guilda.");
                    return;
                }

                if (!guild.isRival(guildaTarget)) {
                    sendError(player, "&4📛 &cSua guilda não é rival da &f" + guildaTarget.getName() + ".");
                    return;
                }

                if (rivalHandler.hasRequest(guildaTarget) &&
                        rivalHandler.getRequestGuild(guildaTarget).getId().equals(guild.getId())) {
                    sendError(player, "&4📨 &cO pedido de paz já foi enviado para essa guilda.");
                    return;
                }

                handler.broadcastGuildMessage(guild, Utils.c("&3📨 &b" + player.getName() + " &3enviou um pedido de paz para &f" + guildaTarget.getName() + "&3."));
                rivalHandler.sendRequest(guild, guildaTarget, player);
                player.sendMessage(Utils.c("&a✔ Um pedido de paz para &f" + guildaTarget.getName() + " &afoi enviada com sucesso."));
            }

            default -> {
                sendError(player, "&c❗ Subcomando inválido. Use: &f/adicionar &cou &f/remover");
            }
        }
    }


    private void allyHandler(Player player, GuildHandler handler, String[] args) {
        if (args.length < 3) {
            sendError(player, "&c❗ Uso correto: &f/guilda aliado <adicionar/remover> <guilda>");
            return;
        }

        Guilds guild = handler.getGuildByPlayer(player.getUniqueId());
        if (guild == null) {
            sendError(player, "&c🚫 Você não faz parte de nenhuma guilda.");
            return;
        }

        GuildRoles role = guild.getRole(player.getUniqueId());
        if (!EnumSet.of(GuildRoles.LEADER, GuildRoles.SUB_LEADER).contains(role)) {
            sendError(player, "&c🔒 Você não tem permissão para gerenciar alianças.");
            return;
        }

        GuildRequestHandler allyHandler = plugin.getRequestManager().getHandler(GuildRequestType.ALLY);

        switch (args[1].toLowerCase()) {
            case "adicionar", "add" -> {
                Guilds guildaTarget = handler.getGuildByTag(args[2]);
                if (guildaTarget == null) {
                    sendError(player, "&c❌ Essa guilda não existe.");
                    return;
                }

                if (guildaTarget.getName().equalsIgnoreCase(guild.getName())) {
                    sendError(player, "&4🤦 &cVocê não pode formar aliança com sua própria guilda.");
                    return;
                }

                if (guild.isRival(guildaTarget)) {
                    sendError(player, "&4⚔ &cNão é possível formar aliança com &f" + guildaTarget.getName() + " &cpois são rivais.");
                    return;
                }

                if (guild.isAlly(guildaTarget)) {
                    sendError(player, "&4🤝 &cSua guilda já é aliada da &f" + guildaTarget.getName() + ".");
                    return;
                }

                if (allyHandler.hasRequest(guildaTarget) &&
                        allyHandler.getRequestGuild(guildaTarget).getId().equals(guild.getId())) {
                    sendError(player, "&4📨 &cO pedido de aliança já foi enviado para essa guilda.");
                    return;
                }

                handler.broadcastGuildMessage(guild, Utils.c("&3📨 &b" + player.getName() + " &3enviou um pedido de aliança para &f" + guildaTarget.getName() + "&3."));
                allyHandler.sendRequest(guild, guildaTarget, player);
            }
            case "remover", "remove" -> {
                Guilds guildaTarget = handler.getGuildByTag(args[2]);
                if (guildaTarget == null) {
                    sendError(player, "&c❌ Essa guilda não existe.");
                    return;
                }

                if (guildaTarget.getName().equalsIgnoreCase(guild.getName())) {
                    sendError(player, "&4🤔 &cVocê não pode remover a aliança com sua própria guilda.");
                    return;
                }

                if (!guild.isAlly(guildaTarget)) {
                    sendError(player, "&c❌ Sua guilda não possui aliança com &f" + guildaTarget.getName() + "&c.");
                    return;
                }

                GuildRemoveAllyEvent event = new GuildRemoveAllyEvent(guild, guildaTarget, player);
                plugin.getServer().getPluginManager().callEvent(event);
                if (event.isCancelled()) {
                    player.sendMessage((event.CancelledMessage == null ? Utils.c("&c⚠ Ops! A entrada na guilda foi interrompida pela API.") : event.CancelledMessage));
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                    return;
                }
                handler.removeAlly(guild, guildaTarget, player);
            }
            default -> {
                sendError(player, "&c❗ Subcomando inválido. Use: &f/guilda adicionar &cou &f/guilda remover");
            }
        }
    }



    private void inviteHandler(Player player, String[] args, GuildHandler handler) {
        if (args.length < 2) {
            sendError(player, "&4⚠ &cUso correto: /guilda convidar <jogador>");
            return;
        }

        Guilds guild = handler.getGuildByPlayer(player.getUniqueId());
        if (guild == null) {
            sendError(player, "&4⚠ &CVocê não faz parte de uma guilda.");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null || !target.isOnline()) {
            sendError(player, "&4⚠ &CJogador não encontrado ou está offline.");
            return;
        }

        if (target.equals(player)) {
            sendError(player, "&4⚠ &cVocê não pode convidar a si mesmo.");
            return;
        }

        if (handler.hasGuild(target)) {
            sendError(player, "&4⚠ &cEste jogador já faz pertence a uma guilda.");
            return;
        }

        GuildRoles cargo = guild.getRole(player.getUniqueId());
        if (!EnumSet.of(GuildRoles.LEADER, GuildRoles.SUB_LEADER, GuildRoles.CAPTAIN, GuildRoles.RECRUITER).contains(cargo)) {
            sendError(player, "&4⚠ &cApenas membros autorizados podem convidar jogadores para a guilda.");
            return;
        }

        if (guild.getMembers().size() - 1 >= guild.getMemberLimit()) {
            sendError(player, "&4⚠ &cSua guilda atingiu o limite de membros (" + guild.getMemberLimit() + ")." + (guild.getLevel() >= 6 ? "" : " Suba o nível da guilda para expandir esse limite!"));
            return;
        }

        if (plugin.getInviteHandler().hasInvite(target.getUniqueId())
                && plugin.getInviteHandler().getInviteGuild(player.getUniqueId()).getId().equals(guild.getId())) {
            sendError(player, "&4⚠ &cEste jogador já foi convidado por sua guilda.");
            return;
        }

        if (!Vault.withdraw(player, Config.GuildInvitePrice)) {
            sendError(player, "&4⚠ &cVocê precisa de &4$" + Config.GuildInvitePrice + " &cpara enviar um convite.");
            return;
        }

        GuildInviteEvent event = new GuildInviteEvent(player, target, guild);
        plugin.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            player.sendMessage((event.CancelledMessage == null? Utils.c("&c⚠ Ops! O convite foi interrompida pela API.") : event.CancelledMessage));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO,1, 1);
            Vault.deposit(player, Config.GuildInvitePrice);
            return;
        }
        plugin.getInviteHandler().sendInvite(target, guild);
        player.sendMessage(Utils.c("&4\uD83D\uDCB5 &cVocê gastou &4$" + Config.GuildInvitePrice + " &cpara convidar &4" + target.getName() + " &cpara a guilda."));
        handler.broadcastGuildMessage(guild, "&3\uD83D\uDCE9 &b" + target.getName() + " &3foi convidado para a guilda por &b" + player.getName() + "&3.");
        player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BIT, 1f, 1f);
    }

    private void confirmAction(Player player, String action, Runnable onConfirm) {
        player.sendMessage(Utils.c("&3\uD83D\uDEA8 &9&lATENÇÃO&7: Digite &e\"Confirmar\" &7no chat para " + action + "."));
        Utils.awaitChatInput(player, new ChatInputCallback() {
            public void onInput(String input) {
                if (input.equalsIgnoreCase("confirmar") || input.equalsIgnoreCase("sim")) {
                    onConfirm.run();
                } else {
                    player.sendMessage(Utils.c("&c❌ Cancelado."));
                }
            }

            public void onCancel() {
                player.sendMessage(Utils.c("&c⏳ Tempo esgotado. Cancelado."));
            }
        });
    }

    private void sendError(Player player, String message) {
        player.sendMessage(Utils.c(message));
        player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
    }
}
