package net.nutchi.multidungeon;

import lombok.RequiredArgsConstructor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class BossRoom {
    private final MultiDungeon plugin;
    private final List<String> bossRoomPlayingPlayers = new ArrayList<>();
    private final Map<String, GameMode> lastGameModeMap = new HashMap<>();

    public void setBossRoomPlaying(Player player) {
        bossRoomPlayingPlayers.add(player.getName());
    }

    public boolean isBossRoomPlaying(Player player) {
        return bossRoomPlayingPlayers.contains(player.getName());
    }

    public void setSpectator(String player) {
        plugin.getPlayer(player).ifPresent(this::setSpectator);
    }

    public void setSpectator(Player player) {
        lastGameModeMap.put(player.getName(), player.getGameMode());
        player.setGameMode(GameMode.SPECTATOR);
    }

    public void unsetSpectator(String player) {
        if (lastGameModeMap.containsKey(player)) {
            plugin.getPlayer(player).ifPresent(p -> p.setGameMode(lastGameModeMap.get(player)));
            lastGameModeMap.remove(player);
        }
    }

    public boolean isSpectator(Player player) {
        return lastGameModeMap.containsKey(player.getName());
    }

    public void restorePlayersGamemode() {
        lastGameModeMap.forEach((name, gameMode) -> plugin.getPlayer(name).ifPresent(p -> p.setGameMode(gameMode)));
    }

    public List<String> getSpectatorNames() {
        return new ArrayList<>(lastGameModeMap.keySet());
    }
}
