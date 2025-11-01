package net.mineskyguildas.utils;

import net.mineskyguildas.MineSkyGuildas;
import net.mineskyguildas.data.Guilds;

import java.text.DecimalFormat;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class GuildStatsManager {

    private static final DecimalFormat df = new DecimalFormat("0.00");

    public static void calculateGuildStats(Guilds guild, GuildStatsCallback callback) {
        if (guild == null || guild.getMembers().isEmpty()) {
            callback.onStatsCalculated(0, 0, 0);
            return;
        }

        AtomicInteger totalKills = new AtomicInteger(0);
        AtomicInteger totalDeaths = new AtomicInteger(0);
        AtomicInteger processed = new AtomicInteger(0);
        int memberCount = guild.getMembers().size();

        for (UUID memberId : guild.getMembers().keySet()) {
            MineSkyGuildas.getInstance().getPlayerData().getKills(memberId, killsObj -> {
                int kills = parseIntSafe(killsObj);

                MineSkyGuildas.getInstance().getPlayerData().getDeaths(memberId, deathsObj -> {
                    int deaths = parseIntSafe(deathsObj);

                    totalKills.addAndGet(kills);
                    totalDeaths.addAndGet(deaths);

                    if (processed.incrementAndGet() >= memberCount) {
                        double kdr = (totalDeaths.get() == 0)
                                ? totalKills.get()
                                : ((double) totalKills.get() / totalDeaths.get());
                        callback.onStatsCalculated(totalKills.get(), totalDeaths.get(), kdr);
                    }
                });
            });
        }
    }

    private static int parseIntSafe(Object value) {
        if (value == null) return 0;
        if (value instanceof Number) return ((Number) value).intValue();
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException ignored) {}
        }
        return 0;
    }

    public interface GuildStatsCallback {
        void onStatsCalculated(int totalKills, int totalDeaths, double kdr);
    }
}
