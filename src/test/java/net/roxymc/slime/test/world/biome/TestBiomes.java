package net.roxymc.slime.test.world.biome;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.roxymc.slime.world.biome.Biomes;

@Data
@Accessors(fluent = true)
@AllArgsConstructor
public class TestBiomes implements Biomes {
    private final CompoundBinaryTag tag;
}
