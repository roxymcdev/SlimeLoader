package net.roxymc.slime.serializer;

import com.github.luben.zstd.Zstd;
import com.google.common.base.Preconditions;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.kyori.adventure.nbt.BinaryTagIO;
import net.kyori.adventure.nbt.BinaryTagTypes;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.roxymc.slime.CompoundBinaryTagHolder;
import net.roxymc.slime.loader.SlimeLoader;
import net.roxymc.slime.util.function.IOBiFunction;
import net.roxymc.slime.util.function.IOConsumer;
import net.roxymc.slime.util.function.IOFunction;
import net.roxymc.slime.world.World;

import java.io.IOException;
import java.util.function.Function;
import java.util.function.IntFunction;

public abstract class SlimeSerializer {
    public static final int LATEST_VERSION = 12;

    protected final SlimeLoader loader;

    public SlimeSerializer(SlimeLoader loader) {
        this.loader = loader;
    }

    public static SlimeSerializer forVersion(SlimeLoader loader, int version) {
        Preconditions.checkArgument(
                version >= 12,
                String.format(
                        "Serializers below version 12 (%s) are not supported. See: https://github.com/roxymc-net/SlimeLoader#legacy-slime-versions",
                        version
                )
        );

        return switch (version) {
            case 12 -> new SlimeSerializer_v12(loader);
            default -> throw new IllegalArgumentException("Unsupported version: " + version);
        };
    }

    public abstract byte version();

    public void serialize(World world, ByteArrayDataOutput out) throws IOException {
        out.writeByte(version());
        out.writeInt(world.version());
    }

    protected void serializeCompressed(byte[] bytes, ByteArrayDataOutput out) {
        byte[] compressed = Zstd.compress(bytes);

        out.writeInt(compressed.length);
        out.writeInt(bytes.length);

        out.write(compressed);
    }

    protected void serializeCompressed(IOConsumer<ByteArrayDataOutput> consumer, ByteArrayDataOutput out) throws IOException {
        ByteArrayDataOutput tempOut = ByteStreams.newDataOutput();
        consumer.accept(tempOut);

        serializeCompressed(tempOut.toByteArray(), out);
    }

    protected void serializeCompound(CompoundBinaryTag tag, ByteArrayDataOutput out) throws IOException {
        serializeCompound(tag, out, true);
    }

    protected void serializeCompound(CompoundBinaryTag tag, ByteArrayDataOutput out, boolean writeLength) throws IOException {
        ByteArrayDataOutput tagOut = ByteStreams.newDataOutput();
        BinaryTagIO.writer().write(tag, tagOut);

        byte[] bytes = tagOut.toByteArray();

        if (writeLength) {
            out.writeInt(bytes.length);
        }

        out.write(bytes);
    }

    protected <T extends CompoundBinaryTagHolder> void serializeCompoundList(T[] array, ByteArrayDataOutput out, String key) throws IOException {
        ListBinaryTag.Builder<CompoundBinaryTag> builder = ListBinaryTag.builder(BinaryTagTypes.COMPOUND);
        for (T element : array) {
            builder.add(element.tag());
        }

        CompoundBinaryTag tag = CompoundBinaryTag.builder()
                .put(key, builder.build())
                .build();

        serializeCompound(tag, out);
    }

    public abstract World deserialize(ByteArrayDataInput in) throws IOException;

    protected byte[] deserializeCompressed(ByteArrayDataInput in) {
        int compressedLength = in.readInt();
        int length = in.readInt();

        byte[] compressed = new byte[compressedLength];
        in.readFully(compressed);

        return Zstd.decompress(compressed, length);
    }

    protected <T> T deserializeCompressed(IOFunction<ByteArrayDataInput, T> function, ByteArrayDataInput in) throws IOException {
        return deserializeCompressed((length, dataIn) -> function.apply(dataIn), in);
    }

    protected <T> T deserializeCompressed(IOBiFunction<Integer, ByteArrayDataInput, T> function, ByteArrayDataInput in) throws IOException {
        byte[] bytes = deserializeCompressed(in);

        return function.apply(bytes.length, ByteStreams.newDataInput(bytes));
    }

    protected CompoundBinaryTag deserializeCompound(ByteArrayDataInput in) throws IOException {
        int length = in.readInt();

        return deserializeCompound(length, in);
    }

    protected CompoundBinaryTag deserializeCompound(int length, ByteArrayDataInput in) throws IOException {
        byte[] bytes = new byte[length];
        in.readFully(bytes);

        return BinaryTagIO.reader().read(ByteStreams.newDataInput(bytes));
    }

    protected <T extends CompoundBinaryTagHolder> T[] deserializeCompoundList(
            IntFunction<T[]> arrayBuilder, ByteArrayDataInput in, String key, Function<CompoundBinaryTag, T> deserializer
    ) throws IOException {
        CompoundBinaryTag tag = deserializeCompound(in);
        ListBinaryTag list = tag.getList(key, BinaryTagTypes.COMPOUND);

        T[] array = arrayBuilder.apply(list.size());
        for (int i = 0; i < array.length; i++) {
            array[i] = deserializer.apply(list.getCompound(i));
        }

        return array;
    }
}
