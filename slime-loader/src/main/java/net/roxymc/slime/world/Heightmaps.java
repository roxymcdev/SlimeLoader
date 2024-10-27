package net.roxymc.slime.world;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.roxymc.slime.CompoundBinaryTagHolder;

public interface Heightmaps extends CompoundBinaryTagHolder {
    @FunctionalInterface
    interface Deserializer {
        Heightmaps deserialize(CompoundBinaryTag tag);
    }
}
