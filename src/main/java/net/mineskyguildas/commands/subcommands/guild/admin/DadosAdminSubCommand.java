package net.mineskyguildas.commands.subcommands.guild.admin;

import net.mineskyguildas.MineSkyGuildas;
import net.mineskyguildas.commands.subcommands.SubCommand;
import net.mineskyguildas.data.Guilds;
import net.mineskyguildas.data.MemberData;
import net.mineskyguildas.enums.GuildRoles;
import net.mineskyguildas.handlers.GuildHandler;
import net.mineskyguildas.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class DadosAdminSubCommand extends SubCommand {

    @Override
    public String getName() {
        return "dados";
    }

    @Override
    public String getDescription() {
        return "Gerencia dados de clãs e jogadores (editar, deletar, resetar).";
    }

    @Override
    public String getUsage() {
        return "/clan admin dados [editar|deletar|resetar] [guildas|jogadores] [tag|nick] <campo> <valor>";
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("data");
    }

    @Override
    public boolean getAdminCommand() {
        return true;
    }

    @Override
    public void perform(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(Utils.c("&c❌ Uso correto: " + getUsage()));
            return;
        }

        String action = args[1].toLowerCase();
        String targetType = args[2].toLowerCase();

        if (!targetType.equals("guildas") && !targetType.equals("jogadores")) {
            player.sendMessage(Utils.c("&c❌ Tipo de alvo inválido! Escolha 'guildas' ou 'jogadores'."));
            return;
        }

        if (args.length < 4) {
            player.sendMessage(Utils.c("&c❌ Defina a tag do clã ou o nick do jogador alvo!"));
            return;
        }

        String targetName = args[3];

        if (targetType.equals("guildas")) {
            handleGuildas(player, action, targetName, args);
        } else {
            handleJogadores(player, action, targetName, args);
        }
    }

    private void handleGuildas(Player admin, String action, String tag, String[] args) {
        Guilds guild = GuildHandler.getGuildByTag(tag);
        if (guild == null) {
            admin.sendMessage(Utils.c("&c❌ Clã com a tag '" + tag + "' não foi encontrado!"));
            return;
        }

        switch (action) {
            case "deletar":
                MineSkyGuildas.l.info("[Clãs] [ADMIN] " + admin.getName() + " deletou o clã " + guild.getName());
                GuildHandler.deleteGuild(guild.getId());
                admin.sendMessage(Utils.c("&a✅ O clã &f" + guild.getName() + " &afoi deletado com sucesso!"));
                break;

            case "resetar":
                guild.setLevel(0);
                guild.setXP(0.0);
                guild.setBalance(0);
                guild.setBase(null);
                guild.getNoticeBoard().clear();
                GuildHandler.saveGuildas();
                MineSkyGuildas.l.info("[Clãs] [ADMIN] " + admin.getName() + " resetou os dados do clã " + guild.getName());
                admin.sendMessage(Utils.c("&a✅ Todos os dados do clã &f" + guild.getName() + " &aforam resetados para o padrão de nível 0!"));
                break;

            case "editar":
                if (args.length < 6) {
                    admin.sendMessage(Utils.c("&c❌ Uso: /clan admin dados editar guildas <tag> [level|xp|balance|mudar-lider] <valor>"));
                    return;
                }
                String field = args[4].toLowerCase();
                String valueStr = args[5];

                if (field.equals("mudar-lider")) {
                    handleMudarLiderField(admin, guild, valueStr);
                    return;
                }

                try {
                    if (field.equals("level")) {
                        int level = Integer.parseInt(valueStr);
                        guild.setLevel(level);
                        MineSkyGuildas.l.info("[Clãs] [ADMIN] " + admin.getName() + " definou o level do clã " + guild.getName() + " para " +level);
                        admin.sendMessage(Utils.c("&a✅ Nível do clã &f" + guild.getName() + " &adefinido para &f" + level));
                    } else if (field.equals("xp")) {
                        double xp = Double.parseDouble(valueStr);
                        guild.setXP(xp);
                        MineSkyGuildas.l.info("[Clãs] [ADMIN] " + admin.getName() + " definou o XP do clã " + guild.getName() + " para " + xp);
                        admin.sendMessage(Utils.c("&a✅ XP do clã &f" + guild.getName() + " &adefinido para &f" + xp));
                    } else if (field.equals("balance")) {
                        int balance = Integer.parseInt(valueStr);
                        guild.setBalance(balance);
                        MineSkyGuildas.l.info("[Clãs] [ADMIN] " + admin.getName() + " definou o saldo do clã " + guild.getName() + " para $" +balance);
                        admin.sendMessage(Utils.c("&a✅ Saldo do clã &f" + guild.getName() + " &adefinido para &f" + balance));
                    } else {
                        admin.sendMessage(Utils.c("&c❌ Campo inválido! Opções válidas: 'level', 'xp', 'balance' ou 'mudar-lider'."));
                        return;
                    }
                    GuildHandler.saveGuildas();
                } catch (NumberFormatException e) {
                    admin.sendMessage(Utils.c("&c❌ O valor inserido deve ser um formato de número válido!"));
                }
                break;

            default:
                admin.sendMessage(Utils.c("&c❌ Ação inválida! Opções válidas: 'deletar', 'editar' ou 'resetar'."));
                break;
        }
    }

    private void handleMudarLiderField(Player admin, Guilds guild, String newLeaderNick) {
        OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(newLeaderNick);
        if (!targetPlayer.hasPlayedBefore() && !targetPlayer.isOnline()) {
            admin.sendMessage(Utils.c("&c❌ O jogador '" + newLeaderNick + "' nunca entrou no servidor!"));
            return;
        }

        UUID newLeaderUuid = targetPlayer.getUniqueId();
        UUID oldLeaderUuid = guild.getLeader();

        if (newLeaderUuid.equals(oldLeaderUuid)) {
            admin.sendMessage(Utils.c("&c❌ Este jogador já é o líder supremo do clã!"));
            return;
        }

        Guilds targetCurrentGuild = GuildHandler.getGuildByPlayer(newLeaderUuid);
        if (targetCurrentGuild != null && !targetCurrentGuild.getId().equals(guild.getId())) {
            targetCurrentGuild.removeMember(newLeaderUuid);
            admin.sendMessage(Utils.c("&7ℹ O jogador '" + targetPlayer.getName() + "' foi removido do seu antigo clã (" + targetCurrentGuild.getName() + ") para assumir o novo cargo."));
        }

        if (!guild.getMembers().containsKey(newLeaderUuid)) {
            guild.addMember(newLeaderUuid, GuildRoles.LEADER, 0, System.currentTimeMillis());
        } else {
            guild.getMemberData(newLeaderUuid).setRole(GuildRoles.LEADER);
        }

        MemberData oldLeaderData = guild.getMemberData(oldLeaderUuid);
        if (oldLeaderData != null) {
            oldLeaderData.setRole(GuildRoles.SUB_LEADER);
        }

        guild.setLeader(newLeaderUuid);

        GuildHandler.saveGuildas();

        MineSkyGuildas.l.info("[Clãs] [ADMIN] " + admin.getName() + " transferiu o cargo de líder do clã " + guild.getName() + " para o " + targetPlayer.getName());
        admin.sendMessage(Utils.c("&a✅ Liderança do clã &f" + guild.getName() + " &atransferida com sucesso para &f" + targetPlayer.getName()));

        Player newLeaderOnline = Bukkit.getPlayer(newLeaderUuid);
        if (newLeaderOnline != null && newLeaderOnline.isOnline()) {
            newLeaderOnline.sendMessage(Utils.c("&6⭐ Você foi promovido a líder do clã " + guild.getName() + " por um administrador!"));
        }
    }

    private void handleJogadores(Player admin, String action, String nick, String[] args) {
        OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(nick);
        if (!targetPlayer.hasPlayedBefore() && !targetPlayer.isOnline()) {
            admin.sendMessage(Utils.c("&c❌ O jogador '" + nick + "' nunca entrou no servidor!"));
            return;
        }

        UUID uuid = targetPlayer.getUniqueId();
        Guilds guild = GuildHandler.getGuildByPlayer(uuid);

        switch (action) {
            case "deletar":
                if (guild == null) {
                    admin.sendMessage(Utils.c("&c❌ Este jogador não pertence a nenhum clã no momento!"));
                    return;
                }
                guild.removeMember(uuid);
                GuildHandler.saveGuildas();
                MineSkyGuildas.l.info("[Clãs] [ADMIN] " + admin.getName() + " removeu o " + targetPlayer.getName() + " do clã" + guild.getName());
                admin.sendMessage(Utils.c("&a✅ O jogador &f" + targetPlayer.getName() + " &afoi removido com sucesso do clã &f" + guild.getName()));
                break;

            case "resetar":
                if (guild != null) {
                    MemberData data = guild.getMemberData(uuid);
                    if (data != null) {
                        data.setKills(0);
                    }
                }
                MineSkyGuildas.getInstance().getPlayerData().resetPlayerStats(uuid);
                GuildHandler.saveGuildas();
                MineSkyGuildas.l.info("[Clãs] [ADMIN] " + admin.getName() + " resetou o dados do jogador " + targetPlayer.getName());
                admin.sendMessage(Utils.c("&a✅ Estatísticas de KDR do jogador &f" + targetPlayer.getName() + " &aforam resetadas!"));
                break;

            case "editar":
                if (args.length < 6) {
                    admin.sendMessage(Utils.c("&c❌ Uso: /clan admin dados editar jogadores <nick> [kills|role] <valor>"));
                    return;
                }
                String field = args[4].toLowerCase();
                String valueStr = args[5];

                if (guild == null) {
                    admin.sendMessage(Utils.c("&c❌ Este jogador não faz parte de nenhuma guilda para ser editado!"));
                    return;
                }

                MemberData data = guild.getMemberData(uuid);
                if (data == null) {
                    admin.sendMessage(Utils.c("&c❌ Erro ao localizar os metadados do jogador no clã!"));
                    return;
                }

                if (field.equals("kills")) {
                    try {
                        int kills = Integer.parseInt(valueStr);
                        data.setKills(kills);
                        MineSkyGuildas.getInstance().getPlayerData().setKills(uuid, kills);
                        GuildHandler.saveGuildas();
                        MineSkyGuildas.l.info("[Clãs] [ADMIN] " + admin.getName() + " definou as kills do jogador " + targetPlayer.getName() + " para " + kills);
                        admin.sendMessage(Utils.c("&a✅ As kills do jogador &f" + targetPlayer.getName() + " &ano clã foram alteradas para &f" + kills));
                    } catch (NumberFormatException e) {
                        admin.sendMessage(Utils.c("&c❌ O valor de kills inserido deve ser um número inteiro válido!"));
                    }
                } else if (field.equals("role")) {
                    try {
                        GuildRoles role = GuildRoles.valueOf(valueStr.toUpperCase());
                        data.setRole(role);
                        GuildHandler.saveGuildas();
                        MineSkyGuildas.l.info("[Clãs] [ADMIN] " + admin.getName() + " definou o cargo de " + targetPlayer.getName() + " para " + role.name());
                        admin.sendMessage(Utils.c("&a✅ O cargo do jogador &f" + targetPlayer.getName() + " &ano clã foi alterado para &f" + role.name()));
                    } catch (IllegalArgumentException e) {
                        admin.sendMessage(Utils.c("&c❌ Cargo de clã inválido! Opções válidas: LEADER, SUB_LEADER, CAPTAIN, RECRUITER, LOYAL, MEMBER, RECRUIT."));
                    }
                } else {
                    admin.sendMessage(Utils.c("&c❌ Campo de edição de jogador inválido! Escolha 'kills' ou 'role'."));
                }
                break;

            default:
                admin.sendMessage(Utils.c("&c❌ Ação inválida! Opções válidas: 'deletar', 'editar' ou 'resetar'."));
                break;
        }
    }
}