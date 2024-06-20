package net.nutchi.multidungeon;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;

import java.io.*;
import java.sql.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class Storage {
    private final MultiDungeon plugin;

    private Connection connection;
    private final Gson gson = new Gson();

    public boolean connect() {
        try {
            File file = new File(plugin.getDataFolder(), "database.db");
            if (!file.exists()) {
                plugin.getDataFolder().mkdir();
                file.createNewFile();
            }

            String url = "jdbc:sqlite:" + file.getAbsolutePath().replace("\\", "/");
            connection = DriverManager.getConnection(url);

            return true;
        } catch (IOException | SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void shutdown() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void init() {
        try (InputStream inputStream = plugin.getResource("setup.sql")) {
            if (inputStream != null) {
                String sql = new BufferedReader(new InputStreamReader(inputStream)).lines().collect(Collectors.joining("\n"));
                for (String query : sql.split(";")) {
                    try (PreparedStatement statement = connection.prepareStatement(query)) {
                        statement.execute();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadDungeons() {
        try (PreparedStatement dungeonStatement = connection.prepareStatement("SELECT * FROM dungeons")) {
            ResultSet dungeonResult = dungeonStatement.executeQuery();

            while (dungeonResult.next()) {
                String dungeonName = dungeonResult.getString("name");
                int playerLimit = dungeonResult.getInt("player_limit");

                Dungeon dungeon = new Dungeon(dungeonName, playerLimit);
                plugin.getDungeonManager().getDungeons().add(dungeon);

                try (PreparedStatement replicaStatement = connection.prepareStatement("SELECT * FROM replicas WHERE dungeon_name = ?")) {
                    replicaStatement.setString(1, dungeonName);

                    ResultSet replicaResult = replicaStatement.executeQuery();
                    while (replicaResult.next()) {
                        int replicaId = replicaResult.getInt("id");
                        Location startLoc = gson.fromJson(replicaResult.getString("start_location"), GsonParsableLocation.class).getLocation(plugin);

                        if (startLoc != null) {
                            dungeon.getDungeonReplicas().add(new DungeonReplica(dungeonName, replicaId, startLoc));
                        }

                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveDungeon(String name) {
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO dungeons (name) VALUES (?)")) {
            statement.setString(1, name);

            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveReplica(int id, String dungeonName, Location loc) {
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO replicas (id, dungeon_name, start_location) VALUES (?, ?, ?)")) {
            statement.setInt(1, id);
            statement.setString(2, dungeonName);
            statement.setString(3, gson.toJson(GsonParsableLocation.fromLocation(loc)));

            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteReplica(int id, String dungeonName) {
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM replicas WHERE id = ? AND dungeon_name = ?")) {
            statement.setInt(1, id);
            statement.setString(2, dungeonName);

            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void savePlayerLimit(String dungeonName, int playerLimit) {
        try (PreparedStatement statement = connection.prepareStatement("UPDATE dungeons SET player_limit = ? WHERE name = ?")) {
            statement.setInt(1, playerLimit);
            statement.setString(2, dungeonName);

            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteDungeon(String name) {
        String[] queries = {
                "DELETE FROM dungeons WHERE name = ?",
                "DELETE FROM replicas WHERE dungeon_name = ?"
        };

        for (String query : queries) {
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, name);

                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
