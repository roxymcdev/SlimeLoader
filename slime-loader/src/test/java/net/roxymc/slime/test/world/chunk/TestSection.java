package net.roxymc.slime.test.world.chunk;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import net.roxymc.slime.world.biome.Biomes;
import net.roxymc.slime.world.block.state.BlockStates;
import net.roxymc.slime.world.chunk.Section;

@Data
@Accessors(fluent = true)
@AllArgsConstructor
public final class TestSection implements Section {
    private final byte[] blockLight;
    private final byte[] skyLight;
    private final BlockStates blockStates;
    private final Biomes biomes;
}
