package net.roxymc.slime.world.block.state;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.roxymc.slime.CompoundBinaryTagHolder;

public interface BlockStates extends CompoundBinaryTagHolder {
    @FunctionalInterface
    interface Deserializer {
        BlockStates deserialize(CompoundBinaryTag tag);
    }
}
