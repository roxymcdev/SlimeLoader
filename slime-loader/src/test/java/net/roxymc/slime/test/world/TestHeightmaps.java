package net.roxymc.slime.test.world;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.roxymc.slime.world.Heightmaps;

@Data
@Accessors(fluent = true)
@AllArgsConstructor
public class TestHeightmaps implements Heightmaps {
    private final CompoundBinaryTag tag;
}
