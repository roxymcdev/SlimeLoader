package net.roxymc.slime.test.world.block.state;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.roxymc.slime.world.block.state.BlockStates;

@Data
@Accessors(fluent = true)
@AllArgsConstructor
public class TestBlockStates implements BlockStates {
    private final CompoundBinaryTag tag;
}
