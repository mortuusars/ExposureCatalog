package io.github.mortuusars.exposure_catalog.client.gui;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum Mode implements StringRepresentable {
    EXPOSURES("exposures"), TEXTURES("textures");

    private final String name;

    Mode(String name) {
        this.name = name;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name;
    }

    public static Mode fromSerializedString(String str) {
        for (Mode value : values()) {
            if (value.getSerializedName().equals(str))
                return value;
        }
        throw new IllegalArgumentException(str + " cannot be deserialized to Mode");
    }
}
