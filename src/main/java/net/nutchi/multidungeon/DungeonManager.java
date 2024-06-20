package net.nutchi.multidungeon;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class DungeonManager {
    private final MultiDungeon plugin;
    @Getter
    private final List<Dungeon> dungeons = new ArrayList<>();

    public boolean addDungeon(String name) {
        if (!getDungeon(name).isPresent()) {
            dungeons.add(new Dungeon(name, 10));
            plugin.getStorage().saveDungeon(name);

            return true;
        } else {
            return false;
        }
    }

    public void removeDungeon(String name) {
        dungeons.removeIf(d -> d.getName().equals(name));
        plugin.getStorage().deleteDungeon(name);
    }

    public boolean setPlayerLimit(String name, int playerLimit) {
        if (getDungeon(name).isPresent()) {
            dungeons.removeIf(d -> d.getName().equals(name));
            dungeons.add(new Dungeon(name, playerLimit));
            plugin.getStorage().savePlayerLimit(name, playerLimit);

            return true;
        } else {
            return false;
        }
    }

    public boolean addReplica(String name, Location startLoc) {
        Optional<Dungeon> dungeon = getDungeon(name);
        if (dungeon.isPresent()) {
            int id = dungeon.get().addReplica(name, startLoc);
            plugin.getStorage().saveReplica(id, name, startLoc);

            return true;
        } else {
            return false;
        }
    }

    public boolean removeDungeonReplica(String name, int id) {
        Optional<Dungeon> dungeon = getDungeon(name);
        if (dungeon.isPresent()) {
            dungeon.get().removeReplica(id);
            plugin.getStorage().deleteReplica(id, name);

            return true;
        } else {
            return false;
        }
    }

    public Optional<Location> getPlayerPlayingReplicaStartLocation(UUID uuid) {
        return dungeons
                .stream()
                .map(d -> d.getPlayerPlayingReplicaStartLocation(uuid))
                .filter(Optional::isPresent)
                .findAny()
                .flatMap(l -> l);
    }

    public List<String> getDungeonNames() {
        return dungeons.stream().map(Dungeon::getName).collect(Collectors.toList());
    }

    public String getDungeonInfo(String name) {
        return getDungeon(name).map(Dungeon::getInfo).orElse("");
    }

    public String getDungeonReplicaInfo(String name) {
        return getDungeon(name).map(d -> d.getReplicaInfo(plugin)).orElse("");
    }

    public boolean saveDungeon(Player player, String name) {
        if (getDungeon(name).isPresent()) {
            plugin.getDungeonGenerator().saveDungeon(player, name);

            return true;
        } else {
            return false;
        }
    }

    public void playSingle(String name, Player player) {
        getDungeon(name).ifPresent(d -> d.playSingle(player));
    }

    public void playMulti(String name, Player player) {
        getDungeon(name).ifPresent(d -> d.playMulti(plugin, player));
    }

    public void cancelMultiPlayWaiting(Player player) {
        dungeons.forEach(d -> d.cancelMultiPlayWaiting(player));
    }

    public void finishDungeonReplica(String name, int id) {
        getDungeon(name).ifPresent(d -> d.finishReplica(plugin, id));
    }

    public void quitPlayer(UUID player) {
        dungeons.forEach(d -> d.quitPlayer(plugin, player));
    }

    public Optional<Dungeon> getDungeon(String name) {
        return dungeons.stream().filter(d -> d.getName().equals(name)).findAny();
    }
}
