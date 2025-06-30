package net.minesky.commands.tabcompleter;

import net.minesky.utils.Utils;
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

public class GuildTabCompleter implements TabCompleter {
    private final List<String> subCommands = Arrays.asList("criar", "editar", "acabar", "sair", "convidar", "aceitar", "rejeitar", "estandarte", "aliado", "rival", "anunciar", "mural", "ajuda");
    private final List<String> AdminsubCommands = Arrays.asList("spy", "reconnect", "reload");

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender s, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (s instanceof Player player) {
            player.playSound(player.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1, 1);
        }

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            if (s.hasPermission("mineskyguildas.reload") || s.hasPermission("mineskyguildas.spy")) completions = getMatches(args[0], subCommands, AdminsubCommands);
            else completions = getMatches(args[0], subCommands);
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("convidar")) {
                completions = getMatches(args[1], Utils.getOnlinePlayerNames());
            } else if (args[0].equalsIgnoreCase("aliado") || args[0].equalsIgnoreCase("rival")) {
                completions = getMatches(args[1], Arrays.asList("adicionar", "remover"));
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("aliado") || args[0].equalsIgnoreCase("rival")) {
                if (s instanceof Player player) {
                    player.playSound(player.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1, 1);
                    completions = getMatches(args[2], Utils.getGuildsTags(player));
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
