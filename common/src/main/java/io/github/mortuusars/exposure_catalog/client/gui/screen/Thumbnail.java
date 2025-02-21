package io.github.mortuusars.exposure_catalog.client.gui.screen;

import com.mojang.datafixers.util.Either;
import io.github.mortuusars.exposure.world.level.storage.ExposureIdentifier;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;

public record Thumbnail(int index, int gridIndex, ExposureIdentifier identifier, Rect2i area,
                        boolean selected) {
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= area.getX() && mouseX < area.getX() + area.getWidth()
                && mouseY >= area.getY() && mouseY < area.getY() + area.getHeight();
    }
}
