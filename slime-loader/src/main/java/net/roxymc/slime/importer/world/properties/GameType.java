package net.roxymc.slime.importer.world.properties;

import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public record GameType(int id, @Nullable String name) {
    private static final Map<Integer, GameType> DEFAULTS = new HashMap<>();

    public static final GameType SURVIVAL = gameType(0, "survival");
    public static final GameType CREATIVE = gameType(1, "creative");
    public static final GameType ADVENTURE = gameType(2, "adventure");
    public static final GameType SPECTATOR = gameType(3, "spectator");

    private static GameType gameType(int id, String name) {
        return DEFAULTS.computeIfAbsent(id, $ -> new GameType(id, name));
    }

    public static GameType byId(int id) {
        return DEFAULTS.getOrDefault(id, new GameType(id, null));
    }
}
