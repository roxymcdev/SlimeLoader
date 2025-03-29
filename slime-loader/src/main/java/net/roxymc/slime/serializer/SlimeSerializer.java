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
import net.roxymc.slime.util.function.IOBiConsumer;
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
                "Serializers below version 12 (%s) are not supported. See: https://github.com/roxymc-net/SlimeLoader#legacy-slime-versions",
                version
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

    protected void writeCompressed(byte[] bytes, ByteArrayDataOutput out) {
        byte[] compressed = Zstd.compress(bytes);

        out.writeInt(compressed.length);
        out.writeInt(bytes.length);

        out.write(compressed);
    }

    protected void writeCompressed(IOConsumer<ByteArrayDataOutput> consumer, ByteArrayDataOutput out) throws IOException {
        ByteArrayDataOutput output = ByteStreams.newDataOutput();
        consumer.accept(output);

        writeCompressed(output.toByteArray(), out);
    }

    protected <T> void writeCompressed(T value, IOBiConsumer<T, ByteArrayDataOutput> consumer, ByteArrayDataOutput out) throws IOException {
        writeCompressed(output -> consumer.accept(value, output), out);
    }

    protected <T> void writeArray(T[] array, ByteArrayDataOutput out, IOBiConsumer<T, ByteArrayDataOutput> serializer) throws IOException {
        out.writeInt(array.length);

        for (T element : array) {
            serializer.accept(element, out);
        }
    }

    protected void writeCompound(CompoundBinaryTag tag, ByteArrayDataOutput out) throws IOException {
        ByteArrayDataOutput output = ByteStreams.newDataOutput();
        writeRawCompound(tag, output);

        byte[] bytes = output.toByteArray();

        out.writeInt(bytes.length);
        out.write(bytes);
    }

    protected void writeRawCompound(CompoundBinaryTag tag, ByteArrayDataOutput out) throws IOException {
        if (!tag.isEmpty()) {
            BinaryTagIO.writer().write(tag, out);
        }
    }

    protected <T extends CompoundBinaryTagHolder> void writeCompoundArray(T[] array, ByteArrayDataOutput out, String key) throws IOException {
        ListBinaryTag.Builder<CompoundBinaryTag> builder = ListBinaryTag.builder(BinaryTagTypes.COMPOUND);

        for (T element : array) {
            builder.add(element.tag());
        }

        CompoundBinaryTag tag = CompoundBinaryTag.builder()
                .put(key, builder.build())
                .build();

        writeCompound(tag, out);
    }

    public abstract World deserialize(ByteArrayDataInput in) throws IOException;

    protected byte[] readCompressed(ByteArrayDataInput in) {
        int compressedLength = in.readInt();
        int length = in.readInt();

        byte[] compressed = new byte[compressedLength];
        in.readFully(compressed);

        return Zstd.decompress(compressed, length);
    }

    protected <T> T readCompressed(IOFunction<ByteArrayDataInput, T> function, ByteArrayDataInput in) throws IOException {
        return readCompressed((length, input) -> function.apply(input), in);
    }

    protected <T> T readCompressed(IOBiFunction<Integer, ByteArrayDataInput, T> function, ByteArrayDataInput in) throws IOException {
        byte[] bytes = readCompressed(in);

        return function.apply(bytes.length, ByteStreams.newDataInput(bytes));
    }

    protected <T> T[] readArray(
            IntFunction<T[]> arrayBuilder, ByteArrayDataInput in, IOFunction<ByteArrayDataInput, T> deserializer
    ) throws IOException {
        int length = in.readInt();

        T[] array = arrayBuilder.apply(length);
        for (int i = 0; i < length; i++) {
            array[i] = deserializer.apply(in);
        }

        return array;
    }

    protected CompoundBinaryTag readCompound(ByteArrayDataInput in) throws IOException {
        int length = in.readInt();

        return readRawCompound(length, in);
    }

    protected CompoundBinaryTag readRawCompound(int length, ByteArrayDataInput in) throws IOException {
        return length == 0 ? CompoundBinaryTag.empty() : BinaryTagIO.reader().read(in);
    }

    protected <T extends CompoundBinaryTagHolder> T[] readCompoundArray(
            IntFunction<T[]> arrayBuilder, ByteArrayDataInput in, String key, Function<CompoundBinaryTag, T> deserializer
    ) throws IOException {
        CompoundBinaryTag tag = readCompound(in);
        ListBinaryTag list = tag.getList(key, BinaryTagTypes.COMPOUND);

        T[] array = arrayBuilder.apply(list.size());
        for (int i = 0; i < array.length; i++) {
            array[i] = deserializer.apply(list.getCompound(i));
        }

        return array;
    }
}
