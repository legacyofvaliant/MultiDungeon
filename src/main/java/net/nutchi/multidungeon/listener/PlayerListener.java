package net.nutchi.multidungeon.listener;

import lombok.RequiredArgsConstructor;
import net.nutchi.multidungeon.MultiDungeon;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@RequiredArgsConstructor
public class PlayerListener implements Listener {
    private final MultiDungeon plugin;

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getTo() != null && isDifferentCoordinate(event.getFrom(), event.getTo()) && plugin.getBossRoom().isSpectator(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    private boolean isDifferentCoordinate(Location loc1, Location loc2) {
        return loc1.getX() != loc2.getX() || loc1.getY() != loc2.getY() || loc1.getZ() != loc2.getZ();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getDungeonManager().quitPlayer(event.getPlayer().getUniqueId());
    }
}
