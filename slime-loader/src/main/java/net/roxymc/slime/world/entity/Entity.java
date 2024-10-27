package net.roxymc.slime.world.entity;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.roxymc.slime.CompoundBinaryTagHolder;

public interface Entity extends CompoundBinaryTagHolder {
    @FunctionalInterface
    interface Deserializer {
        Entity deserialize(CompoundBinaryTag tag);
    }
}
