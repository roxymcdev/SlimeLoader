package net.roxymc.slime.test.world.block.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.roxymc.slime.world.block.entity.BlockEntity;

@Data
@Accessors(fluent = true)
@AllArgsConstructor
public class TestBlockEntity implements BlockEntity {
    private final CompoundBinaryTag tag;
}
