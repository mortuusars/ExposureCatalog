package io.github.mortuusars.exposure_catalog.gui.screen;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.mortuusars.exposure_catalog.ExposureCatalog;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public class ConfirmScreen extends Screen {
    public static final ResourceLocation TEXTURE = ExposureCatalog.resource("textures/gui/confirm.png");

    protected final Screen parent;
    protected final Component message;
    protected final Component yesButtonMsg;
    protected final Button.OnPress onYesButtonPress;
    protected final Component noButtonMsg;
    protected final Button.OnPress onNoButtonPress;

    protected int imageWidth;
    protected int imageHeight;
    protected int leftPos;
    protected int topPos;
    private Button yesButton;
    private Button noButton;

    public ConfirmScreen(Screen parent, Component message, Component yesButtonMsg, Button.OnPress onYesButtonPress,
                         Component noButtonMsg, Button.OnPress onNoButtonPress) {
        super(Component.empty());
        this.parent = parent;
        this.message = message;
        this.yesButtonMsg = yesButtonMsg;
        this.onYesButtonPress = onYesButtonPress;
        this.noButtonMsg = noButtonMsg;
        this.onNoButtonPress = onNoButtonPress;
    }

    @Override
    protected void init() {
        this.imageWidth = 240;
        this.imageHeight = 88;
        this.leftPos = width / 2 - imageWidth / 2;
        this.topPos = height / 2 - imageHeight / 2;

        yesButton = Button.builder(yesButtonMsg, button -> {
                    onClose();
                    onYesButtonPress.onPress(button);
                })
                .bounds(leftPos + 9, topPos + 60, 108, 19)
                .build();
        addRenderableWidget(yesButton);

        noButton = Button.builder(noButtonMsg, button -> {
                    onClose();
                    onNoButtonPress.onPress(button);
                })
                .bounds(leftPos + 123, topPos + 60, 108, 19)
                .build();
        addRenderableWidget(noButton);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);

        // Main texture
        guiGraphics.blit(TEXTURE, leftPos, topPos, imageWidth, imageHeight, 0, 0,
                imageWidth, imageHeight, 256, 256);

        super.render(guiGraphics, mouseX, mouseY, partialTick);

        List<FormattedCharSequence> messageLines = font.split(message, 221);
        int messageY = 28 - ((messageLines.size() * font.lineHeight) / 2);
        for (int i = 0; i < messageLines.size(); i++) {
            FormattedCharSequence messageLine = messageLines.get(i);
            guiGraphics.drawCenteredString(font, messageLine, leftPos + 120, topPos + messageY + i * font.lineHeight, 0xFFFFFFFF);
        }
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (getFocused() == null && keyCode == InputConstants.KEY_RETURN) {
            yesButton.onPress();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
