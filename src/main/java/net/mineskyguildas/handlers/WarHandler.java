package net.mineskyguildas.handlers;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.ReplaceOptions;
import net.mineskyguildas.MineSkyGuildas;
import net.mineskyguildas.data.Guilds;
import net.mineskyguildas.handlers.requests.GuildRequestType;
import net.mineskyguildas.hooks.ClaimHook;
import net.mineskyguildas.utils.Utils;
import net.mineskyguildas.war.WarSession;
import net.mineskyguildas.war.WarState;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class WarHandler {
    private final MineSkyGuildas plugin;
    private final Map<UUID, WarSession> activeSessions = new ConcurrentHashMap<>();
    private final ZoneId TIME_ZONE = ZoneId.of("America/Sao_Paulo");

    private final MongoCollection<Document> warsColl;
    private final MongoCollection<Document> rivalriesColl;
    private final MongoCollection<Document> guildsColl;

    private final long MIN_RIVALRY_TIME_MS = 3L * 24 * 60 * 60 * 1000;

    public WarHandler(MineSkyGuildas plugin) {
        this.plugin = plugin;
        this.warsColl = plugin.mm.getGuildWar();
        this.rivalriesColl = plugin.mm.getRivalryTimestamps();
        this.guildsColl = plugin.mm.getGuildas();

        loadWarsFromDatabase();
        startWarScheduler();
    }

    private void loadWarsFromDatabase() {
        for (Document doc : warsColl.find()) {
            try {
                UUID warId = UUID.fromString(doc.getString("_id"));
                String g1Id = doc.getString("guild1_id");
                String g2Id = doc.getString("guild2_id");

                Guilds g1 = GuildHandler.getGuildByID(g1Id);
                Guilds g2 = GuildHandler.getGuildByID(g2Id);

                if (g1 == null || g2 == null) {
                    warsColl.deleteOne(new Document("_id", warId.toString()));
                    continue;
                }

                WarState state = WarState.valueOf(doc.getString("state"));
                LocalDateTime inviteTime = LocalDateTime.parse(doc.getString("invite_time"));

                LocalDateTime decTime = doc.containsKey("declaration_time") && doc.getString("declaration_time") != null
                        ? LocalDateTime.parse(doc.getString("declaration_time")) : null;
                LocalDateTime startTime = doc.containsKey("start_time") && doc.getString("start_time") != null
                        ? LocalDateTime.parse(doc.getString("start_time")) : null;

                int l1Offline = doc.containsKey("leader1_offline_left") ? doc.getInteger("leader1_offline_left") : 600;
                int l2Offline = doc.containsKey("leader2_offline_left") ? doc.getInteger("leader2_offline_left") : 600;

                WarSession session = new WarSession(warId, g1, g2, state, inviteTime, decTime, startTime, l1Offline, l2Offline);

                if (doc.containsKey("guild1_supporters")) {
                    session.getGuild1Supporters().addAll(doc.getList("guild1_supporters", String.class));
                }
                if (doc.containsKey("guild2_supporters")) {
                    session.getGuild2Supporters().addAll(doc.getList("guild2_supporters", String.class));
                }

                activeSessions.put(warId, session);
            } catch (Exception e) {
                plugin.getLogger().warning("Erro ao carregar guerra do banco: " + e.getMessage());
            }
        }
    }

    public void saveWarToDatabase(WarSession session) {
        Bukkit.getAsyncScheduler().runNow(plugin, task -> {
            Document doc = new Document("_id", session.getWarId().toString())
                    .append("guild1_id", session.getGuild1().getId())
                    .append("guild2_id", session.getGuild2().getId())
                    .append("state", session.getState().name())
                    .append("invite_time", session.getInviteTime().toString())
                    .append("declaration_time", session.getDeclarationTime() != null ? session.getDeclarationTime().toString() : null)
                    .append("start_time", session.getStartTime() != null ? session.getStartTime().toString() : null)
                    .append("leader1_offline_left", session.getLeader1OfflineTimeLeft())
                    .append("leader2_offline_left", session.getLeader2OfflineTimeLeft())
                    .append("guild1_supporters", new ArrayList<>(session.getGuild1Supporters()))
                    .append("guild2_supporters", new ArrayList<>(session.getGuild2Supporters()));

            warsColl.replaceOne(new Document("_id", session.getWarId().toString()), doc, new ReplaceOptions().upsert(true));
        });
    }

    public void removeWarFromDatabase(UUID warId) {
        Bukkit.getAsyncScheduler().runNow(plugin, task -> {
            warsColl.deleteOne(new Document("_id", warId.toString()));
        });
    }

    public void recordRivalry(String guild1Id, String guild2Id) {
        long now = System.currentTimeMillis();
        String rId = getRivalryId(guild1Id, guild2Id);
        Bukkit.getAsyncScheduler().runNow(plugin, task -> {
            Document doc = new Document("_id", rId)
                    .append("guild1", guild1Id)
                    .append("guild2", guild2Id)
                    .append("timestamp", now);
            rivalriesColl.replaceOne(new Document("_id", rId), doc, new ReplaceOptions().upsert(true));
        });
    }

    public void removeRivalryRecord(String guild1Id, String guild2Id) {
        String rId = getRivalryId(guild1Id, guild2Id);
        Bukkit.getAsyncScheduler().runNow(plugin, task -> {
            rivalriesColl.deleteOne(new Document("_id", rId));
        });
    }

    public long getRivalryDuration(String guild1Id, String guild2Id) {
        Document doc = rivalriesColl.find(new Document("_id", getRivalryId(guild1Id, guild2Id))).first();
        if (doc == null) return 0L;
        return System.currentTimeMillis() - doc.getLong("timestamp");
    }

    private String getRivalryId(String g1, String g2) {
        List<String> list = Arrays.asList(g1, g2);
        Collections.sort(list);
        return list.get(0) + ":" + list.get(1);
    }

    public void removeAllRivalriesForGuild(String guildId) {
        Bukkit.getAsyncScheduler().runNow(plugin, task -> {
            rivalriesColl.deleteMany(new org.bson.Document("$or", java.util.Arrays.asList(
                    new org.bson.Document("guild1", guildId),
                    new org.bson.Document("guild2", guildId)
            )));
        });
    }

    public boolean inviteWar(Player sender, Guilds inviter, Guilds target) {
        if (!inviter.isRival(target)) {
            sender.sendMessage(Utils.c("&c❌ Você só pode declarar guerra para clãs considerados rivais!"));
            return false;
        }

        long duration = getRivalryDuration(inviter.getId(), target.getId());
        if (duration < MIN_RIVALRY_TIME_MS) {
            long remaining = MIN_RIVALRY_TIME_MS - duration;
            long days = remaining / (24 * 60 * 60 * 1000);
            long hours = (remaining % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000);
            sender.sendMessage(Utils.c("&c❌ Vocês precisam ser rivais por mais tempo! Faltam " + days + " dias e " + hours + " horas."));
            return false;
        }

        if (isGuildInWarOrPending(inviter.getId()) || isGuildInWarOrPending(target.getId())) {
            sender.sendMessage(Utils.c("&c❌ Uma das guildas já possui convite pendente ou está em guerra ativa!"));
            return false;
        }

        WarSession session = new WarSession(inviter, target);
        activeSessions.put(session.getWarId(), session);
        saveWarToDatabase(session);

        GuildHandler.broadcastGuildMessage(inviter,  "&4⚔ &c" + sender.getName() + " enviou um pedido de &lGUERRA &cpara o clã &f" + target.getName());

        plugin.getRequestManager().getHandler(GuildRequestType.WAR).sendRequest(inviter, target, sender);
        MineSkyGuildas.l.info("[Clãs] " + sender.getName() + " membro do clã " + inviter.getName() + " declarou guerra para o clã " + target.getName());
        return true;
    }

    public void acceptWar(WarSession session) {
        session.setState(WarState.SCHEDULED);

        LocalDateTime tomorrow = LocalDateTime.now(TIME_ZONE).plusDays(1);
        session.setDeclarationTime(tomorrow.withHour(13).withMinute(0).withSecond(0));
        session.setStartTime(tomorrow.withHour(15).withMinute(30).withSecond(0));

        saveWarToDatabase(session);

        String msg =
                "&r\n" +
                        "&4&l══════════ ⚔ GUERRA DE CLÃS ⚔ ══════════\n" +
                        "&aO desafio foi &2ACEITO&a!\n" +
                        "&f" + session.getGuild1().getName() + " &8⚔ &f" + session.getGuild2().getName() + "\n" +
                        "&e🕒 A guerra acontecerá &6amanhã às &e15:30&6!\n" +
                        "&cPreparem seus equipamentos e organizem seu clã!\n" +
                        "&4&l════════════════════════════════════════";
        GuildHandler.broadcastGuildMessage(session.getGuild1(), msg);
        GuildHandler.broadcastGuildMessage(session.getGuild2(), msg);
        MineSkyGuildas.l.info("[Clãs] Guerra entre " + session.getGuild1().getName() + " e " + session.getGuild2().getName() + " foi aceita ");

        inviteAlliesToWar(session);
    }

    private void inviteAlliesToWar(WarSession session) {
        for (String allyId : session.getGuild1().getAllies()) {
            Guilds ally = GuildHandler.getGuildByID(allyId);
            if (ally != null && !ally.isAlly(session.getGuild2())) {
                Player leader = Bukkit.getPlayer(session.getGuild1().getLeader());
                plugin.getRequestManager().getHandler(GuildRequestType.WAR).sendRequest(session.getGuild1(), ally, leader);
            }
        }
        for (String allyId : session.getGuild2().getAllies()) {
            Guilds ally = GuildHandler.getGuildByID(allyId);
            if (ally != null && !ally.isAlly(session.getGuild1())) {
                Player leader = Bukkit.getPlayer(session.getGuild2().getLeader());
                plugin.getRequestManager().getHandler(GuildRequestType.WAR).sendRequest(session.getGuild2(), ally, leader);
            }
        }
    }

    public void rejectWar(WarSession session, Player refuser) {
        GuildHandler.broadcastGuildMessage(session.getGuild1(), "&c⚔ O clã desafiado recusou seu convite de guerra.");
        GuildHandler.broadcastGuildMessage(session.getGuild2(), "&c⚔ O convite de guerra foi recusado por " + refuser.getName());

        activeSessions.remove(session.getWarId());
        removeWarFromDatabase(session.getWarId());
        MineSkyGuildas.l.info("[Clãs] " + refuser.getName() + " recusou o pedido de guerra do clã " + session.getGuild2().getName());
    }

    private void startWarScheduler() {
        Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, scheduledTask -> {
            LocalDateTime now = LocalDateTime.now(TIME_ZONE);

            for (WarSession session : new ArrayList<>(activeSessions.values())) {
                if (session.getState() == WarState.PENDING) {
                    if (session.getInviteTime().plusDays(3).isBefore(now)) {
                        acceptWar(session);
                    }
                    continue;
                }

                if (session.getState() == WarState.DECLARED) {
                    long minutesRemaining = java.time.Duration.between(now, session.getStartTime()).toMinutes();

                    if (minutesRemaining != session.getLastCountdownBroadcastMinute()) {
                        if (minutesRemaining == 60 || minutesRemaining == 30 || minutesRemaining == 15
                                || minutesRemaining == 10 || minutesRemaining == 5 || minutesRemaining == 1) {
                            session.setLastCountdownBroadcastMinute(minutesRemaining);
                            Bukkit.broadcastMessage(" ");
                            Bukkit.broadcastMessage(Utils.c("&4&l⚔ GUERRA!"));
                            Bukkit.broadcastMessage(Utils.c("&cA batalha começará em &e" + minutesRemaining + " minuto" + (minutesRemaining == 1 ? "" : "s") + "&c!"));
                            Bukkit.broadcastMessage(Utils.c("&f" + session.getGuild1().getName() + " &8⚔ &f" + session.getGuild2().getName()));
                            Bukkit.broadcastMessage(Utils.c("&eReúnam seus membros e preparem-se para o combate!"));
                            Bukkit.broadcastMessage(" ");

                            Bukkit.getOnlinePlayers().forEach(p -> {
                                p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_AMBIENT, 1.0f, 1.0f);
                            });
                        }
                    }
                }

                if (session.getState() == WarState.SCHEDULED) {
                    if (now.isAfter(session.getDeclarationTime())) {
                        session.setState(WarState.DECLARED);
                        saveWarToDatabase(session);

                        Bukkit.broadcastMessage(" ");
                        Bukkit.broadcastMessage(Utils.c("&4&l⚔ A GUERRA FOI DECLARADA! ⚔"));
                        Bukkit.broadcastMessage(Utils.c("&cOs clãs &f" + session.getGuild1().getName() + " &8⚔ &f" + session.getGuild2().getName() + " &centraram em estado de guerra!"));
                        Bukkit.broadcastMessage(Utils.c("&e📅 O confronto acontecerá às &615:30&e."));
                        Bukkit.broadcastMessage(Utils.c("&cA vitória pertence ao clã mais forte!"));
                        Bukkit.broadcastMessage(" ");

                        Bukkit.getOnlinePlayers().forEach(p -> {
                            p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_AMBIENT, 1.0f, 1.0f);
                        });
                    }
                    continue;
                }

                if (session.getState() == WarState.DECLARED) {
                    if (now.isAfter(session.getStartTime())) {
                        session.setState(WarState.ACTIVE);
                        saveWarToDatabase(session);
                        Bukkit.broadcastMessage(" ");
                        Bukkit.broadcastMessage(Utils.c("&4&l⚔ A GUERRA COMEÇOU! ⚔"));
                        Bukkit.broadcastMessage(Utils.c("&f" + session.getGuild1().getName() + " &8⚔ &f" + session.getGuild2().getName()));
                        Bukkit.broadcastMessage(Utils.c("&cOs exércitos já estão em combate!"));
                        Bukkit.broadcastMessage(Utils.c("&e✈ O voo dos membros de ambos os clãs foi desativado."));
                        Bukkit.broadcastMessage(Utils.c("&4Não há mais volta. Lutem pela vitória!"));
                        Bukkit.broadcastMessage(" ");

                        Bukkit.getOnlinePlayers().forEach(p -> {
                            p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_AMBIENT, 1.0f, 1.0f);
                        });
                        disableFlyForWarParticipants(session);
                    }
                    continue;
                }

                if (session.getState() == WarState.ACTIVE) {
                    Player leader1 = Bukkit.getPlayer(session.getGuild1().getLeader());
                    Player leader2 = Bukkit.getPlayer(session.getGuild2().getLeader());

                    if (leader1 == null || !leader1.isOnline()) {
                        session.decrementLeader1OfflineTime();
                        if (session.getLeader1OfflineTimeLeft() % 60 == 0 || session.getLeader1OfflineTimeLeft() <= 10) {
                            GuildHandler.broadcastGuildMessageNoNotice(session.getGuild1(), "&c⚠ Alerta: Seu líder está offline! Restam "
                                    + session.getLeader1OfflineTimeLeft() + " segundos de timeout antes da derrota imediata!");
                        }
                        if (session.getLeader1OfflineTimeLeft() <= 0) {
                            endWar(session, session.getGuild2(), session.getGuild1(), "O líder do clã rival falhou em manter-se online durante a guerra.");
                            return;
                        }
                    } else {
                        if (session.getLeader1OfflineTimeLeft() < 600) {
                            session.setLeader1OfflineTimeLeft(600);
                            GuildHandler.broadcastGuildMessageNoNotice(session.getGuild1(), "&a✔ Seu líder reconectou! O tempo de timeout foi restaurado.");
                        }
                    }

                    if (leader2 == null || !leader2.isOnline()) {
                        session.decrementLeader2OfflineTime();
                        if (session.getLeader2OfflineTimeLeft() % 60 == 0 || session.getLeader2OfflineTimeLeft() <= 10) {
                            GuildHandler.broadcastGuildMessageNoNotice(session.getGuild2(), "&c⚠ Alerta: Seu líder está offline! Restam "
                                    + session.getLeader2OfflineTimeLeft() + " segundos de timeout antes da derrota imediata!");
                        }
                        if (session.getLeader2OfflineTimeLeft() <= 0) {
                            endWar(session, session.getGuild1(), session.getGuild2(), "O líder do clã rival falhou em manter-se online durante a guerra.");
                            return;
                        }
                    } else {
                        if (session.getLeader2OfflineTimeLeft() < 600) {
                            session.setLeader2OfflineTimeLeft(600);
                            GuildHandler.broadcastGuildMessageNoNotice(session.getGuild2(), "&a✔ Seu líder reconectou! O tempo de timeout foi restaurado.");
                        }
                    }
                }
            }
        }, 20L, 20L);
    }

    private void disableFlyForWarParticipants(WarSession session) {
        Set<String> involvedGuilds = new HashSet<>();
        involvedGuilds.add(session.getGuild1().getId());
        involvedGuilds.add(session.getGuild2().getId());
        involvedGuilds.addAll(session.getGuild1Supporters());
        involvedGuilds.addAll(session.getGuild2Supporters());

        for (String gId : involvedGuilds) {
            Guilds g = GuildHandler.getGuildByID(gId);
            if (g != null) {
                for (UUID uuid : g.getMembers().keySet()) {
                    Player p = Bukkit.getPlayer(uuid);
                    if (p != null && p.isOnline()) {
                        p.setAllowFlight(false);
                        p.setFlying(false);
                        p.sendMessage(Utils.c("&cSeu voo foi desativado automaticamente devido à guerra!"));
                    }
                }
            }
        }
    }

    public void endWar(WarSession session, Guilds winner, Guilds loser, String reason) {
        session.setState(WarState.FINISHED);
        activeSessions.remove(session.getWarId());
        removeWarFromDatabase(session.getWarId());

        Bukkit.broadcastMessage(" ");
        Bukkit.broadcastMessage(Utils.c("&4&l⚔ A GUERRA CHEGOU AO FIM! ⚔"));
        Bukkit.broadcastMessage(Utils.c("&a🏆 &lVENCEDOR: &f" + winner.getName()));
        Bukkit.broadcastMessage(Utils.c("&c☠ &lDERROTADO: &f" + loser.getName()));
        Bukkit.broadcastMessage(Utils.c("&aO clã vencedor conquistou todo o banco inimigo e recebeu muito XP!"));
        Bukkit.broadcastMessage(Utils.c("&cO clã derrotado foi completamente erradicado do servidor."));
        Bukkit.broadcastMessage(Utils.c("&e📜 Motivo: &f" + reason));
        Bukkit.broadcastMessage(" ");

        int loserBalance = loser.getBalance();
        winner.deposit(loserBalance);
        winner.addXP(10000.0);

        recordDefeatedClanCooldown(loser.getName(), loser.getTag());

        GuildHandler.saveGuildas();
        GuildHandler.deleteGuild(loser.getId());
    }

    public boolean isClaimExposed(Location loc, WarSession session, Guilds playerGuild, ClaimHook claimHook) {
        Guilds claimGuild = claimHook.getClaimOwnerGuildAt(loc);
        if (claimGuild == null) return false;

        String claimId = claimGuild.getId();
        String pgId = playerGuild.getId();

        if (claimId.equals(pgId)) return false;

        Guilds g1 = session.getGuild1();
        Guilds g2 = session.getGuild2();

        int playerSide = 0;
        if (pgId.equals(g1.getId()) || session.getGuild1Supporters().contains(pgId)) {
            playerSide = 1;
        } else if (pgId.equals(g2.getId()) || session.getGuild2Supporters().contains(pgId)) {
            playerSide = 2;
        }

        if (playerSide == 0) return false;

        boolean alliedToG1 = claimGuild.isAlly(g1);
        boolean alliedToG2 = claimGuild.isAlly(g2);
        if (alliedToG1 && alliedToG2) {
            return true;
        }

        if (playerSide == 1) {
            return claimId.equals(g2.getId()) || session.getGuild2Supporters().contains(claimId);
        } else {
            return claimId.equals(g1.getId()) || session.getGuild1Supporters().contains(claimId);
        }
    }

    public void recordDefeatedClanCooldown(String name, String tag) {
        long now = System.currentTimeMillis();
        String cleanName = name.toLowerCase();
        String cleanTag = net.mineskyguildas.utils.Utils.getTag(tag).toLowerCase();

        org.bukkit.Bukkit.getAsyncScheduler().runNow(plugin, task -> {
            org.bson.Document nameDoc = new org.bson.Document("_id", "cooldown_name:" + cleanName)
                    .append("type", "NAME")
                    .append("value", name)
                    .append("timestamp", now);
            guildsColl.replaceOne(new org.bson.Document("_id", nameDoc.getString("_id")), nameDoc, new com.mongodb.client.model.ReplaceOptions().upsert(true));

            org.bson.Document tagDoc = new org.bson.Document("_id", "cooldown_tag:" + cleanTag)
                    .append("type", "TAG")
                    .append("value", net.mineskyguildas.utils.Utils.getTag(tag))
                    .append("timestamp", now);
            guildsColl.replaceOne(new org.bson.Document("_id", tagDoc.getString("_id")), tagDoc, new com.mongodb.client.model.ReplaceOptions().upsert(true));
        });
    }

    public boolean isNameUnderCooldown(String name) {
        String cleanName = name.toLowerCase();
        org.bson.Document doc = guildsColl.find(new org.bson.Document("_id", "cooldown_name:" + cleanName)).first();
        if (doc == null) {
            return false;
        }

        long timestamp = doc.containsKey("timestamp") ? doc.getLong("timestamp") : 0L;
        long elapsed = System.currentTimeMillis() - timestamp;
        long oneDayMs = 24L * 60 * 60 * 1000;

        if (elapsed < oneDayMs) {
            return true;
        }

        org.bukkit.Bukkit.getAsyncScheduler().runNow(plugin, task -> guildsColl.deleteOne(new org.bson.Document("_id", "cooldown_name:" + cleanName)));
        return false;
    }

    public boolean isTagUnderCooldown(String tag) {
        String cleanTag = net.mineskyguildas.utils.Utils.getTag(tag).toLowerCase();
        org.bson.Document doc = guildsColl.find(new org.bson.Document("_id", "cooldown_tag:" + cleanTag)).first();
        if (doc == null) {
            return false;
        }

        long timestamp = doc.containsKey("timestamp") ? doc.getLong("timestamp") : 0L;
        long elapsed = System.currentTimeMillis() - timestamp;
        long oneDayMs = 24L * 60 * 60 * 1000;

        if (elapsed < oneDayMs) {
            return true;
        }

        org.bukkit.Bukkit.getAsyncScheduler().runNow(plugin, task -> guildsColl.deleteOne(new org.bson.Document("_id", "cooldown_tag:" + cleanTag)));
        return false;
    }

    public long getCooldownRemainingTime(String value, String type) {
        String cleanVal = type.equals("NAME") ? value.toLowerCase() : net.mineskyguildas.utils.Utils.getTag(value).toLowerCase();
        String id = (type.equals("NAME") ? "cooldown_name:" : "cooldown_tag:") + cleanVal;

        org.bson.Document doc = guildsColl.find(new org.bson.Document("_id", id)).first();
        if (doc == null) {
            return 0;
        }

        long timestamp = doc.containsKey("timestamp") ? doc.getLong("timestamp") : 0L;
        long elapsed = System.currentTimeMillis() - timestamp;
        long remaining = (24L * 60 * 60 * 1000) - elapsed;
        return Math.max(0, remaining);
    }

    public boolean isGuildInWarOrPending(String guildId) {
        return activeSessions.values().stream()
                .anyMatch(s -> s.getGuild1().getId().equals(guildId) || s.getGuild2().getId().equals(guildId));
    }

    public boolean isGuildInActiveWar(String guildId) {
        return activeSessions.values().stream()
                .anyMatch(s -> (s.getGuild1().getId().equals(guildId)
                        || s.getGuild2().getId().equals(guildId)
                        || s.getGuild1Supporters().contains(guildId)
                        || s.getGuild2Supporters().contains(guildId))
                        && s.getState() == WarState.ACTIVE);
    }

    public Optional<WarSession> getActiveWarByGuild(String guildId) {
        return activeSessions.values().stream()
                .filter(s -> (s.getGuild1().getId().equals(guildId)
                        || s.getGuild2().getId().equals(guildId)
                        || s.getGuild1Supporters().contains(guildId)
                        || s.getGuild2Supporters().contains(guildId))
                        && s.getState() == WarState.ACTIVE)
                .findFirst();
    }

    public Optional<WarSession> getScheduledWarInvolving(String guildId) {
        return activeSessions.values().stream()
                .filter(s -> (s.getGuild1().getId().equals(guildId) || s.getGuild2().getId().equals(guildId))
                        && s.getState() == WarState.SCHEDULED)
                .findFirst();
    }

    public Optional<WarSession> getPendingInvite(Guilds target, Guilds inviter) {
        return activeSessions.values().stream()
                .filter(s -> s.getState() == WarState.PENDING
                        && s.getGuild1().getId().equals(inviter.getId())
                        && s.getGuild2().getId().equals(target.getId()))
                .findFirst();
    }

    public Map<UUID, WarSession> getActiveSessions() {
        return activeSessions;
    }
}