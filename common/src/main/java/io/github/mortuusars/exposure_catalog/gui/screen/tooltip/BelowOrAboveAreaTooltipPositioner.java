package io.github.mortuusars.exposure_catalog.gui.screen.tooltip;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.renderer.Rect2i;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;
import org.joml.Vector2ic;

public class BelowOrAboveAreaTooltipPositioner implements ClientTooltipPositioner {
    private final Rect2i area;

    public BelowOrAboveAreaTooltipPositioner(Rect2i area) {
        this.area = area;
    }

    @Override
    public @NotNull Vector2ic positionTooltip(int screenWidth, int screenHeight, int mouseX, int mouseY, int tooltipWidth, int tooltipHeight) {
        Vector2i vector2i = new Vector2i();
        vector2i.x = this.area.getX() + 8;
        vector2i.y = this.area.getY() + this.area.getHeight() + 8 + 1;
        if (vector2i.y + tooltipHeight + 3 > screenHeight) {
            vector2i.y = this.area.getY() - tooltipHeight - 8 - 1;
        }
        if (vector2i.x + tooltipWidth > screenWidth) {
            vector2i.x = Math.max(this.area.getX() + this.area.getWidth() - tooltipWidth - 8, 4);
        }
        return vector2i;
    }
}