package net.roxymc.slime.world.biome;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.roxymc.slime.CompoundBinaryTagHolder;

public interface Biomes extends CompoundBinaryTagHolder {
    @FunctionalInterface
    interface Deserializer {
        Biomes deserialize(CompoundBinaryTag tag);
    }
}
