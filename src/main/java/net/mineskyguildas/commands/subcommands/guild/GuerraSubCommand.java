package net.mineskyguildas.commands.subcommands.guild;

import net.mineskyguildas.MineSkyGuildas;
import net.mineskyguildas.commands.subcommands.SubCommand;
import net.mineskyguildas.data.Guilds;
import net.mineskyguildas.enums.GuildRoles;
import net.mineskyguildas.handlers.GuildHandler;
import net.mineskyguildas.handlers.WarHandler;
import net.mineskyguildas.handlers.requests.GuildRequestType;
import net.mineskyguildas.utils.Utils;
import net.mineskyguildas.war.WarSession;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class GuerraSubCommand extends SubCommand {

    @Override
    public String getName() {
        return "guerra";
    }

    @Override
    public String getDescription() {
        return "Desafia clãs rivais, gerencia convites ou apoia seus aliados.";
    }

    @Override
    public String getUsage() {
        return "/clan guerra <clã> ou /clan guerra [aceitar|recusar] <clã>";
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("war");
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

        if (args.length < 2) {
            player.sendMessage(Utils.c("&c❌ Use: /clan guerra <tag/nome> ou /clan guerra [aceitar/recusar] <tag/nome>"));
            return;
        }

        WarHandler warHandler = MineSkyGuildas.getInstance().getWarHandler();
        String action = args[1].toLowerCase();

        if (action.equals("aceitar") || action.equals("recusar")) {
            if (!GuildRoles.isLeadership(myGuild.getRole(player.getUniqueId()))) {
                player.sendMessage(Utils.c("&c❌ Você precisa ser Líder ou Sub-Líder para gerenciar convites de guerra!"));
                return;
            }

            if (args.length < 3) {
                player.sendMessage(Utils.c("&c❌ Use: /clan guerra " + action + " <tag/nome do clã desafiante>"));
                return;
            }

            String targetTagOrName = args[2];
            Guilds challenger = GuildHandler.getGuildByTag(targetTagOrName);
            if (challenger == null) {
                challenger = GuildHandler.getGuilds().values().stream()
                        .filter(g -> g.getName().equalsIgnoreCase(targetTagOrName))
                        .findFirst().orElse(null);
            }

            if (challenger == null) {
                player.sendMessage(Utils.c("&c❌ Clã desafiante não encontrado!"));
                return;
            }

            Optional<WarSession> activeSession = warHandler.getScheduledWarInvolving(challenger.getId());
            if (activeSession.isPresent()) {
                WarSession session = activeSession.get();

                if (session.getGuild1().getId().equals(myGuild.getId()) || session.getGuild2().getId().equals(myGuild.getId())) {
                    player.sendMessage(Utils.c("&c❌ Esta guerra já está agendada!"));
                    return;
                }

                boolean isAllyOf1 = myGuild.isAlly(session.getGuild1());
                boolean isAllyOf2 = myGuild.isAlly(session.getGuild2());

                if (!isAllyOf1 && !isAllyOf2) {
                    player.sendMessage(Utils.c("&c❌ Seu clã não possui aliança com nenhum dos lados para poder apoiá-los!"));
                    return;
                }

                if (isAllyOf1 && isAllyOf2) {
                    player.sendMessage(Utils.c("&c❌ Seu clã é aliado de ambos os lados e deve permanecer Neutro nesta guerra!"));
                    return;
                }

                if (action.equals("recusar")) {
                    if (MineSkyGuildas.getInstance().getRequestManager().getHandler(GuildRequestType.WAR).hasRequest(myGuild)) {
                        player.sendMessage(Utils.c("&c❌ Seu clã já recusou!"));
                        return;
                    }
                }

                if (action.equals("aceitar")) {
                    if (session.getGuild1Supporters().contains(myGuild.getId()) || session.getGuild2Supporters().contains(myGuild.getId())) {
                        player.sendMessage(Utils.c("&c❌ Seu clã já está participando desta guerra como apoiador!"));
                        return;
                    }

                    MineSkyGuildas.getInstance().getRequestManager().getHandler(GuildRequestType.WAR).removeRequest(myGuild);

                    if (session.getGuild1().getId().equals(challenger.getId())) {
                        session.getGuild1Supporters().add(myGuild.getId());
                        warHandler.saveWarToDatabase(session);
                        GuildHandler.broadcastGuildMessage(session.getGuild1(), "&a🤝 " + myGuild.getName() + " aceitou apoiar seu clã na guerra!");
                        GuildHandler.broadcastGuildMessage(session.getGuild2(), "&4⚔ &c" + myGuild.getName() + " Começou a apoiar o clã &f" + session.getGuild1().getName() + "!");
                        GuildHandler.broadcastGuildMessage(myGuild, "&a🤝 Seu clã entrou na guerra em apoio a " + session.getGuild1().getName() + "!");
                        MineSkyGuildas.l.info("[Clãs] " + player.getName() + " membro do clã " + myGuild.getName() + " aceitou apoiar o clã " + session.getGuild1().getName() + " na guerra.");
                    } else if (session.getGuild2().getId().equals(challenger.getId())) {
                        session.getGuild2Supporters().add(myGuild.getId());
                        warHandler.saveWarToDatabase(session);
                        GuildHandler.broadcastGuildMessage(session.getGuild2(), "&a🤝 " + myGuild.getName() + " aceitou apoiar seu clã na guerra!");
                        GuildHandler.broadcastGuildMessage(session.getGuild1(), "&4⚔ &c" + myGuild.getName() + " Começou a apoiar o clã &f" + session.getGuild2().getName() + "!");
                        GuildHandler.broadcastGuildMessage(myGuild, "&a🤝 Seu clã entrou na guerra em apoio a " + session.getGuild2().getName() + "!");
                        MineSkyGuildas.l.info("[Clãs] " + player.getName() + " membro do clã " + myGuild.getName() + " aceitou apoiar o clã " + session.getGuild2().getName() + " na guerra.");
                    }
                } else {
                    MineSkyGuildas.getInstance().getRequestManager().getHandler(GuildRequestType.WAR).removeRequest(myGuild);
                    player.sendMessage(Utils.c("&cVocê recusou apoiar o clã na guerra."));
                    MineSkyGuildas.l.info("[Clãs] " + player.getName() + " membro do clã " + myGuild.getName() + " recusou participar da guerra.");
                }
                return;
            }

            Optional<WarSession> sessionOpt = warHandler.getPendingInvite(myGuild, challenger);
            if (sessionOpt.isEmpty()) {
                player.sendMessage(Utils.c("&c❌ Não há convites de guerra pendentes deste clã para o seu!"));
                return;
            }

            if (action.equals("aceitar")) {
                MineSkyGuildas.getInstance().getRequestManager().getHandler(GuildRequestType.WAR).removeRequest(myGuild);
                warHandler.acceptWar(sessionOpt.get());
            } else {
                MineSkyGuildas.getInstance().getRequestManager().getHandler(GuildRequestType.WAR).removeRequest(myGuild);
                warHandler.rejectWar(sessionOpt.get(), player);
            }
            return;
        }

        if (!myGuild.getLeader().equals(player.getUniqueId())) {
            player.sendMessage(Utils.c("&c❌ Apenas o Líder supremo do clã pode formalmente declarar guerra!"));
            return;
        }

        String targetTagOrName = args[1];
        Guilds target = GuildHandler.getGuildByTag(targetTagOrName);
        if (target == null) {
            target = GuildHandler.getGuilds().values().stream()
                    .filter(g -> g.getName().equalsIgnoreCase(targetTagOrName))
                    .findFirst().orElse(null);
        }

        if (target == null) {
            player.sendMessage(Utils.c("&c❌ Clã rival não encontrado!"));
            return;
        }

        if (target.getId().equals(myGuild.getId())) {
            player.sendMessage(Utils.c("&c❌ Você não pode declarar guerra a si mesmo!"));
            return;
        }

        Guilds finalTarget = target;
        Utils.confirmAction(player, "declarar guerra contra o clã " + finalTarget.getName() + "\n" +
                "\n" +
                "&a&lPROS (EM CASO DE VITÓRIA):\n" +
                " &a✔ &7Seu clã drenará e receberá todo o saldo do banco do clã inimigo!\n" +
                " &a✔ &7O seu clã receberá uma recompensa militar massiva de &e10.000 XP&7!\n" +
                "\n" +
                "&c&lCONTRAS (EM CASO DE DERROTA):\n" +
                " &e⚠ &7Durante o período da guerra, todos os seus territórios ficarão expostos a invasões!\n" +
                " &e⚠ &7Em caso de derrota, seu clã será &ccompletamente erradicado e deletado&7 do servidor!\n" +
                "\n", () -> {
            warHandler.inviteWar(player, myGuild, finalTarget);
        });
    }
}