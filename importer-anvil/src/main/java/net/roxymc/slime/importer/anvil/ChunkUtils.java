package net.roxymc.slime.importer.anvil;

import net.kyori.adventure.nbt.ListBinaryTag;
import net.roxymc.slime.world.block.entity.BlockEntity;
import net.roxymc.slime.world.block.state.BlockStates;
import net.roxymc.slime.world.chunk.Section;
import net.roxymc.slime.world.entity.Entity;

import java.util.Arrays;

final class ChunkUtils {
    private static final String PALETTE_TAG = "palette";
    private static final String NAME_TAG = "Name";
    private static final String BLOCK_AIR = "minecraft:air";

    private ChunkUtils() {
    }

    static boolean isEmpty(Section[] sections, BlockEntity[] blockEntities, Entity[] entities) {
        if (Arrays.stream(sections).anyMatch(section -> !isOnlyAir(section.blockStates()))) {
            return false;
        }

        return blockEntities.length == 0 && entities.length == 0;
    }

    static boolean isOnlyAir(BlockStates blockStates) {
        ListBinaryTag palette = blockStates.tag().getList(PALETTE_TAG);
        if (palette.isEmpty() || palette.size() > 1) {
            return palette.isEmpty();
        }

        return palette.getCompound(0).getString(NAME_TAG).equals(BLOCK_AIR);
    }
}
