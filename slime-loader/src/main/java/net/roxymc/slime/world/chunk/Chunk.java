package net.roxymc.slime.world.chunk;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.roxymc.slime.CompoundBinaryTagHolder;
import net.roxymc.slime.world.Heightmaps;
import net.roxymc.slime.world.block.entity.BlockEntity;
import net.roxymc.slime.world.entity.Entity;

public interface Chunk extends CompoundBinaryTagHolder {
    int x();

    int z();

    Section[] sections();

    Heightmaps heightmaps();

    BlockEntity[] blockEntities();

    Entity[] entities();

    @FunctionalInterface
    interface Deserializer {
        Chunk deserialize(int x, int z, Section[] sections, Heightmaps heightmaps, BlockEntity[] blockEntities, Entity[] entities, CompoundBinaryTag tag);
    }
}
