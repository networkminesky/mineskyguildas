package net.mineskyguildas.hooks;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class Vault {
    public static Economy economy;

    public static boolean setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    public static boolean deposit(Player player, double amount) {
        if (economy == null) return false;
        economy.depositPlayer(player, amount);
        return true;
    }

    public static boolean withdraw(Player player, double amount) {
        if (economy == null) return false;
        if (economy.getBalance(player) >= amount) {
            economy.withdrawPlayer(player, amount);
            return true;
        }
        return false;
    }

    public static double getBalance(Player player) {
        return economy == null ? 0 : economy.getBalance(player);
    }

    @SuppressWarnings("deprecation")
    public static double getBalance(OfflinePlayer off) {
        try {
            return economy.getBalance(off);
        } catch (Throwable e1) {
            try {
                return economy.getBalance(off.getName());
            } catch (Throwable e2) {
                return 0D;
            }
        }
    }
}
