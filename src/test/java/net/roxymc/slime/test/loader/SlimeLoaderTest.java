package net.roxymc.slime.test.loader;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;
import net.roxymc.slime.loader.SlimeLoader;
import net.roxymc.slime.test.world.TestHeightmaps;
import net.roxymc.slime.test.world.TestWorld;
import net.roxymc.slime.test.world.biome.TestBiomes;
import net.roxymc.slime.test.world.block.entity.TestBlockEntity;
import net.roxymc.slime.test.world.block.state.TestBlockStates;
import net.roxymc.slime.test.world.chunk.TestChunk;
import net.roxymc.slime.test.world.chunk.TestSection;
import net.roxymc.slime.test.world.entity.TestEntity;
import net.roxymc.slime.world.World;
import net.roxymc.slime.world.block.entity.BlockEntity;
import net.roxymc.slime.world.chunk.Chunk;
import net.roxymc.slime.world.chunk.Section;
import net.roxymc.slime.world.entity.Entity;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class SlimeLoaderTest {
    private static final SlimeLoader LOADER = SlimeLoader.builder()
            .deserializers(builder -> builder
                    .biomes(TestBiomes::new)
                    .blockEntity(TestBlockEntity::new)
                    .blockStates(TestBlockStates::new)
                    .chunk(TestChunk::new)
                    .section(TestSection::new)
                    .entity(TestEntity::new)
                    .heightmaps(TestHeightmaps::new)
                    .world(TestWorld::new)
            )
            .build();

    private static Section layer(String blockState) {
        return new TestSection(
                null,
                null,
                new TestBlockStates(CompoundBinaryTag.builder()
                        .put("type", StringBinaryTag.stringBinaryTag(blockState))
                        .build()
                ),
                new TestBiomes(CompoundBinaryTag.builder()
                        .put("type", StringBinaryTag.stringBinaryTag("minecraft:plains"))
                        .build()
                )
        );
    }

    private static Chunk chunk(int x, int z, int layers, String blockState) {
        Section[] sectionArray = new Section[layers];
        for (int y = 0; y < layers; y++) {
            sectionArray[y] = layer(blockState);
        }

        return new TestChunk(
                x,
                z,
                sectionArray,
                new TestHeightmaps(CompoundBinaryTag.empty()),
                new BlockEntity[]{
                        new TestBlockEntity(CompoundBinaryTag.builder()
                                .put("Hello", StringBinaryTag.stringBinaryTag("World"))
                                .build()
                        )
                },
                new Entity[]{
                        new TestEntity(CompoundBinaryTag.builder()
                                .put("type", StringBinaryTag.stringBinaryTag("Freddy Fazbear"))
                                .build()
                        )
                },
                CompoundBinaryTag.empty()
        );
    }

    @Test
    public void saveAndLoadTest() throws IOException {
        TestWorld world = new TestWorld(
                0,
                new Chunk[]{
                        chunk(0, 0, 0, "minecraft:air"),
                        chunk(0, 1, 2, "minecraft:stone"),
                        chunk(1, 0, 1, "minecraft:grass_block"),
                        chunk(1, 1, 1, "minecraft:diamond_block")
                },
                CompoundBinaryTag.empty()
        );

        byte[] bytes = LOADER.save(world);

        assert LOADER.load(bytes).equals(world);
    }
}
