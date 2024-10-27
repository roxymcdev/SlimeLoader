package net.roxymc.slime.test.world.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.roxymc.slime.world.entity.Entity;

@Data
@Accessors(fluent = true)
@AllArgsConstructor
public class TestEntity implements Entity {
    private final CompoundBinaryTag tag;
}
