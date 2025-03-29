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

        writeCompressed(world.chunks(), this::serializeChunks, out);
        writeCompressed(world.tag(), this::writeRawCompound, out);
    }

    protected void serializeChunks(Chunk[] chunks, ByteArrayDataOutput out) throws IOException {
        writeArray(chunks, out, this::serializeChunk);
    }

    protected void serializeChunk(Chunk chunk, ByteArrayDataOutput out) throws IOException {
        out.writeInt(chunk.x());
        out.writeInt(chunk.z());

        serializeSections(chunk.sections(), out);
        serializeHeightmaps(chunk.heightmaps(), out);
        serializeBlockEntities(chunk.blockEntities(), out);
        serializeEntities(chunk.entities(), out);
        writeCompound(chunk.tag(), out);
    }

    protected void serializeSections(Section[] sections, ByteArrayDataOutput out) throws IOException {
        writeArray(sections, out, this::serializeSection);
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
        writeCompound(blockStates.tag(), out);
    }

    protected void serializeBiomes(Biomes biomes, ByteArrayDataOutput out) throws IOException {
        writeCompound(biomes.tag(), out);
    }

    protected void serializeHeightmaps(Heightmaps heightmaps, ByteArrayDataOutput out) throws IOException {
        writeCompound(heightmaps.tag(), out);
    }

    protected void serializeBlockEntities(BlockEntity[] blockEntities, ByteArrayDataOutput out) throws IOException {
        writeCompoundArray(blockEntities, out, TILE_ENTITIES);
    }

    protected void serializeEntities(Entity[] entities, ByteArrayDataOutput out) throws IOException {
        writeCompoundArray(entities, out, ENTITIES);
    }
    //</editor-fold>

    //<editor-fold desc="Deserializers" defaultstate="collapsed">
    @Override
    public World deserialize(ByteArrayDataInput in) throws IOException {
        int worldVersion = in.readInt();

        Chunk[] chunks = readCompressed(this::deserializeChunks, in);
        CompoundBinaryTag extraData = readCompressed(this::readRawCompound, in);

        return loader.deserializers().world().deserialize(worldVersion, chunks, extraData);
    }

    protected Chunk[] deserializeChunks(ByteArrayDataInput in) throws IOException {
        return readArray(Chunk[]::new, in, this::deserializeChunk);
    }

    protected Chunk deserializeChunk(ByteArrayDataInput in) throws IOException {
        int x = in.readInt();
        int z = in.readInt();

        Section[] sections = deserializeSections(in);
        Heightmaps heightmaps = deserializeHeightmaps(in);
        BlockEntity[] blockEntities = deserializeBlockEntities(in);
        Entity[] entities = deserializeEntities(in);
        CompoundBinaryTag extraData = readCompound(in);

        return loader.deserializers().chunk().deserialize(x, z, sections, heightmaps, blockEntities, entities, extraData);
    }

    protected Section[] deserializeSections(ByteArrayDataInput in) throws IOException {
        return readArray(Section[]::new, in, this::deserializeSection);
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
        return loader.deserializers().blockStates().deserialize(readCompound(in));
    }

    protected Biomes deserializeBiomes(ByteArrayDataInput in) throws IOException {
        return loader.deserializers().biomes().deserialize(readCompound(in));
    }

    protected Heightmaps deserializeHeightmaps(ByteArrayDataInput in) throws IOException {
        return loader.deserializers().heightmaps().deserialize(readCompound(in));
    }

    protected BlockEntity[] deserializeBlockEntities(ByteArrayDataInput in) throws IOException {
        return readCompoundArray(BlockEntity[]::new, in, TILE_ENTITIES, loader.deserializers().blockEntity()::deserialize);
    }

    protected Entity[] deserializeEntities(ByteArrayDataInput in) throws IOException {
        return readCompoundArray(Entity[]::new, in, ENTITIES, loader.deserializers().entity()::deserialize);
    }
    //</editor-fold>
}
