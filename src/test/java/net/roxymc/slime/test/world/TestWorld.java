package net.roxymc.slime.test.world;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.roxymc.slime.world.World;
import net.roxymc.slime.world.chunk.Chunk;

@Data
@Accessors(fluent = true)
@AllArgsConstructor
public class TestWorld implements World {
    private final int version;
    private final Chunk[] chunks;
    private final CompoundBinaryTag tag;
}
