package net.mineskyguildas.commands.tabcompleter;

import net.mineskyguildas.utils.Utils;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GuildTabCompleter implements TabCompleter {
    private final List<String> subCommands = Arrays.asList("criar", "editar", "acabar", "sair", "expulsar", "promover", "rebaixar", "fogo-amigo", "base", "banco", "convidar", "aceitar", "rejeitar", "estandarte", "aliado", "rival", "anunciar", "mural", "ajuda", "lista", "membros");
    private final List<String> AdminsubCommands = Arrays.asList("admin");

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender s, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (s instanceof Player player) {
            player.playSound(player.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1, 1);
        }

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            if (s.hasPermission("mineskyguildas.admin") || s.hasPermission("mineskyguildas.reload") || s.hasPermission("mineskyguildas.spy")) completions = getMatches(args[0], subCommands, AdminsubCommands);
            else completions = getMatches(args[0], subCommands);
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("membros")) {
                if (s instanceof Player player) {
                    player.playSound(player.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1, 1);
                    completions = getMatches(args[1], Utils.getGuildsTags());
                }
            } else if (args[0].equalsIgnoreCase("banco")) {
                completions = getMatches(args[1], Arrays.asList("dar", "sacar", "saldo"));
            } else if (args[0].equalsIgnoreCase("base")) {
                completions = getMatches(args[1], Arrays.asList("setar", "teleportar"));
            } else if (args[0].equalsIgnoreCase("convidar")) {
                completions = getMatches(args[1], Utils.getOnlinePlayerNames());
            } else if (args[0].equalsIgnoreCase("aliado") || args[0].equalsIgnoreCase("rival")) {
                completions = getMatches(args[1], Arrays.asList("adicionar", "remover"));
            } else if (args[0].equalsIgnoreCase("promover") || args[0].equalsIgnoreCase("rebaixar") || args[0].equalsIgnoreCase("expulsar")) {
                if (s instanceof Player player) {
                    completions = getMatches(args[1], Utils.getGuildMembersNamePerPlayer(player));
                }
            } else if (args[0].equalsIgnoreCase("admin") && s.hasPermission("mineskyguildas.admin")) {
                completions = getMatches(args[1], Arrays.asList("banco-admin", "promover-admin", "rebaixar-admin", "forçar-entrada", "resetar-kdr", "dados", "fogo-amigo-global", "spy", "reload", "reconnect"));
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("aliado") || args[0].equalsIgnoreCase("rival")) {
                if (s instanceof Player player) {
                    player.playSound(player.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1, 1);
                    completions = getMatches(args[2], Utils.getGuildsTags(player));
                }
            } else if (args[0].equalsIgnoreCase("promover")) {
               completions = getMatches(args[2], Arrays.asList("Sub-líder", "Capitão", "Recrutador", "Leal", "Membro"));
            } else if (args[0].equalsIgnoreCase("banco")) {
                completions = getMatches(args[2], Arrays.asList("<quantidade>"));
            } else if (args[0].equalsIgnoreCase("admin")) {
                if (args[1].equalsIgnoreCase("banco-admin")) {
                    completions = getMatches(args[2], Arrays.asList("setar", "tirar", "resetar", "dar", "saldo"));
                } else if (args[1].equalsIgnoreCase("fogo-amigo-global")) {
                    completions = getMatches(args[2], Arrays.asList("ativar", "desativar"));
                } else if (args[1].equalsIgnoreCase("promover-admin") || args[1].equalsIgnoreCase("rebaixar-admin")) {
                    completions = getMatches(args[2], Utils.getOnlinePlayerNames());
                } else if (args[1].equalsIgnoreCase("forçar-entrada")) {
                    completions = getMatches(args[2], Utils.getGuildsTags());
                }
            }
        } else if (args.length == 4) {
            if (args[0].equalsIgnoreCase("admin")) {
                if (args[1].equalsIgnoreCase("banco-admin")) {
                    completions = getMatches(args[3], Utils.getGuildsTags());
                } else if (args[1].equalsIgnoreCase("forçar-entrada")) {
                    completions = getMatches(args[3], Utils.getOnlinePlayerNames());
                } else if (args[1].equalsIgnoreCase("dados")) {
                    completions = getMatches(args[3], Arrays.asList("editar", "deletar", "resetar"));
                } else if (args[1].equalsIgnoreCase("promover-admin")) {
                    completions = getMatches(args[3], Arrays.asList("Sub-líder", "Capitão", "Recrutador", "Leal", "Membro"));
                }
            }
        } else if (args.length == 5) {
            if (args[0].equalsIgnoreCase("admin")) {
                if (args[1].equalsIgnoreCase("dados")) {
                    completions = getMatches(args[4], Arrays.asList("jogadores", "guildas"));
                } else if (args[1].equalsIgnoreCase("banco-admin")) {
                    completions = getMatches(args[4], Arrays.asList("<quantidade>"));
                }
            }
        } else if (args.length == 6) {
            if (args[0].equalsIgnoreCase("admin")) {
                if (args[1].equalsIgnoreCase("dados")) {
                    if (args[4].equalsIgnoreCase("guildas")) {
                        completions = getMatches(args[5], Stream.concat(Utils.getGuildsTags().stream(), Stream.of("todos"))
                                .collect(Collectors.toList()));
                    } else if (args[4].equalsIgnoreCase("jogadores")) {
                        completions =  getMatches(args[5], Utils.getOnlinePlayerNames());
                    }
                }
            }
        } else if (args.length == 7) {
            if (args[0].equalsIgnoreCase("admin")) {
                if (args[1].equalsIgnoreCase("dados")) {
                    if (args[3].equalsIgnoreCase("editar")) {
                        if (args[4].equalsIgnoreCase("jogadores")) {
                            completions =  getMatches(args[5], Arrays.asList("kills", "deaths"));
                        }
                    }
                }
            }
        }

        return completions;
    }

    private List<String> getMatches(String input, List<String> possibilities) {
        List<String> matches = new ArrayList<>();
        String lower = input.toLowerCase();
        for (String possibility : possibilities) {
            if (possibility.toLowerCase().startsWith(lower)) {
                matches.add(possibility);
            }
        }
        return matches;
    }

    private List<String> getMatches(String input, List<String> possibilities, List<String> admin) {
        List<String> combined = new ArrayList<>(possibilities);
        combined.addAll(admin);

        List<String> matches = new ArrayList<>();
        String lower = input.toLowerCase();
        for (String possibility : combined) {
            if (possibility.toLowerCase().startsWith(lower)) {
                matches.add(possibility);
            }
        }
        return matches;
    }
}
