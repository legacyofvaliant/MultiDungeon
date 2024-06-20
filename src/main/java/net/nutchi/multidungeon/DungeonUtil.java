package net.nutchi.multidungeon;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class DungeonUtil {
    private final MultiDungeon plugin;

    public void executeCommandAtPlayerPlayingReplicaRelativeCoordinate(String player, CommandSender sender, String command) {
        plugin.getPlayerUuid(player).flatMap(uuid -> plugin.getDungeonManager().getPlayerPlayingReplicaStartLocation(uuid)).ifPresent(startLoc -> {
            Map<String, String> replacements = new HashMap<>();

            Matcher matcher = Pattern.compile("[xyz]:[+-]?([0-9]*[.])?[0-9]+").matcher(command);
            while (matcher.find()) {
                getAbsoluteCoordinate(startLoc, matcher.group()).ifPresent(d -> replacements.put(matcher.group(), String.valueOf(d)));
            }

            String replaced = replacements.keySet().stream().reduce(command, (part, element) -> part.replace(element, replacements.get(element)));

            plugin.getServer().dispatchCommand(sender, replaced);
        });
    }

    private OptionalDouble getAbsoluteCoordinate(Location baseLoc, String relativeCoordinate) {
        String[] parts = relativeCoordinate.split(":");
        if (parts.length == 2) {
            String axis = parts[0];
            String relativeStr = parts[1];
            if (NumberUtils.isParsable(relativeStr)) {
                double relative = Double.parseDouble(relativeStr);
                switch (axis) {
                    case "x":
                        return OptionalDouble.of(baseLoc.getX() + relative);
                    case "y":
                        return OptionalDouble.of(baseLoc.getY() + relative);
                    case "z":
                        return OptionalDouble.of(baseLoc.getZ() + relative);
                }
            }
        }

        return OptionalDouble.empty();
    }
}
