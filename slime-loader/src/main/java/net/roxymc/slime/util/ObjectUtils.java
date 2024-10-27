package net.roxymc.slime.util;

import java.util.Objects;

public final class ObjectUtils {
    private ObjectUtils() {
    }

    public static <T> T nonNull(T obj, String name) {
        return Objects.requireNonNull(obj, name + " deserializer == null");
    }
}
