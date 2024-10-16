package net.roxymc.slime.world;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.roxymc.slime.CompoundBinaryTagHolder;
import net.roxymc.slime.world.chunk.Chunk;

public interface World extends CompoundBinaryTagHolder {
    int version();

    Chunk[] chunks();

    @FunctionalInterface
    interface Deserializer {
        World deserialize(int version, Chunk[] chunks, CompoundBinaryTag tag);
    }
}
