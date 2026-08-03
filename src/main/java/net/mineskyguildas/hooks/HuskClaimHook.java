package net.mineskyguildas.hooks;

import net.mineskyguildas.data.Guilds;
import net.mineskyguildas.handlers.GuildHandler;
import net.william278.huskclaims.api.HuskClaimsAPI;
import net.william278.huskclaims.claim.Claim;
import net.william278.huskclaims.position.Position;
import net.william278.huskclaims.position.World;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.Optional;
import java.util.UUID;

public class HuskClaimHook implements ClaimHook {
    private final HuskClaimsAPI api;

    public HuskClaimHook() {
        this.api = HuskClaimsAPI.getInstance();
    }

    @Override
    public Guilds getClaimOwnerGuildAt(Location loc) {
        if (loc == null) return null;
        try {
            World world = fromBukkit(loc.getWorld());
            Position position = Position.at(loc.getX(), loc.getY(), loc.getZ(), world);
            Optional<Claim> claimOpt = api.getClaimAt(position);

            if (claimOpt.isPresent()) {
                Claim claim = claimOpt.get();

                if (claim.isAdminClaim()) {
                    return null;
                }

                Optional<UUID> ownerOpt = claim.getOwner();
                if (ownerOpt.isPresent()) {
                    return GuildHandler.getGuildByPlayer(ownerOpt.get());
                }
            }
        } catch (Exception e) {
            Bukkit.getLogger().warning("Erro ao verificar claim no HuskClaimHook: " + e.getMessage());
        }
        return null;
    }

//    @Override
//    public Guilds getClaimOwnerGuildAt(Location loc) {
//        World world = fromBukkit(loc.getWorld());
//        Position position = Position.at(loc.getX(), loc.getY(), loc.getZ(), world);
//        Optional<Claim> optionalClaim = HuskClaimsAPI.getInstance().getClaimAt(position);
//        Claim claim = optionalClaim.get();
//        UUID ownerId = claim.getOwner().get();
//        return GuildHandler.getGuildByPlayer(ownerId);
//    }

    public static World fromBukkit(org.bukkit.World bukkitWorld) {
        if (bukkitWorld == null) {
            return null;
        }

        return net.william278.huskclaims.position.World.of(
                bukkitWorld.getName(),
                bukkitWorld.getUID(),
                bukkitWorld.getEnvironment().name()
        );
    }
}