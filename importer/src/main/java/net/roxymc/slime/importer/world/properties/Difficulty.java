package net.roxymc.slime.importer.world.properties;

import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public record Difficulty(int id, @Nullable String name) {
    private static final Map<Integer, Difficulty> DEFAULTS = new HashMap<>();

    public static final Difficulty PEACEFUL = gameType(0, "peaceful");
    public static final Difficulty EASY = gameType(1, "easy");
    public static final Difficulty NORMAL = gameType(2, "normal");
    public static final Difficulty HARD = gameType(3, "hard");

    private static Difficulty gameType(int id, String name) {
        return DEFAULTS.computeIfAbsent(id, $ -> new Difficulty(id, name));
    }

    public static Difficulty byId(int id) {
        return DEFAULTS.getOrDefault(id, new Difficulty(id, null));
    }
}
