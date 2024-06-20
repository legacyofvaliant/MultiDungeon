package net.nutchi.multidungeon;

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.*;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.session.SessionManager;
import com.sk89q.worldedit.world.World;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

@RequiredArgsConstructor
public class DungeonGenerator {
    private final MultiDungeon plugin;

    public void saveDungeon(Player player, String name) {
        com.sk89q.worldedit.entity.Player actor = BukkitAdapter.adapt(player);
        SessionManager manager = WorldEdit.getInstance().getSessionManager();
        LocalSession localSession = manager.get(actor);

        World selectionWorld = localSession.getSelectionWorld();
        if (selectionWorld != null) {
            try {
                ClipboardHolder clipboard = localSession.getClipboard();

                File file = getSchematicFile(name);

                getSchematicParentFolder().mkdirs();

                try (ClipboardWriter writer = BuiltInClipboardFormat.SPONGE_SCHEMATIC.getWriter(new FileOutputStream(file))) {
                    writer.write(clipboard.getClipboard());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (EmptyClipboardException e) {
                e.printStackTrace();
            }
        }
    }

    public void restoreDungeon(String name, Location baseLocation) {
        File file = getSchematicFile(name);

        ClipboardFormat format = ClipboardFormats.findByFile(file);
        try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
            Clipboard clipboard = reader.read();

            if (baseLocation.getWorld() != null) {
                World world = BukkitAdapter.adapt(baseLocation.getWorld());

                try (EditSession editSession = WorldEdit.getInstance().newEditSession(world)) {
                    Operation operation = new ClipboardHolder(clipboard)
                            .createPaste(editSession)
                            .copyBiomes(true)
                            .copyEntities(true)
                            .to(BlockVector3.at(baseLocation.getX(), baseLocation.getY(), baseLocation.getZ()))
                            .build();
                    Operations.complete(operation);
                } catch (WorldEditException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File getSchematicFile(String name) {
        return new File(getSchematicParentFolder(), name + ".schem");
    }

    private File getSchematicParentFolder() {
        return new File(plugin.getDataFolder(), "dungeons");
    }
}
