package io.github.mortuusars.exposure_catalogue.gui.screen;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public abstract class OverlayScreen extends Screen {
    private final Screen parent;

    public OverlayScreen(Screen parent, Component title) {
        super(title);
        this.parent = parent;
    }

    public Screen getParent() {
        return parent;
    }
}
