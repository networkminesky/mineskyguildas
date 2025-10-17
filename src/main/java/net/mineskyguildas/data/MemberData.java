package net.mineskyguildas.data;

import net.mineskyguildas.enums.GuildRoles;

public class MemberData {

    private final Guilds guild;
    private GuildRoles role;
    private int kills;

    public MemberData(Guilds guild, GuildRoles role, int kills) {
        this.guild = guild;
        this.role = role;
        this.kills = kills;
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

    public void addKill() {
       kills++;
    }

    public void setRole(GuildRoles role) {
        this.role = role;
    }
}

