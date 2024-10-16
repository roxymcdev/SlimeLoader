package net.roxymc.slime.world.block.entity;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.roxymc.slime.CompoundBinaryTagHolder;

public interface BlockEntity extends CompoundBinaryTagHolder {
    @FunctionalInterface
    interface Deserializer {
        BlockEntity deserialize(CompoundBinaryTag tag);
    }
}
