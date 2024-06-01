package io.github.mortuusars.exposure_catalog.gui;

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

    public static io.github.mortuusars.exposure_catalog.gui.Mode fromSerializedString(String str) {
        for (io.github.mortuusars.exposure_catalog.gui.Mode value : values()) {
            if (value.getSerializedName().equals(str))
                return value;
        }
        throw new IllegalArgumentException(str + " cannot be deserialized to Mode");
    }
}
