package net.roxymc.slime.importer.world.properties;

import org.jspecify.annotations.NullUnmarked;

@NullUnmarked
public record WorldProperties(
        Difficulty difficulty,
        boolean difficultyLocked,
        GameType gameType,
        boolean hardcore,
        SpawnPosition position
) {
}
