package io.github.mortuusars.exposure_catalog.gui;

import io.github.mortuusars.exposure_catalog.gui.screen.CatalogScreen;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum Sorting implements StringRepresentable {
    ALPHABETICAL("alphabetical"), DATE("date");

    private final String name;

    Sorting(String name) {
        this.name = name;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name;
    }

    public static Sorting fromSerializedString(String str) {
        for (Sorting value : values()) {
            if (value.getSerializedName().equals(str))
                return value;
        }
        throw new IllegalArgumentException(str + " cannot be deserialized to Sorting");
    }

}
