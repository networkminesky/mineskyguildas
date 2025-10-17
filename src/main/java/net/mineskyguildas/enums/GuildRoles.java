package net.mineskyguildas.enums;

import java.util.ArrayList;
import java.util.List;

public enum GuildRoles {
    LEADER,        // Acesso total
    SUB_LEADER,    // Gerencia membros, guerras e banco
    CAPTAIN,       // Recruta, expulsa membros e chat privado dos líderes
    RECRUITER,     // Apenas recruta, para ser promovido a esse cargo o jogador precisa ter sido pelo menos o cargo Membro anteriormente
    LOYAL,         // Membro com 120+ kills
    MEMBER,        // Membro padrão (40+ kills)
    RECRUIT;       // Membro recém-adicionado

    /**
     * Retorna uma lista de cargos que estão acima do cargo atual.
     */
    public static List<GuildRoles> getRolesAbove(GuildRoles currentRole) {
        List<GuildRoles> rolesAbove = new ArrayList<>();
        for (GuildRoles role : values()) {
            if (role.ordinal() < currentRole.ordinal()) rolesAbove.add(role);
        }
        return rolesAbove;
    }

    /**
     * Retorna uma lista de cargos que o jogador pode promover.
     */
    public static List<GuildRoles> getPromotableRoles(GuildRoles playerRole) {
        List<GuildRoles> promotable = new ArrayList<>();
        for (GuildRoles role : values()) {
            if (role.ordinal() > playerRole.ordinal()) promotable.add(role);
        }
        return promotable;
    }

    /**
     * Verifica se o executor pode promover o alvo para o cargo desejado
     */
    public static boolean canPromoteTo(GuildRoles executorRole, GuildRoles targetRole, GuildRoles desiredRole) {
        // O cargo desejado deve ser superior ao cargo atual do alvo
        if (desiredRole.ordinal() >= targetRole.ordinal()) return false;
        // O cargo desejado não pode ser igual ou superior ao cargo do executor
        return desiredRole.ordinal() > executorRole.ordinal();
    }

    public static boolean canPermission(GuildRoles executorRole, GuildRoles targetRole) {
        // O executor só pode atuar em cargos **inferiores ao seu**
        return executorRole.ordinal() < targetRole.ordinal();
    }

    /**
     * Retorna o cargo pelo nome.
     */
    public static GuildRoles getRole(String name) {
        if (name == null) return RECRUIT;

        name = name.toLowerCase().replace("-", "").replace("_", "").replace("é", "e").replace("í", "i").trim();

        return switch (name) {
            case "leader", "lider" -> LEADER;
            case "subleader", "sub-lider", "sublider" -> SUB_LEADER;
            case "captain", "capitao", "capitão" -> CAPTAIN;
            case "recruiter", "recrutador" -> RECRUITER;
            case "loyal", "leal" -> LOYAL;
            case "member", "membro" -> MEMBER;
            case "recruit", "recruta" -> RECRUIT;
            default -> null;
        };
    }


    /**
     * Verifica se o cargo é de liderança.
     */
    public static boolean isLeadership(GuildRoles role) {
        return role != null && (
                role.equals(LEADER) ||
                        role.equals(SUB_LEADER) ||
                        role.equals(CAPTAIN)
        );
    }

    public static boolean isLeadershipAndRecruiter(GuildRoles role) {
        return role != null && (
                role.equals(LEADER) ||
                        role.equals(SUB_LEADER) ||
                        role.equals(CAPTAIN) ||
                        role.equals(RECRUITER)
        );
    }

    public static boolean isLeaders(GuildRoles role) {
        return role != null && (role.equals(LEADER) ||
                role.equals(SUB_LEADER));
    }

    /**
     * Retorna o nome "bonito" do cargo.
     */
    public static String getLabelRole(GuildRoles role) {
        switch (role) {
            case LEADER -> { return "Líder"; }
            case SUB_LEADER -> { return "Sub-Líder"; }
            case CAPTAIN -> { return "Capitão"; }
            case RECRUITER -> { return "Recrutador"; }
            case LOYAL -> { return "Leal"; }
            case MEMBER -> { return "Membro"; }
            case RECRUIT -> { return "Recruta"; }
        }
        return role.toString();
    }
}
