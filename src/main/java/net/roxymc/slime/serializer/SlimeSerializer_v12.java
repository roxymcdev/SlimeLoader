package net.roxymc.slime.serializer;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.roxymc.slime.loader.SlimeLoader;
import net.roxymc.slime.world.Heightmaps;
import net.roxymc.slime.world.World;
import net.roxymc.slime.world.biome.Biomes;
import net.roxymc.slime.world.block.entity.BlockEntity;
import net.roxymc.slime.world.block.state.BlockStates;
import net.roxymc.slime.world.chunk.Chunk;
import net.roxymc.slime.world.chunk.Section;
import net.roxymc.slime.world.entity.Entity;
import org.jspecify.annotations.Nullable;

import java.io.IOException;

public class SlimeSerializer_v12 extends SlimeSerializer {
    public static final byte VERSION = 12;

    protected static final String TILE_ENTITIES = "tileEntities";
    protected static final String ENTITIES = "entities";

    public SlimeSerializer_v12(SlimeLoader loader) {
        super(loader);
    }

    @Override
    public byte version() {
        return VERSION;
    }

    //<editor-fold desc="Serializers" defaultstate="collapsed">
    @Override
    public void serialize(World world, ByteArrayDataOutput out) throws IOException {
        super.serialize(world, out);

        serializeCompressed(chunksOut -> serializeChunks(world.chunks(), chunksOut), out);
        serializeCompressed(dataOut -> serializeCompound(world.tag(), dataOut, false), out);
    }

    protected void serializeChunks(Chunk[] chunks, ByteArrayDataOutput out) throws IOException {
        out.writeInt(chunks.length);

        for (Chunk chunk : chunks) {
            serializeChunk(chunk, out);
        }
    }

    protected void serializeChunk(Chunk chunk, ByteArrayDataOutput out) throws IOException {
        out.writeInt(chunk.x());
        out.writeInt(chunk.z());

        serializeSections(chunk.sections(), out);
        serializeHeightMaps(chunk.heightmaps(), out);
        serializeBlockEntities(chunk.blockEntities(), out);
        serializeEntities(chunk.entities(), out);
        serializeCompound(chunk.tag(), out);
    }

    protected void serializeSections(Section[] sections, ByteArrayDataOutput out) throws IOException {
        out.writeInt(sections.length);

        for (Section section : sections) {
            serializeSection(section, out);
        }
    }

    protected void serializeSection(Section section, ByteArrayDataOutput out) throws IOException {
        serializeLightData(section.blockLight(), out);
        serializeLightData(section.skyLight(), out);
        serializeBlockStates(section.blockStates(), out);
        serializeBiomes(section.biomes(), out);
    }

    protected void serializeLightData(byte @Nullable [] bytes, ByteArrayDataOutput out) {
        out.writeBoolean(bytes != null);

        if (bytes != null) {
            out.write(bytes);
        }
    }

    protected void serializeBlockStates(BlockStates blockStates, ByteArrayDataOutput out) throws IOException {
        serializeCompound(blockStates.tag(), out);
    }

    protected void serializeBiomes(Biomes biomes, ByteArrayDataOutput out) throws IOException {
        serializeCompound(biomes.tag(), out);
    }

    protected void serializeHeightMaps(Heightmaps heightMaps, ByteArrayDataOutput out) throws IOException {
        serializeCompound(heightMaps.tag(), out);
    }

    protected void serializeBlockEntities(BlockEntity[] blockEntities, ByteArrayDataOutput out) throws IOException {
        serializeCompoundList(blockEntities, out, TILE_ENTITIES);
    }

    protected void serializeEntities(Entity[] entities, ByteArrayDataOutput out) throws IOException {
        serializeCompoundList(entities, out, ENTITIES);
    }
    //</editor-fold>

    //<editor-fold desc="Deserializers" defaultstate="collapsed">
    @Override
    public World deserialize(ByteArrayDataInput in) throws IOException {
        int worldVersion = in.readInt();

        Chunk[] chunks = deserializeCompressed(this::deserializeChunks, in);
        CompoundBinaryTag extraData = deserializeCompressed((length, dataIn) -> this.deserializeCompound(length, dataIn), in);

        return loader.deserializers().world().deserialize(worldVersion, chunks, extraData);
    }

    protected Chunk[] deserializeChunks(ByteArrayDataInput in) throws IOException {
        int length = in.readInt();

        Chunk[] chunks = new Chunk[length];
        for (int i = 0; i < length; i++) {
            chunks[i] = deserializeChunk(in);
        }

        return chunks;
    }

    protected Chunk deserializeChunk(ByteArrayDataInput in) throws IOException {
        int x = in.readInt();
        int z = in.readInt();

        Section[] sections = deserializeSections(in);
        Heightmaps heightMaps = deserializeHeightMaps(in);
        BlockEntity[] blockEntities = deserializeBlockEntities(in);
        Entity[] entities = deserializeEntities(in);
        CompoundBinaryTag extraData = deserializeCompound(in);

        return loader.deserializers().chunk().deserialize(x, z, sections, heightMaps, blockEntities, entities, extraData);
    }

    protected Section[] deserializeSections(ByteArrayDataInput in) throws IOException {
        int length = in.readInt();

        Section[] sections = new Section[length];
        for (int i = 0; i < length; i++) {
            sections[i] = deserializeSection(in);
        }

        return sections;
    }

    protected Section deserializeSection(ByteArrayDataInput in) throws IOException {
        byte[] blockLight = deserializeLightData(in);
        byte[] skyLight = deserializeLightData(in);
        BlockStates blockStates = deserializeBlockStates(in);
        Biomes biomes = deserializeBiomes(in);

        return loader.deserializers().section().deserialize(blockLight, skyLight, blockStates, biomes);
    }

    protected byte @Nullable [] deserializeLightData(ByteArrayDataInput in) {
        boolean hasLightData = in.readBoolean();
        if (!hasLightData) {
            return null;
        }

        byte[] bytes = new byte[Section.LIGHT_ARRAY_LENGTH];
        in.readFully(bytes);

        return bytes;
    }

    protected BlockStates deserializeBlockStates(ByteArrayDataInput in) throws IOException {
        return loader.deserializers().blockStates().deserialize(deserializeCompound(in));
    }

    protected Biomes deserializeBiomes(ByteArrayDataInput in) throws IOException {
        return loader.deserializers().biomes().deserialize(deserializeCompound(in));
    }

    protected Heightmaps deserializeHeightMaps(ByteArrayDataInput in) throws IOException {
        return loader.deserializers().heightmaps().deserialize(deserializeCompound(in));
    }

    protected BlockEntity[] deserializeBlockEntities(ByteArrayDataInput in) throws IOException {
        return deserializeCompoundList(BlockEntity[]::new, in, TILE_ENTITIES, loader.deserializers().blockEntity()::deserialize);
    }

    protected Entity[] deserializeEntities(ByteArrayDataInput in) throws IOException {
        return deserializeCompoundList(Entity[]::new, in, ENTITIES, loader.deserializers().entity()::deserialize);
    }
    //</editor-fold>
}
