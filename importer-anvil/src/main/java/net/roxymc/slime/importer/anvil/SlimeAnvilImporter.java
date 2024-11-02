package net.roxymc.slime.importer.anvil;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.BinaryTagIO;
import net.kyori.adventure.nbt.BinaryTagTypes;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.roxymc.slime.importer.ImportResult;
import net.roxymc.slime.importer.SlimeImporter;
import net.roxymc.slime.importer.world.properties.Difficulty;
import net.roxymc.slime.importer.world.properties.GameType;
import net.roxymc.slime.importer.world.properties.SpawnPosition;
import net.roxymc.slime.importer.world.properties.WorldProperties;
import net.roxymc.slime.loader.SlimeLoader;
import net.roxymc.slime.world.Heightmaps;
import net.roxymc.slime.world.block.entity.BlockEntity;
import net.roxymc.slime.world.chunk.Chunk;
import net.roxymc.slime.world.chunk.Section;
import net.roxymc.slime.world.entity.Entity;
import org.jspecify.annotations.Nullable;

import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import static net.roxymc.slime.util.ObjectUtils.nonNull;

public class SlimeAnvilImporter implements SlimeImporter {
    private static final Entity[] EMPTY_ENTITIES = new Entity[0];
    private static final int SECTION_SIZE = 4096;
    private static final String LEVEL_DAT = "level.dat";
    private static final String REGION_DIR = "region";
    private static final String ENTITIES_DIR = "entities";
    private static final String MCA = ".mca";

    // Level Data
    private static final String DATA_TAG = "Data";
    private static final String DATA_VERSION_TAG = "DataVersion";

    // World Properties
    private static final String DIFFICULTY_TAG = "Difficulty";
    private static final String DIFFICULTY_LOCKED_TAG = "DifficultyLocked";
    private static final String GAME_TYPE_TAG = "GameType";
    private static final String HARDCORE_TAG = "hardcore";
    private static final String SPAWN_X_TAG = "SpawnX";
    private static final String SPAWN_Y_TAG = "SpawnY";
    private static final String SPAWN_Z_TAG = "SpawnZ";

    // Entity Chunk Data
    private static final String POSITION_TAG = "Position";
    private static final String ENTITIES_TAG = "Entities";

    // Chunk Data
    private static final String CHUNK_STATUS_TAG = "Status";
    private static final String FULL_CHUNK = "minecraft:full";
    private static final String CHUNK_X_TAG = "xPos";
    private static final String CHUNK_Y_TAG = "yPos";
    private static final String CHUNK_Z_TAG = "zPos";
    private static final String SECTIONS_TAG = "sections";
    private static final String BLOCK_LIGHT_TAG = "BlockLight";
    private static final String SKY_LIGHT_TAG = "SkyLight";
    private static final String BLOCK_STATES_TAG = "block_states";
    private static final String BIOMES_TAG = "biomes";
    private static final String HEIGHTMAPS_TAG = "Heightmaps";
    private static final String BLOCK_ENTITIES_TAG = "block_entities";

    private final SlimeLoader slimeLoader;
    private final Set<String> preservedWorldTags;
    private final Set<String> preservedChunkTags;

    private SlimeAnvilImporter(Builder builder) {
        this.slimeLoader = builder.slimeLoader;
        this.preservedWorldTags = builder.preservedWorldTags;
        this.preservedChunkTags = builder.preservedChunkTags;
    }

    public static Builder builder(SlimeLoader slimeLoader) {
        return new Builder(slimeLoader);
    }

    @Override
    public Set<String> preservedWorldTags() {
        return preservedWorldTags;
    }

    @Override
    public Set<String> preservedChunkTags() {
        return preservedChunkTags;
    }

    @Override
    public ImportResult importWorld(File source) throws IOException {
        LevelData levelData = readLevelData(source);
        Map<ChunkPos, EntityChunk> entityChunks = Arrays.stream(readEntityChunks(source)).collect(Collectors.toMap(
                chunk -> new ChunkPos(chunk.x(), chunk.z()),
                Function.identity()
        ));
        Chunk[] chunks = readChunks(source, entityChunks);

        return new ImportResult(
                slimeLoader.deserializers().world().deserialize(
                        levelData.dataVersion(),
                        chunks,
                        levelData.tag()
                ),
                levelData.properties()
        );
    }

    private LevelData readLevelData(File root) throws IOException {
        File levelDat = new File(root, LEVEL_DAT);

        CompoundBinaryTag tag;
        try (FileInputStream is = new FileInputStream(levelDat)) {
            tag = BinaryTagIO.reader().read(is, BinaryTagIO.Compression.GZIP);
        }

        int dataVersion = tag.getCompound(DATA_TAG).getInt(DATA_VERSION_TAG);

        CompoundBinaryTag customDataTag = readCustomData(tag, preservedWorldTags);

        WorldProperties properties = new WorldProperties(
                Difficulty.byId(tag.getByte(DIFFICULTY_TAG, (byte) 1)),
                tag.getBoolean(DIFFICULTY_LOCKED_TAG),
                GameType.byId(tag.getInt(GAME_TYPE_TAG, 1)),
                tag.getBoolean(HARDCORE_TAG),
                new SpawnPosition(
                        tag.getInt(SPAWN_X_TAG),
                        tag.getInt(SPAWN_Y_TAG),
                        tag.getInt(SPAWN_Z_TAG)
                )
        );

        return new LevelData(dataVersion, properties, customDataTag);
    }

    private EntityChunk[] readEntityChunks(File root) throws IOException {
        return readRegionFiles(new File(root, ENTITIES_DIR), EntityChunk[]::new, this::readEntityChunk);
    }

    private EntityChunk readEntityChunk(CompoundBinaryTag tag) {
        int[] position = tag.getIntArray(POSITION_TAG);
        int x = position[0];
        int z = position[1];

        Entity[] entities = tag.getList(ENTITIES_TAG, BinaryTagTypes.COMPOUND).stream()
                .map(CompoundBinaryTag.class::cast)
                .map(slimeLoader.deserializers().entity()::deserialize)
                .toArray(Entity[]::new);

        return new EntityChunk(x, z, entities);
    }

    private Chunk[] readChunks(File root, Map<ChunkPos, EntityChunk> entityChunks) throws IOException {
        return readRegionFiles(new File(root, REGION_DIR), Chunk[]::new, tag -> readChunk(tag, entityChunks));
    }

    private @Nullable Chunk readChunk(CompoundBinaryTag tag, Map<ChunkPos, EntityChunk> entityChunks) {
        if (!tag.getString(CHUNK_STATUS_TAG).equals(FULL_CHUNK)) {
            return null;
        }

        int x = tag.getInt(CHUNK_X_TAG);
        int z = tag.getInt(CHUNK_Z_TAG);

        int minSectionY = tag.getInt(CHUNK_Y_TAG);

        Section[] sections = tag.getList(SECTIONS_TAG, BinaryTagTypes.COMPOUND).stream()
                .map(CompoundBinaryTag.class::cast)
                .filter(binaryTag -> binaryTag.getInt("Y") >= minSectionY)
                .map(binaryTag -> {
                    //noinspection DataFlowIssue
                    byte[] blockLight = binaryTag.getByteArray(BLOCK_LIGHT_TAG, null);
                    //noinspection DataFlowIssue
                    byte[] skyLight = binaryTag.getByteArray(SKY_LIGHT_TAG, null);

                    CompoundBinaryTag blockStatesTag = binaryTag.getCompound(BLOCK_STATES_TAG);
                    CompoundBinaryTag biomesTag = binaryTag.getCompound(BIOMES_TAG);

                    return slimeLoader.deserializers().section().deserialize(
                            blockLight,
                            skyLight,
                            slimeLoader.deserializers().blockStates().deserialize(blockStatesTag),
                            slimeLoader.deserializers().biomes().deserialize(biomesTag)
                    );
                })
                .toArray(Section[]::new);

        Heightmaps heightmaps = slimeLoader.deserializers().heightmaps().deserialize(
                tag.getCompound(HEIGHTMAPS_TAG)
        );

        BlockEntity[] blockEntities = tag.getList(BLOCK_ENTITIES_TAG, BinaryTagTypes.COMPOUND).stream()
                .map(CompoundBinaryTag.class::cast)
                .map(slimeLoader.deserializers().blockEntity()::deserialize)
                .toArray(BlockEntity[]::new);

        ChunkPos chunkPos = new ChunkPos(x, z);
        Entity[] entities = entityChunks.containsKey(chunkPos) ? entityChunks.get(chunkPos).entities() : EMPTY_ENTITIES;

        CompoundBinaryTag customDataTag = readCustomData(tag, preservedChunkTags);

        return slimeLoader.deserializers().chunk().deserialize(
                x,
                z,
                sections,
                heightmaps,
                blockEntities,
                entities,
                customDataTag
        );
    }

    private CompoundBinaryTag readCustomData(CompoundBinaryTag tag, Set<String> tags) {
        if (tags.isEmpty()) {
            return CompoundBinaryTag.empty();
        } else {
            CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();

            tags.forEach(tagName -> {
                BinaryTag binaryTag = tag.get(tagName);
                if (binaryTag == null) {
                    return;
                }

                builder.put(tagName, binaryTag);
            });

            return builder.build();
        }
    }

    private <T> T[] readRegionFiles(File regionDir, IntFunction<T[]> generator, Function<CompoundBinaryTag, T> function) throws IOException {
        File[] files = regionDir.listFiles((dir, name) -> name.endsWith(MCA));
        if (files == null || files.length == 0) {
            return generator.apply(0);
        }

        List<T[]> list = new ArrayList<>();
        for (File file : files) {
            list.add(readRegionFile(file, generator, function));
        }

        return list.stream().reduce(generator.apply(0), (arr1, arr2) ->
                Stream.concat(Arrays.stream(arr1), Arrays.stream(arr2)).toArray(generator)
        );
    }

    private <T> T[] readRegionFile(File regionFile, IntFunction<T[]> generator, Function<CompoundBinaryTag, T> function) throws IOException {
        byte[] bytes;
        try (FileInputStream is = new FileInputStream(regionFile)) {
            bytes = is.readAllBytes();
        }

        ByteArrayDataInput in = ByteStreams.newDataInput(bytes);

        List<T> data = new ArrayList<>(1024);
        for (int i = 0; i < 1024; i++) {
            int entry;
            try {
                entry = in.readInt();
            } catch (IllegalStateException e) {
                break;
            }
            if (entry == 0) continue;

            int offset = (entry >>> 8) * SECTION_SIZE;
            int size = (entry & 0xF) * SECTION_SIZE;

            ByteArrayDataInput headerIn = ByteStreams.newDataInput(new ByteArrayInputStream(bytes, offset, size));
            int chunkSize = headerIn.readInt() - 1;
            int compressionScheme = headerIn.readByte();

            InputStream chunkStream = new ByteArrayInputStream(bytes, offset + 5, chunkSize);
            InputStream decompressorStream = switch (compressionScheme) {
                case 1 -> new GZIPInputStream(chunkStream);
                case 2 -> new InflaterInputStream(chunkStream);
                case 3 -> chunkStream;
                default -> throw new IllegalStateException("Unexpected value: " + compressionScheme);
            };

            CompoundBinaryTag tag = BinaryTagIO.reader().read(decompressorStream);
            T element = function.apply(tag);
            //noinspection ConstantValue
            if (element != null) {
                data.add(element);
            }
        }

        return data.toArray(generator);
    }

    public static class Builder {
        private final SlimeLoader slimeLoader;
        private Set<String> preservedWorldTags = Collections.emptySet();
        private Set<String> preservedChunkTags = Collections.emptySet();

        private Builder(SlimeLoader slimeLoader) {
            this.slimeLoader = nonNull(slimeLoader, "slimeLoader");
        }

        public Builder preserveWorldTags(String... tags) {
            preservedWorldTags = Set.of(tags);
            return this;
        }

        public Builder preserveChunkTags(String... tags) {
            preservedChunkTags = Set.of(tags);
            return this;
        }

        public SlimeAnvilImporter build() {
            return new SlimeAnvilImporter(this);
        }
    }
}
