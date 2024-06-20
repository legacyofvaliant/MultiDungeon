CREATE TABLE IF NOT EXISTS dungeons
(
    name                varchar(16) NOT NULL PRIMARY KEY,
    player_limit        int(10) NOT NULL DEFAULT 10
);

CREATE TABLE IF NOT EXISTS replicas
(
    id                  int(10) NOT NULL,
    dungeon_name        varchar(16) NOT NULL,
    start_location      text NOT NULL,
    UNIQUE (id, dungeon_name)
);

CREATE TABLE IF NOT EXISTS db_version
(
    version             int(10) NOT NULL DEFAULT 1
);
