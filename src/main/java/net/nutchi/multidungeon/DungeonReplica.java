package net.nutchi.multidungeon;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.HumanEntity;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
@RequiredArgsConstructor
public class DungeonReplica {
    private final String dungeonName;
    private final int id;
    private final Location startLocation;
    private final List<UUID> players = new ArrayList<>();
    private boolean locked = false;

    public int getPlayerNumber() {
        return players.size();
    }

    public String getInfo(MultiDungeon plugin) {
        return "id: " + id + "; loc: (" + startLocation.getWorld().getName() + ", " + startLocation.getBlockX() + ", " + startLocation.getBlockY() + ", " + startLocation.getBlockZ() + "); online (" + getPlayerNumber() + "): " + getOnlinePlayers(plugin);
    }

    private List<String> getOnlinePlayers(MultiDungeon plugin) {
        return players.stream()
                .map(plugin::getPlayer)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(HumanEntity::getName)
                .collect(Collectors.toList());
    }

    public void joinPlayer(UUID player) {
        players.add(player);
    }

    public void leavePlayers() {
        players.clear();
    }

    public void leavePlayer(UUID player) {
        players.remove(player);
    }

    public boolean isEmpty() {
        return players.isEmpty();
    }

    public boolean isPlaying(UUID player) {
        return players.contains(player);
    }
}
