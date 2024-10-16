package net.roxymc.slime.test.world.chunk;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.roxymc.slime.world.Heightmaps;
import net.roxymc.slime.world.block.entity.BlockEntity;
import net.roxymc.slime.world.chunk.Chunk;
import net.roxymc.slime.world.chunk.Section;
import net.roxymc.slime.world.entity.Entity;

@Data
@Accessors(fluent = true)
@AllArgsConstructor
public class TestChunk implements Chunk {
    private final int x;
    private final int z;
    private final Section[] sections;
    private final Heightmaps heightmaps;
    private final BlockEntity[] blockEntities;
    private final Entity[] entities;
    private final CompoundBinaryTag tag;
}
