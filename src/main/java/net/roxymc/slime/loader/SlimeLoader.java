package net.roxymc.slime.loader;

import com.google.common.base.Preconditions;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.roxymc.slime.serializer.SlimeSerializer;
import net.roxymc.slime.world.World;

import java.io.IOException;
import java.util.Arrays;
import java.util.function.UnaryOperator;

import static net.roxymc.slime.util.ObjectUtils.nonNull;

public class SlimeLoader {
    public static final byte[] SLIME_HEADER = new byte[]{-79, 11};

    private final Deserializers deserializers;

    private SlimeLoader(Builder builder) {
        this.deserializers = nonNull(builder.deserializers, "deserializers");
    }

    public static Builder builder() {
        return new Builder();
    }

    public static boolean isSlimeWorld(ByteArrayDataInput in) {
        try {
            byte[] header = new byte[SLIME_HEADER.length];
            in.readFully(header);

            return Arrays.equals(SLIME_HEADER, header);
        } catch (Exception e) {
            return false;
        }
    }

    public Deserializers deserializers() {
        return deserializers;
    }

    public World load(byte[] bytes) throws IOException {
        ByteArrayDataInput in = ByteStreams.newDataInput(bytes);

        Preconditions.checkArgument(isSlimeWorld(in), "bytes are not a slime world");

        int serializerVersion = in.readUnsignedByte();

        return SlimeSerializer.forVersion(this, serializerVersion).deserialize(in);
    }

    public byte[] save(World world) throws IOException {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        out.write(SLIME_HEADER);

        SlimeSerializer.forVersion(this, SlimeSerializer.LATEST_VERSION).serialize(world, out);

        return out.toByteArray();
    }

    @SuppressWarnings("NotNullFieldNotInitialized") // it's a builder
    public static class Builder {
        private Deserializers deserializers;

        public Builder deserializers(UnaryOperator<Deserializers.Builder> builder) {
            this.deserializers = builder.apply(new Deserializers.Builder()).build();
            return this;
        }

        public SlimeLoader build() {
            return new SlimeLoader(this);
        }
    }
}
