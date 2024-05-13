package io.github.mortuusars.exposure_catalogue.gui.screen;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.datafixers.util.Either;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.camera.infrastructure.FrameData;
import io.github.mortuusars.exposure.data.storage.ExposureSavedData;
import io.github.mortuusars.exposure.gui.screen.PhotographScreen;
import io.github.mortuusars.exposure.gui.screen.album.AlbumPhotographScreen;
import io.github.mortuusars.exposure.item.PhotographItem;
import io.github.mortuusars.exposure.render.modifiers.ExposurePixelModifiers;
import io.github.mortuusars.exposure.util.ItemAndStack;
import io.github.mortuusars.exposure_catalogue.ExposureCatalogue;
import io.github.mortuusars.exposure_catalogue.network.Packets;
import io.github.mortuusars.exposure_catalogue.network.packet.server.QueryAllExposureIdsC2SP;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class CatalogueScreen extends Screen {
    public static final ResourceLocation TEXTURE = ExposureCatalogue.resource("textures/gui/catalogue.png");

    private int imageWidth;
    private int imageHeight;
    private int leftPos;
    private int topPos;

    private List<String> ids = Collections.emptyList();
    private int firstRow = 0;

    public CatalogueScreen() {
        super(Component.translatable("gui.exposure_catalogue.catalogue"));

        Packets.sendToServer(new QueryAllExposureIdsC2SP());
    }

    public void setExposureIds(@NotNull List<String> ids) {
        Preconditions.checkNotNull(ids);

        ids.sort(new Comparator<String>() {
            public int compare(String o1, String o2) {
                return extractTick(o1) - extractTick(o2);
            }

            int extractTick(String s) {
                s = s.replace("_chromatic", "");
                int first = s.indexOf('_');

                String secondPart = first == -1 ? s : s.substring(first + 1);
                int next = secondPart.indexOf('_');
                int endIndex = next != -1 ? next : secondPart.length() - 1;
                String num = secondPart.substring(0, endIndex).replaceAll("\\D", "");
                // return 0 if no digits found
                return num.isEmpty() ? 0 : Integer.parseInt(num);
            }
        });

        this.firstRow = 0;

        this.ids = ids;
    }

    @Override
    protected void init() {
        super.init();
        this.imageWidth = 246;
        this.imageHeight = 257;
        this.leftPos = width / 2 - imageWidth / 2;
        this.topPos = height / 2 - imageHeight / 2;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        // Main texture
        guiGraphics.blit(TEXTURE, leftPos, topPos, imageWidth, imageHeight, 0, 0,
                imageWidth, imageHeight, 512, 512);

        drawScrollBar(guiGraphics);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(leftPos + 13, topPos + 23, 10);

        for (int row = 0; row < 4; row++) {
            for (int column = 0; column < 4; column++) {
                int gridIndex = column + row * 4;
                int idIndex = gridIndex + this.firstRow * 4;

                if (idIndex >= ids.size())
                    break;

                guiGraphics.fill(column * 53 - 1, row * 53, column * 53 + 49, row * 53 + 50, 0xFF555555);
                guiGraphics.fill(column * 53 - 1, row * 53 - 1, column * 53 + 49, row * 53 + 49, 0xFFEFEFEF);

                MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
                ExposureClient.getExposureRenderer().render(Either.left(ids.get(idIndex)), ExposurePixelModifiers.EMPTY,
                        guiGraphics.pose(), bufferSource, column * 53, row * 53, 48, 48);
                bufferSource.endBatch();
            }
        }

        guiGraphics.pose().popPose();

        drawLabels(guiGraphics, mouseX, mouseY, partialTick);

        renderTooltip(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void renderTooltip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (mouseX > leftPos + 13 && mouseX <= leftPos + 224 && mouseY > topPos + 23 && mouseY <= topPos + 234) {
            int x = (int)mouseX - (leftPos + 13);
            int y = (int)mouseY - (topPos + 23);

            int col = x / 53;
            int row = y / 53;

            int gridIndex = col + row * 4;
            int hoverIndex = gridIndex + this.firstRow * 4;

            if (hoverIndex >= ids.size() || hoverIndex < 0)
                return;

            String exposureId = ids.get(hoverIndex);

            List<Component> lines = new ArrayList<>();
            lines.add(Component.literal(exposureId));

            ExposureClient.getExposureStorage().getOrQuery(exposureId).ifPresent(data -> {
                CompoundTag properties = data.getProperties();

                lines.add(Component.literal(data.getWidth() + "x" + data.getHeight()).withStyle(ChatFormatting.GRAY));

                if (properties.getBoolean(ExposureSavedData.WAS_PRINTED_PROPERTY))
                    lines.add(Component.literal("Printed").withStyle(ChatFormatting.GRAY));
            });

            guiGraphics.renderTooltip(font, lines, Optional.empty(), mouseX, mouseY);
        }
    }

    private void drawScrollBar(@NotNull GuiGraphics guiGraphics) {
        int scrollBarHeight = 205;
        int scrollBarStartY = 24;
        int scrollBarEndY = 228;

        int totalRows = (int) Math.ceil(ids.size() / 4f);
        int size = Mth.clamp(scrollBarHeight / Math.max(1, totalRows), 8, scrollBarHeight);
        int pos = (int)Mth.map((firstRow) / (float)Math.max(1, totalRows - 4), 0f, 1f, 0f, scrollBarHeight - size);

        guiGraphics.fill(leftPos + 231, topPos + scrollBarStartY + pos, leftPos + 238, topPos + scrollBarStartY + pos + size, 0xFFcb006e);
    }

    private void drawLabels(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Title
        guiGraphics.drawString(font, title, leftPos + 8, topPos + 6, 0xFF414141, false);

        // Count
        if (!ids.isEmpty()) {
            String countStr = Integer.toString(ids.size());
            guiGraphics.drawString(font, countStr, leftPos + imageWidth - 8 - font.width(countStr),
                    topPos + 241, 0xFFFFFFFF, true);
        }

        // Draw ids on the left
        for (int i = 0; i < Math.min(ids.size(), 16); i++) {
            guiGraphics.drawString(font, ids.get(i), 5, 5 + i * 9, 0xFFFFFFFF);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button))
            return true;

        if (button != InputConstants.MOUSE_BUTTON_LEFT)
            return false;

        if (mouseX > leftPos + 13 && mouseX <= leftPos + 224 && mouseY > topPos + 23 && mouseY <= topPos + 234) {
            int x = (int)mouseX - (leftPos + 13);
            int y = (int)mouseY - (topPos + 23);

            int col = x / 53;
            int row = y / 53;

            int gridIndex = col + row * 4;
            int hoverIndex = gridIndex + this.firstRow * 4;

            if (hoverIndex >= ids.size())
                return false;

            openPhotographView(hoverIndex);
            return true;
        }

        int scrollBarHeight = 205;
        int scrollBarStartY = 24;
        int scrollBarEndY = 228;

        int totalRows = (int) Math.ceil(ids.size() / 4f);
        int size = Mth.clamp(scrollBarHeight / Math.max(1, totalRows), 8, scrollBarHeight);
        int pos = (int)Mth.map((firstRow) / (float)Math.max(1, totalRows - 4), 0f, 1f, 0f, scrollBarHeight - size);

//        guiGraphics.fill(leftPos + 231, topPos + scrollBarStartY + pos, leftPos + 238, topPos + scrollBarStartY + pos + size, 0xFFcb006e);

        if (mouseX > leftPos + 231 && mouseX <= leftPos + 238 && mouseY > topPos + scrollBarStartY && mouseY <= topPos + scrollBarEndY) {
            int msy = (int)mouseY - (topPos + scrollBarStartY);
            if (msy > pos && msy <= pos + size) {
                this.setDragging(true);
                draggingScrollbar = true;
                dragDelta = 0;
                rowAtDragStart = firstRow;
            }

            if (mouseY > topPos + scrollBarStartY + pos + size) {
                firstRow = Math.min((int)Math.ceil(Math.max(0, ids.size() - 16) / 4f), firstRow + 1);
                return true;
            }

            if (mouseY < topPos + scrollBarStartY + pos) {
                firstRow = Math.max(0, firstRow - 1);
                return true;
            }
        }

        return false;
    }

    private boolean draggingScrollbar = false;
    private int rowAtDragStart = 0;
    private double dragDelta = 0;

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (super.mouseDragged(mouseX, mouseY, button, dragX, dragY))
            return true;

        if (!draggingScrollbar || button != InputConstants.MOUSE_BUTTON_LEFT)
            return false;

        dragDelta += dragY;

        int scrollBarHeight = 205;
        int scrollBarStartY = 24;
        int scrollBarEndY = 228;

        int totalRows = (int) Math.ceil(ids.size() / 4f);
        int size = Mth.clamp(scrollBarHeight / Math.max(1, totalRows), 8, scrollBarHeight);
        int pos = (int)Mth.map((firstRow) / (float)Math.max(1, totalRows - 4), 0f, 1f,
                0f, scrollBarHeight - size);

        double distance = (scrollBarHeight / Math.max(1, Math.ceil(Math.max(0, ids.size() - 16) / 4f)));
        double ddist = (dragDelta / distance);
        int dist = ddist > 0 ? (int)Math.ceil(ddist) : (int)Math.floor(ddist);

        if (dist != 0) {
            firstRow = Mth.clamp(rowAtDragStart + dist, 0, (int)Math.ceil(Math.max(0, ids.size() - 16) / 4f));
        }

        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingScrollbar = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (super.mouseScrolled(mouseX, mouseY, delta))
            return true;

        if (delta > 0) {
            firstRow = Math.max(0, firstRow - 1);
            return true;
        }

        if (delta < 0) {
            firstRow = Math.min((int)Math.ceil(Math.max(0, ids.size() - 16) / 4f), firstRow + 1);
            return true;
        }

        return false;
    }

    private void openPhotographView(int clickedIndex) {
        List<ItemAndStack<PhotographItem>> photographs = new java.util.ArrayList<>(ids.stream().map(id -> {
            ItemStack stack = new ItemStack(Exposure.Items.PHOTOGRAPH.get());
            CompoundTag tag = new CompoundTag();
            tag.putString(FrameData.ID, id);
            stack.setTag(tag);
            return new ItemAndStack<PhotographItem>(stack);
        }).toList());

        Collections.rotate(photographs, -clickedIndex);

        PhotographScreen screen = new AlbumPhotographScreen(this, photographs);
        Minecraft.getInstance().setScreen(screen);
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(Exposure.SoundEvents.PHOTOGRAPH_RUSTLE.get(),
                Objects.requireNonNull(Minecraft.getInstance().player).level().getRandom().nextFloat() * 0.2f + 1.3f, 0.75f));
    }
}
