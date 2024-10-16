package net.roxymc.slime.world.chunk;

import net.roxymc.slime.world.biome.Biomes;
import net.roxymc.slime.world.block.state.BlockStates;
import org.jspecify.annotations.Nullable;

public interface Section {
    int LIGHT_ARRAY_LENGTH = 16 * 16 * 16 / (8 / 4);

    byte @Nullable [] blockLight();

    byte @Nullable [] skyLight();

    BlockStates blockStates();

    Biomes biomes();

    @FunctionalInterface
    interface Deserializer {
        Section deserialize(byte @Nullable [] blockLight, byte @Nullable [] skyLight, BlockStates blockStates, Biomes biomes);
    }
}
