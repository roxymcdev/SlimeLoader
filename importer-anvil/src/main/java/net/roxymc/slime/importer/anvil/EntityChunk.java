package net.roxymc.slime.importer.anvil;

import net.roxymc.slime.world.entity.Entity;

record EntityChunk(int x, int z, Entity[] entities) {
}
