package io.github.mortuusars.exposure_catalog.gui.screen.widget;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class EnumButton<T extends Enum<T>> extends ImageButton {
    private final OnStateChanged<T> onStateChanged;
    private final List<T> states;
    private final int xDiffTex;

    private int currentStateIndex;
    private @Nullable Function<T, Tooltip> tooltipFunc;
    private @Nullable Tooltip defaultTooltip;

    public EnumButton(Class<T> enumClass, int x, int y, int width, int height, int xTexStart, int yTexStart,
                      int xDiffTex, int yDiffTex, ResourceLocation textureLocation, int textureWidth, int textureHeight,
                      OnStateChanged<T> onStateChanged, Component message) {
        super(x, y, width, height, xTexStart, yTexStart, yDiffTex, textureLocation, textureWidth, textureHeight, b -> {}, message);
        this.onStateChanged = onStateChanged;
        this.states = Arrays.asList(enumClass.getEnumConstants());
        this.currentStateIndex = 0;
        this.xDiffTex = xDiffTex;
    }

    public T getState() {
        return states.get(currentStateIndex);
    }

    public void setState(T state) {
        currentStateIndex = state.ordinal();
    }

    public void setStateIndex(int index) {
        Preconditions.checkElementIndex(index, states.size());
        currentStateIndex = index;
    }

    public void previousState() {
        currentStateIndex = (currentStateIndex - 1 + states.size()) % states.size();
    }

    public void nextState() {
        currentStateIndex = (currentStateIndex + 1) % states.size();
    }

    public void setDefaultTooltip(@Nullable Tooltip tooltip) {
        this.defaultTooltip = tooltip;
    }

    public void setTooltipFunc(@Nullable Function<T, Tooltip> tooltipFunc) {
        this.tooltipFunc = tooltipFunc;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (isHoveredOrFocused())
            setTooltip(tooltipFunc != null ? tooltipFunc.apply(getState()) : defaultTooltip);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void renderTexture(GuiGraphics guiGraphics, ResourceLocation texture, int x, int y, int uOffset, int vOffset, int yDiffTex, int width, int height, int textureWidth, int textureHeight) {
        int xTex = uOffset + (xDiffTex * currentStateIndex);
        int yTex = vOffset;
        if (!this.isActive()) {
            yTex = vOffset + yDiffTex * 2;
        } else if (this.isHoveredOrFocused()) {
            yTex = vOffset + yDiffTex;
        }

        RenderSystem.enableDepthTest();
        guiGraphics.blit(texture, x, y, xTex, yTex, width, height, textureWidth, textureHeight);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isActive() && clicked(mouseX, mouseY)) {
            int prevIndex = currentStateIndex;

            if (button == InputConstants.MOUSE_BUTTON_RIGHT)
                previousState();
            else
                nextState();

            playDownSound(Minecraft.getInstance().getSoundManager());
            onStateChanged.onStateChanged(this, states.get(prevIndex), states.get(currentStateIndex));
            onClick(mouseX, mouseY);
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (isActive() && clicked(mouseX, mouseY)) {
            int prevIndex = currentStateIndex;

            if (delta < 0)
                previousState();
            else
                nextState();

            playDownSound(Minecraft.getInstance().getSoundManager());
            onStateChanged.onStateChanged(this, states.get(prevIndex), states.get(currentStateIndex));
            return true;
        }

        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (isActive() && CommonInputs.selected(keyCode)) {
            int prevIndex = currentStateIndex;

            if (Screen.hasShiftDown())
                previousState();
            else
                nextState();

            playDownSound(Minecraft.getInstance().getSoundManager());
            onStateChanged.onStateChanged(this, states.get(prevIndex), states.get(currentStateIndex));
            onPress();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public interface OnStateChanged<T extends Enum<T>> {
        void onStateChanged(Button button, T previousState, T newState);
    }
}
