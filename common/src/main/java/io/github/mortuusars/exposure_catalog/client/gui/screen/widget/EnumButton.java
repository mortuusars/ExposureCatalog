package io.github.mortuusars.exposure_catalog.client.gui.screen.widget;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.mojang.blaze3d.platform.InputConstants;
import io.github.mortuusars.exposure.client.gui.Widgets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnumButton<T extends Enum<T>> extends Button {
    protected final List<T> states;
    protected final Map<T, WidgetSprites> sprites;
    protected final OnStateChanged<T> onStateChanged;

    protected int currentStateIndex;
    protected @Nullable Function<T, Tooltip> tooltipFunc;
    protected @Nullable Tooltip defaultTooltip;

    public EnumButton(Class<T> enumClass, int x, int y, int width, int height, ResourceLocation sprite,
                      OnStateChanged<T> onStateChanged, Component message) {
        super(x, y, width, height, message, b -> {}, n -> Component.empty());
        this.states = Arrays.asList(enumClass.getEnumConstants());
        this.sprites = new HashMap<>();
        this.onStateChanged = onStateChanged;
        this.currentStateIndex = 0;

        for (T state : states) {
            String name = state.name().toLowerCase();
            WidgetSprites sprites = Widgets.threeStateSprites(sprite.withSuffix("_" + name));
            this.sprites.put(state, sprites);
        }
    }

    public T getState() {
        return states.get(currentStateIndex);
    }

    public void setState(T state) {
        setStateIndex(state.ordinal());
    }

    public void setStateIndex(int index) {
        Preconditions.checkElementIndex(index, states.size());
        currentStateIndex = index;
    }

    public void changeState(T state) {
        T previousState = getState();
        if (!previousState.equals(state)) {
            setState(state);
            onStateChanged.onStateChanged(this, previousState, state);
        }
    }

    public void changeStateIndex(int index) {
        int previousIndex = currentStateIndex;
        if (previousIndex != index) {
            setStateIndex(index);
            onStateChanged.onStateChanged(this, states.get(previousIndex), states.get(index));
        }
    }

    public void previousState() {
        changeStateIndex((currentStateIndex - 1 + states.size()) % states.size());

    }

    public void nextState() {
        changeStateIndex((currentStateIndex + 1) % states.size());
    }

    public void setDefaultTooltip(@Nullable Tooltip tooltip) {
        this.defaultTooltip = tooltip;
    }

    public void setTooltipFunc(@Nullable Function<T, Tooltip> tooltipFunc) {
        this.tooltipFunc = tooltipFunc;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (isHoveredOrFocused()) {
            setTooltip(tooltipFunc != null ? tooltipFunc.apply(getState()) : defaultTooltip);
        }

        WidgetSprites sprites = this.sprites.get(getState());
        ResourceLocation resourceLocation = sprites.get(isActive(), isHoveredOrFocused());
        guiGraphics.blitSprite(resourceLocation, this.getX(), this.getY(), this.width, this.height);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isActive() && clicked(mouseX, mouseY)) {
            if (button == InputConstants.MOUSE_BUTTON_RIGHT) {
                previousState();
            } else {
                nextState();
            }

            playDownSound(Minecraft.getInstance().getSoundManager());
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (isActive() && clicked(mouseX, mouseY)) {
            if (scrollY < 0) {
                previousState();
            } else {
                nextState();
            }

            playDownSound(Minecraft.getInstance().getSoundManager());
            return true;
        }

        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (isActive() && CommonInputs.selected(keyCode)) {
            if (Screen.hasShiftDown()) {
                previousState();
            } else {
                nextState();
            }

            playDownSound(Minecraft.getInstance().getSoundManager());
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public interface OnStateChanged<T extends Enum<T>> {
        void onStateChanged(EnumButton<T> button, T previousState, T newState);
    }
}