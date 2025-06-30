package net.minesky.enums;

public enum GuildRoles {
    LEADER,        // Acesso total
    SUB_LEADER,    // Gerencia membros, guerras e banco
    CAPTAIN,      // Recruta, expulsa membros e chat privado dos líderes
    RECRUITER,   // Apenas recruta, para ser promovido a esse cargo o jogador precisa ter sido pelo menos o cargo Membro anteriormente
    LOYAL,         // Membro com 120+ kills
    MEMBER,       // Membro padrão (40+ kills)
    RECRUIT;      // Membro recém-adicionado

    public static GuildRoles getRole(String name) {
        try {
            return GuildRoles.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return RECRUIT;
        }
    }

    public static boolean isLeadership(GuildRoles role) {
        return role != null && (
                role.equals(LEADER) ||
                        role.equals(SUB_LEADER) ||
                        role.equals(CAPTAIN)
        );
    }

    public static String getLabelRole(GuildRoles roles) {
        switch (roles) {
            case LEADER -> {
                return "Líder";
            }
            case SUB_LEADER -> {
                return "Sub-Líder";
            }
            case CAPTAIN -> {
                return "Capitão";
            }
            case RECRUITER -> {
                return "Recrutador";
            }
            case LOYAL -> {
                return "Leal";
            }
            case MEMBER -> {
                return "Membro";
            }
            case RECRUIT -> {
                return "Recruta";
            }
        }
        return roles.toString();
    }
}
