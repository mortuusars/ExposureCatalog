package io.github.mortuusars.exposure_catalog.gui;

import io.github.mortuusars.exposure_catalog.gui.screen.CatalogScreen;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum Order implements StringRepresentable {
    ASCENDING("ascending"), DESCENDING("descending");

    private final String name;

    Order(String name) {
        this.name = name;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name;
    }

    public static Order fromSerializedString(String str) {
        for (Order value : values()) {
            if (value.getSerializedName().equals(str))
                return value;
        }
        throw new IllegalArgumentException(str + " cannot be deserialized to Order");
    }

}
