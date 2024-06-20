package net.nutchi.multidungeon;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

@Getter
@RequiredArgsConstructor
public class GsonParsableLocation {
    private final @Nullable String world;
    private final double x;
    private final double y;
    private final double z;
    private final float pitch;
    private final float yaw;

    @Nullable
    public Location getLocation(JavaPlugin plugin) {
        World bukkitWorld = world != null ? plugin.getServer().getWorld(world) : null;
        return new Location(bukkitWorld, x, y, z, pitch, yaw);
    }

    public static GsonParsableLocation fromLocation(Location loc) {
        String worldName = loc.getWorld() != null ? loc.getWorld().getName() : null;
        return new GsonParsableLocation(worldName, loc.getX(), loc.getY(), loc.getZ(), loc.getPitch(), loc.getYaw());
    }
}
