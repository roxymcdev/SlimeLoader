package net.roxymc.slime.util.function;

import java.io.IOException;

@FunctionalInterface
public interface IOBiConsumer<T, U> {
    void accept(T t, U u) throws IOException;
}
