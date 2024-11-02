package net.roxymc.slime.importer.anvil;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.roxymc.slime.importer.world.properties.WorldProperties;

record LevelData(int dataVersion, WorldProperties properties, CompoundBinaryTag tag) {
}
