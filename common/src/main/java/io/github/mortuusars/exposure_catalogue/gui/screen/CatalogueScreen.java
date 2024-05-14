package io.github.mortuusars.exposure_catalogue.gui.screen;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
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
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class CatalogueScreen extends Screen {
    public record Thumbnail(int index, int gridIndex, Either<String, ResourceLocation> idOrTexture, Rect2i area, boolean selected) {
        public boolean isMouseOver(double mouseX, double mouseY) {
            return mouseX > area.getX() && mouseX <= area.getX() + area.getWidth()
                    && mouseY > area.getY() && mouseY <= area.getY() + area.getHeight();
        }
    }

    public static final ResourceLocation TEXTURE = ExposureCatalogue.resource("textures/gui/catalogue.png");

    public static final int ROWS = 4;
    public static final int COLS = 6;
    public static final Rect2i SCROLL_BAR_AREA = new Rect2i(339, 22, 7, 221);
    public static final Rect2i SEARCH_BAR_AREA = new Rect2i(228, 7, 118, 10);
    public static final Rect2i THUMBNAILS_GRID_AREA = new Rect2i(8, 22, 329, 221);

    protected int imageWidth;
    protected int imageHeight;
    protected int leftPos;
    protected int topPos;

    protected List<String> ids = Collections.emptyList();
    protected int totalRows = 0;

    protected int topRowIndex = 0;

    protected boolean isDraggingScrollbar = false;
    protected int topRowAtDragStart = 0;
    protected double dragDelta = 0;

    protected ArrayList<Thumbnail> visibleThumbnails = new ArrayList<>();

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

        ids = ids.stream().skip(95).toList();

        this.topRowIndex = 0;
        this.ids = ids;
        this.totalRows = (int) Math.ceil(ids.size() / (float)ROWS);
        refreshThumbnailsGrid();
    }

    public void refreshThumbnailsGrid() {
        visibleThumbnails.clear();

        for (int row = 0; row < ROWS; row++) {
            for (int column = 0; column < COLS; column++) {
                int gridIndex = column + row * COLS;
                int idIndex = gridIndex + this.topRowIndex * COLS;

                if (idIndex >= ids.size())
                    break;

                int thumbnailX = column * 54;
                int thumbnailY = row * 54;

                Either<String, ResourceLocation> idOrTexture = Either.left(ids.get(idIndex));
                Rect2i area = new Rect2i(THUMBNAILS_GRID_AREA.getX() + 5 + thumbnailX,
                        THUMBNAILS_GRID_AREA.getY() + 5 + thumbnailY, 48, 48);
                Thumbnail thumbnail = new Thumbnail(idIndex, gridIndex, idOrTexture, area, false);
                visibleThumbnails.add(thumbnail);
            }
        }
    }

    @Override
    protected void init() {
        super.init();
        this.imageWidth = 354;
        this.imageHeight = 265;
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

        renderScrollBar(guiGraphics, mouseX, mouseY, partialTick);
        renderThumbnailsGrid(guiGraphics, mouseX, mouseY, partialTick);
        renderLabels(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY, partialTick);
    }

    protected void renderThumbnailsGrid(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        for (Thumbnail thumbnail : visibleThumbnails) {
            MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
            ExposureClient.getExposureRenderer().render(thumbnail.idOrTexture(), ExposurePixelModifiers.EMPTY,
                    guiGraphics.pose(), bufferSource, leftPos + thumbnail.area().getX(), topPos + thumbnail.area().getY(),
                    thumbnail.area().getWidth(), thumbnail.area().getHeight());
            bufferSource.endBatch();

            int frameVOffset = thumbnail.isMouseOver(mouseX - leftPos, mouseY - topPos) ? 54 : 0;

            RenderSystem.enableBlend();
            // Frame overlay
            guiGraphics.blit(TEXTURE, leftPos + thumbnail.area().getX() - 3, topPos + thumbnail.area().getY() - 3, 361, frameVOffset,
                    54, 54, 512, 512);
            RenderSystem.disableBlend();
        }

//        guiGraphics.pose().pushPose();
//        guiGraphics.pose().translate(leftPos + THUMBNAILS_GRID_AREA.getX(), topPos + THUMBNAILS_GRID_AREA.getY(), 0);
//
//        for (int row = 0; row < ROWS; row++) {
//            for (int column = 0; column < COLS; column++) {
//                int gridIndex = column + row * COLS;
//                int idIndex = gridIndex + this.topRowIndex * COLS;
//
//                if (idIndex >= ids.size())
//                    break;
//
//                MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
//                int thumbnailX = column * 53;
//                int thumbnailY = row * 53;
//                ExposureClient.getExposureRenderer().render(Either.left(ids.get(idIndex)), ExposurePixelModifiers.EMPTY,
//                        guiGraphics.pose(), bufferSource, thumbnailX, thumbnailY, 48, 48);
//                bufferSource.endBatch();
//
//                boolean mouseOver = mouseX > leftPos + THUMBNAILS_GRID_AREA.getX() + thumbnailX - 2
//                                 && mouseX <= leftPos + THUMBNAILS_GRID_AREA.getX() + thumbnailX - 2 + 54
//                                 && mouseY > topPos + THUMBNAILS_GRID_AREA.getY() + thumbnailY - 2
//                                 && mouseY <= topPos + THUMBNAILS_GRID_AREA.getY() + thumbnailY - 2 + 54;
//
//                int frameVOffset = mouseOver ? 54 : 0;
//
//                // Frame overlay
//                guiGraphics.blit(TEXTURE, thumbnailX - 2, thumbnailY - 2, 361, frameVOffset,
//                        54, 54, 512, 512);
//            }
//        }
//
//        guiGraphics.pose().popPose();
    }

    protected void renderTooltip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        for (Thumbnail thumbnail : visibleThumbnails) {
            if (thumbnail.isMouseOver(mouseX - leftPos, mouseY - topPos)) {
                String idOrTextureStr = thumbnail.idOrTexture().map(s -> s, ResourceLocation::toString);

                List<Component> lines = new ArrayList<>();
                lines.add(Component.literal(idOrTextureStr));

                thumbnail.idOrTexture().ifLeft(exposureId -> {
                    ExposureClient.getExposureStorage().getOrQuery(exposureId).ifPresent(data -> {
                        CompoundTag properties = data.getProperties();

                        lines.add(Component.literal(data.getWidth() + "x" + data.getHeight()).withStyle(ChatFormatting.GRAY));

                        if (properties.getBoolean(ExposureSavedData.WAS_PRINTED_PROPERTY))
                            lines.add(Component.literal("Printed").withStyle(ChatFormatting.GRAY));
                    });
                });

                guiGraphics.renderTooltip(font, lines, Optional.empty(), mouseX, mouseY);

                break;
            }
        }
    }

    protected void renderScrollBar(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int size = Mth.clamp(SCROLL_BAR_AREA.getHeight() / Math.max(1, totalRows), 9, SCROLL_BAR_AREA.getHeight());
        size = Math.max(9, size - size % 3);
        int pos = (int)Mth.map((topRowIndex) / (float)Math.max(1, totalRows - 4), 0f, 1f, 0f, SCROLL_BAR_AREA.getHeight() - size);

        // Top
        guiGraphics.blit(TEXTURE, leftPos + SCROLL_BAR_AREA.getX(), topPos + SCROLL_BAR_AREA.getY() + pos,
                354, 0, 7, 3, 512, 512);

        // Middle
        int middleParts = (size - 6) / 4;
        for (int i = 0; i < middleParts; i++) {
            guiGraphics.blit(TEXTURE, leftPos + SCROLL_BAR_AREA.getX(), topPos + SCROLL_BAR_AREA.getY() + pos + i * 4 + 3,
                    354, 3, 7, 4, 512, 512);
        }

        // Bottom
        guiGraphics.blit(TEXTURE, leftPos + SCROLL_BAR_AREA.getX(), topPos + SCROLL_BAR_AREA.getY() + pos + (middleParts * 4) + 3,
                354, 7, 7, 2, 512, 512);
    }

    protected void renderLabels(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Title
        guiGraphics.drawString(font, title, leftPos + 8, topPos + 8, 0xFF414141, false);

        // Count
        if (!ids.isEmpty()) {
            String countStr = Integer.toString(ids.size());
            guiGraphics.drawString(font, countStr, leftPos + (imageWidth / 2) - (font.width(countStr) / 2),
                    topPos + 249, 0xFF414141, false);
        }

//        // Draw ids on the left
//        for (int i = 0; i < Math.min(ids.size(), 16); i++) {
//            guiGraphics.drawString(font, ids.get(i), 5, 5 + i * 9, 0xFFFFFFFF);
//        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button))
            return true;

        if (button != InputConstants.MOUSE_BUTTON_LEFT)
            return false;

        for (Thumbnail thumbnail : visibleThumbnails) {
            if (thumbnail.isMouseOver(mouseX - leftPos, mouseY - topPos)) {
                openPhotographView(thumbnail.index);
                break;
            }
        }

        int scrollBarHeight = 205;
        int scrollBarStartY = 24;
        int scrollBarEndY = 228;

        int size = Mth.clamp(scrollBarHeight / Math.max(1, totalRows), 8, scrollBarHeight);
        int pos = (int)Mth.map((topRowIndex) / (float)Math.max(1, totalRows - 4), 0f, 1f, 0f, scrollBarHeight - size);

        if (mouseX > leftPos + 231 && mouseX <= leftPos + 238 && mouseY > topPos + scrollBarStartY && mouseY <= topPos + scrollBarEndY) {
            int msy = (int)mouseY - (topPos + scrollBarStartY);
            if (msy > pos && msy <= pos + size) {
                this.setDragging(true);
                isDraggingScrollbar = true;
                dragDelta = 0;
                topRowAtDragStart = topRowIndex;
            }

            if (mouseY > topPos + scrollBarStartY + pos + size) {
                scroll(1);
                return true;
            }

            if (mouseY < topPos + scrollBarStartY + pos) {
                scroll(-1);
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (super.mouseDragged(mouseX, mouseY, button, dragX, dragY))
            return true;

        if (!isDraggingScrollbar || button != InputConstants.MOUSE_BUTTON_LEFT)
            return false;

        dragDelta += dragY;

        int scrollBarHeight = 205;
//        int scrollBarStartY = 24;
//        int scrollBarEndY = 228;

//        int size = Mth.clamp(scrollBarHeight / Math.max(1, totalRows), 8, scrollBarHeight);
//        int pos = (int)Mth.map((topRowIndex) / (float)Math.max(1, totalRows - 4), 0f, 1f,
//                0f, scrollBarHeight - size);

        double distance = (scrollBarHeight / Math.max(1, Math.ceil(Math.max(0, ids.size() - 16) / 4f)));
        double ddist = (dragDelta / distance);
        int dist = ddist > 0 ? (int)Math.ceil(ddist) : (int)Math.floor(ddist);

        if (dist != 0) {
            scroll(topRowAtDragStart + dist);
        }

        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        isDraggingScrollbar = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (super.mouseScrolled(mouseX, mouseY, delta))
            return true;

        scroll(delta > 0 ? -1 : +1);
        return true;
    }

    public void scroll(int rows) {
        int maxRowWhenAtEnd = Math.max(0, (int) Math.ceil((ids.size() - ROWS * COLS) / (float) ROWS));
        topRowIndex = Mth.clamp(topRowIndex + rows, 0, maxRowWhenAtEnd);
        refreshThumbnailsGrid();
    }

    protected void openPhotographView(int clickedIndex) {
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
