package net.roxymc.slime.loader;

import net.roxymc.slime.world.Heightmaps;
import net.roxymc.slime.world.World;
import net.roxymc.slime.world.biome.Biomes;
import net.roxymc.slime.world.block.entity.BlockEntity;
import net.roxymc.slime.world.block.state.BlockStates;
import net.roxymc.slime.world.chunk.Chunk;
import net.roxymc.slime.world.chunk.Section;
import net.roxymc.slime.world.entity.Entity;

import static net.roxymc.slime.util.ObjectUtils.nonNull;

public final class Deserializers {
    private final Biomes.Deserializer biomes;
    private final BlockEntity.Deserializer blockEntity;
    private final BlockStates.Deserializer blockStates;
    private final Chunk.Deserializer chunk;
    private final Section.Deserializer section;
    private final Entity.Deserializer entity;
    private final Heightmaps.Deserializer heightmaps;
    private final World.Deserializer world;

    private Deserializers(Builder builder) {
        this.biomes = nonNull(builder.biomes, "biomes");
        this.blockEntity = nonNull(builder.blockEntity, "blockEntity");
        this.blockStates = nonNull(builder.blockStates, "blockStates");
        this.chunk = nonNull(builder.chunk, "chunk");
        this.section = nonNull(builder.section, "section");
        this.entity = nonNull(builder.entity, "entity");
        this.heightmaps = nonNull(builder.heightmaps, "heightmaps");
        this.world = nonNull(builder.world, "world");
    }

    public Biomes.Deserializer biomes() {
        return biomes;
    }

    public BlockEntity.Deserializer blockEntity() {
        return blockEntity;
    }

    public BlockStates.Deserializer blockStates() {
        return blockStates;
    }

    public Chunk.Deserializer chunk() {
        return chunk;
    }

    public Section.Deserializer section() {
        return section;
    }

    public Entity.Deserializer entity() {
        return entity;
    }

    public Heightmaps.Deserializer heightmaps() {
        return heightmaps;
    }

    public World.Deserializer world() {
        return world;
    }

    @SuppressWarnings("NotNullFieldNotInitialized") // it's a builder
    public static final class Builder {
        private Biomes.Deserializer biomes;
        private BlockEntity.Deserializer blockEntity;
        private BlockStates.Deserializer blockStates;
        private Chunk.Deserializer chunk;
        private Section.Deserializer section;
        private Entity.Deserializer entity;
        private Heightmaps.Deserializer heightmaps;
        private World.Deserializer world;

        Builder() {
        }

        public Builder biomes(Biomes.Deserializer biomes) {
            this.biomes = biomes;
            return this;
        }

        public Builder blockEntity(BlockEntity.Deserializer blockEntity) {
            this.blockEntity = blockEntity;
            return this;
        }

        public Builder blockStates(BlockStates.Deserializer blockStates) {
            this.blockStates = blockStates;
            return this;
        }

        public Builder chunk(Chunk.Deserializer chunk) {
            this.chunk = chunk;
            return this;
        }

        public Builder section(Section.Deserializer section) {
            this.section = section;
            return this;
        }

        public Builder entity(Entity.Deserializer entity) {
            this.entity = entity;
            return this;
        }

        public Builder heightmaps(Heightmaps.Deserializer heightmaps) {
            this.heightmaps = heightmaps;
            return this;
        }

        public Builder world(World.Deserializer world) {
            this.world = world;
            return this;
        }

        Deserializers build() {
            return new Deserializers(this);
        }
    }
}
