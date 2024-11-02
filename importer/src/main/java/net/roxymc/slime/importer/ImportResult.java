package net.roxymc.slime.importer;

import net.roxymc.slime.importer.world.properties.WorldProperties;
import net.roxymc.slime.world.World;
import org.jspecify.annotations.Nullable;

public record ImportResult(World world, @Nullable WorldProperties properties) {
}
