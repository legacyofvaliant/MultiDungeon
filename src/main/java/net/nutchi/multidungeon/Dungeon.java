package net.nutchi.multidungeon;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public class Dungeon {
    private final String name;
    private final List<DungeonReplica> dungeonReplicas = new ArrayList<>();
    private final int playerLimit;
    private final Set<String> multiPlayWaitingPlayers = new HashSet<>();

    public int addReplica(String dungeonName, Location startLoc) {
        int id = dungeonReplicas.stream().mapToInt(DungeonReplica::getId).max().orElse(0) + 1;
        dungeonReplicas.add(new DungeonReplica(dungeonName, id, startLoc));
        return id;
    }

    public void removeReplica(int id) {
        dungeonReplicas.removeIf(r -> r.getId() == id);
    }

    public Optional<DungeonReplica> getReplica(int id) {
        return dungeonReplicas.stream().filter(r -> r.getId() == id).findAny();
    }

    public Optional<DungeonReplica> getPlayerPlayingReplica(UUID player) {
        return dungeonReplicas.stream().filter(r -> r.isPlaying(player)).findAny();
    }

    public Optional<Location> getPlayerPlayingReplicaStartLocation(UUID uuid) {
        return dungeonReplicas.stream()
                .filter(r -> r.isPlaying(uuid))
                .map(DungeonReplica::getStartLocation)
                .findAny();
    }

    public String getReplicaInfo(MultiDungeon plugin) {
        return dungeonReplicas.stream().map(r -> r.getInfo(plugin)).collect(Collectors.joining("\n"));
    }

    public void playSingle(Player player) {
        Optional<DungeonReplica> replica = getAvailableReplica();

        if (replica.isPresent()) {
            replica.get().joinPlayer(player.getUniqueId());
            replica.get().setLocked(true);
            player.teleport(replica.get().getStartLocation());
        } else {
            player.sendMessage(ChatColor.GREEN + "ダンジョンが定員に達しているためしばらくお待ちください");
        }
    }

    public void playMulti(MultiDungeon plugin, Player player) {
        if (multiPlayWaitingPlayers.isEmpty() || multiPlayWaitingPlayers.size() < playerLimit) {
            multiPlayWaitingPlayers.add(player.getName());
            player.sendMessage(ChatColor.GREEN + "他のプレイヤーを待機しています...");
        } else {
            Optional<DungeonReplica> replica = getAvailableReplica();

            if (replica.isPresent()) {
                replica.get().joinPlayer(player.getUniqueId());
                player.teleport(replica.get().getStartLocation());

                multiPlayWaitingPlayers.forEach(name -> plugin.getPlayer(name).ifPresent(p -> {
                    replica.get().joinPlayer(p.getUniqueId());
                    p.teleport(replica.get().getStartLocation());
                }));

                replica.get().setLocked(true);
            } else {
                player.sendMessage(ChatColor.GREEN + "ダンジョンが定員に達しているためしばらくお待ちください");
            }
        }
    }

    public void cancelMultiPlayWaiting(Player player) {
        if (multiPlayWaitingPlayers.contains(player.getName())) {
            multiPlayWaitingPlayers.remove(player.getName());
            player.sendMessage(ChatColor.GREEN + "ダンジョンへの参加をキャンセルしました");
        }
    }

    public void finishReplica(MultiDungeon plugin, int id) {
        getReplica(id).ifPresent(r -> {
            r.leavePlayers();
            r.setLocked(false);
            plugin.getDungeonGenerator().restoreDungeon(name, r.getStartLocation());
        });
    }

    public void quitPlayer(MultiDungeon plugin, UUID player) {
        getPlayerPlayingReplica(player).ifPresent(r -> {
            r.leavePlayer(player);
            if (r.isEmpty()) {
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    plugin.getDungeonGenerator().restoreDungeon(name, r.getStartLocation());
                    r.setLocked(false);
                }, 200);
            }
        });
    }

    private Optional<DungeonReplica> getAvailableReplica() {
        return dungeonReplicas.stream()
                .filter(r -> r.getPlayerNumber() < playerLimit && !r.isLocked())
                .min(Comparator.comparingInt(DungeonReplica::getPlayerNumber));
    }

    public String getInfo() {
        return "replicas (" + dungeonReplicas.size() + "): " + dungeonReplicas.stream().map(DungeonReplica::getId).collect(Collectors.toList()) + " (`/md listReplicas <dungeon>` for more info)\n" +
                "player limit: " + playerLimit + "\n" +
                "waiting players (" + multiPlayWaitingPlayers.size() + "): " + multiPlayWaitingPlayers;
    }
}
