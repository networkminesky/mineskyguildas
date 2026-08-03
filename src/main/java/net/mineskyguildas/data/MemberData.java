package net.mineskyguildas.data;

import net.mineskyguildas.enums.GuildRoles;

public class MemberData {

    private final Guilds guild;
    private GuildRoles role;
    private int kills;
    private final long joinedAt;

    public MemberData(Guilds guild, GuildRoles role, int kills, long joinedAt) {
        this.guild = guild;
        this.role = role;
        this.kills = kills;
        this.joinedAt = joinedAt;
    }

    public GuildRoles getRole() {
        return role;
    }

    public int getKills() {
        return kills;
    }

    public Guilds getGuild() {
        return guild;
    }

    public long getJoinedAt() {
        return joinedAt;
    }

    public void addKill() {
       kills++;
    }

    public void setRole(GuildRoles role) {
        this.role = role;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }
}

