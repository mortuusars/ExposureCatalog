package io.github.mortuusars.exposure_catalog.gui.screen;

import com.google.common.collect.Lists;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.camera.infrastructure.FrameData;
import io.github.mortuusars.exposure.data.ExposureLook;
import io.github.mortuusars.exposure.data.ExposureSize;
import io.github.mortuusars.exposure.data.storage.ExposureSavedData;
import io.github.mortuusars.exposure.gui.screen.PhotographScreen;
import io.github.mortuusars.exposure.gui.screen.album.AlbumPhotographScreen;
import io.github.mortuusars.exposure.item.PhotographItem;
import io.github.mortuusars.exposure.render.modifiers.ExposurePixelModifiers;
import io.github.mortuusars.exposure.util.ItemAndStack;
import io.github.mortuusars.exposure_catalog.ExposureCatalog;
import io.github.mortuusars.exposure_catalog.data.ExposureInfo;
import io.github.mortuusars.exposure_catalog.data.ExposureThumbnail;
import io.github.mortuusars.exposure_catalog.data.client.CatalogClient;
import io.github.mortuusars.exposure_catalog.gui.Mode;
import io.github.mortuusars.exposure_catalog.gui.Order;
import io.github.mortuusars.exposure_catalog.gui.Sorting;
import io.github.mortuusars.exposure_catalog.gui.screen.tooltip.BelowOrAboveAreaTooltipPositioner;
import io.github.mortuusars.exposure_catalog.gui.screen.widget.EnumButton;
import io.github.mortuusars.exposure_catalog.network.Packets;
import io.github.mortuusars.exposure_catalog.network.packet.server.CatalogClosedC2SP;
import io.github.mortuusars.exposure_catalog.network.packet.server.DeleteExposureC2SP;
import io.github.mortuusars.exposure_catalog.network.packet.server.ExportExposureC2SP;
import io.github.mortuusars.exposure_catalog.network.packet.server.QueryExposuresC2SP;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.searchtree.PlainTextSearchTree;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CatalogScreen extends Screen {
    public static final ResourceLocation TEXTURE = ExposureCatalog.resource("textures/gui/catalog.png");
    public static final int TEX_SIZE = 512;
    public static final int ROWS = 4;
    public static final int COLS = 6;

    protected static final int SCROLL_THUMB_TOP_HEIGHT = 3;
    protected static final int SCROLL_THUMB_MID_HEIGHT = 4;
    protected static final int SCROLL_THUMB_BOT_HEIGHT = 2;

    public static final int REFRESH_COOLDOWN_MS = 500; // 0.5 seconds
    public static final int RELOAD_COOLDOWN_MS = 5000; // 5 seconds

    public record Thumbnail(int index, int gridIndex, Either<String, ResourceLocation> idOrTexture, Rect2i area,
                            boolean selected) {
        public boolean isMouseOver(double mouseX, double mouseY) {
            return mouseX >= area.getX() && mouseX < area.getX() + area.getWidth()
                    && mouseY >= area.getY() && mouseY < area.getY() + area.getHeight();
        }
    }

    protected final File stateFile = new File(Minecraft.getInstance().gameDirectory, "exposure_catalog_state.json");

    protected int imageWidth;
    protected int imageHeight;
    protected int leftPos;
    protected int topPos;
    protected Rect2i windowArea = new Rect2i(0, 0, 361, 265);
    protected Rect2i scrollBarArea = new Rect2i(343, 22, 10, 221);
    protected Rect2i searchBarArea = new Rect2i(219, 7, 118, 10);
    protected Rect2i thumbnailsArea = new Rect2i(8, 22, 329, 221);

    protected EnumButton<Order> orderButton;
    protected EnumButton<Sorting> sortingButton;
    protected EditBox searchBox;
    protected EnumButton<Mode> modeButton;
    protected Rect2i scrollThumb = new Rect2i(0, 0, 0, 0);
    protected List<Thumbnail> thumbnails = Collections.synchronizedList(new ArrayList<>());
    protected Button refreshButton;
    protected Button exportButton;
    protected Button deleteButton;

    protected Mode mode = Mode.EXPOSURES;
    protected Order order = Order.ASCENDING;
    protected Sorting sorting = Sorting.DATE;
    protected ExposureSize exportSize = ExposureSize.X1;
    protected ExposureLook exportLook = ExposureLook.REGULAR;

    protected boolean isLoading;

    protected List<String> exposures = new ArrayList<>();
    protected List<String> textures = Collections.emptyList();

    protected ArrayList<String> filteredItems = new ArrayList<>();

    protected SelectionHandler selection = new SelectionHandler();

    protected int totalRows = 0;

    protected int topRowIndex = 0;
    protected boolean isThumbnailsGridFocused;

    protected int focusedThumbnailIndex;
    protected boolean isDraggingScrollbar = false;

    protected int topRowIndexAtDragStart = 0;
    protected double dragDelta = 0;
    protected boolean initialized;

    protected long refreshCooldownExpireTime = 0;
    protected long lastScrolledTime = 0;

    public CatalogScreen() {
        super(Component.translatable("gui.exposure_catalog.catalog"));
    }

    public void onExposuresReceived(Map<String, ExposureInfo> exposuresList) {
        isLoading = false;
        this.exposures = new ArrayList<>(exposuresList.keySet().stream().toList());
        orderAndSortExposuresList(this.order, this.sorting);

        if (mode == Mode.EXPOSURES) {
            this.topRowIndex = 0;
            refreshSearchResults();
        }
    }

    @Override
    protected void init() {
        super.init();
        this.imageWidth = windowArea.getWidth();
        this.imageHeight = windowArea.getHeight();
        this.leftPos = width / 2 - imageWidth / 2;
        this.topPos = height / 2 - imageHeight / 2;

        scrollBarArea = new Rect2i(leftPos + 343, topPos + 22, 10, 221);
        searchBarArea = new Rect2i(leftPos + 219, topPos + 7, 118, 10);
        thumbnailsArea = new Rect2i(leftPos + 8, topPos + 22, 329, 221);

        orderButton = new EnumButton<>(Order.class, leftPos + 188, topPos + 6, 12, 12, 425, 0,
                12, 12, TEXTURE, 512, 512, (b, prev, current) -> changeOrder(current),
                Component.translatable("gui.exposure_catalog.catalog.order")) {
            @Override
            public void playDownSound(SoundManager handler) {
                playClickSound();
            }
        };
        orderButton.setTooltipFunc(value -> {
            MutableComponent component = Component.translatable("gui.exposure_catalog.catalog.order");

            for (Order v : Order.values()) {
                component.append("\n ");
                component.append(Component.translatable("gui.exposure_catalog.catalog.order." + v.getSerializedName())
                        .withStyle(Style.EMPTY.withColor(value == v ? 0x6677FF : 0x444444)));
            }

            return Tooltip.create(component);
        });
        addRenderableWidget(orderButton);

        sortingButton = new EnumButton<>(Sorting.class, leftPos + 203, topPos + 6, 12, 12, 425, 36,
                12, 12, TEXTURE, 512, 512, (b, prev, current) -> changeSorting(current),
                Component.translatable("gui.exposure_catalog.catalog.sorting")) {
            @Override
            public void playDownSound(SoundManager handler) {
                playClickSound();
            }
        };
        sortingButton.setTooltipFunc(value -> {
            MutableComponent component = Component.translatable("gui.exposure_catalog.catalog.sorting");

            for (Sorting v : Sorting.values()) {
                component.append("\n ");
                component.append(Component.translatable("gui.exposure_catalog.catalog.sorting." + v.getSerializedName())
                        .withStyle(Style.EMPTY.withColor(value == v ? 0x6677FF : 0x444444)));
            }

            return Tooltip.create(component);
        });
        addRenderableWidget(sortingButton);

        searchBox = new EditBox(font, searchBarArea.getX() + 1, searchBarArea.getY() + 1, searchBarArea.getWidth(), font.lineHeight, Component.translatable("itemGroup.search"));
        searchBox.setMaxLength(50);
        searchBox.setBordered(false);
        searchBox.setVisible(true);
        searchBox.setTextColor(0xFFFFFF);
        addRenderableWidget(searchBox);

        modeButton = new EnumButton<>(Mode.class, leftPos + 342, topPos + 6, 12, 12,
                449, 0, 12, 12, TEXTURE, 512, 512,
                (b, prev, current) -> changeMode(current), Component.translatable("gui.exposure_catalog.catalog.mode")) {
            @Override
            public void playDownSound(SoundManager handler) {
                playClickSound();
            }
        };
        modeButton.setTooltipFunc(value -> {
            MutableComponent component = Component.translatable("gui.exposure_catalog.catalog.mode")
                    .append(" ")
                    .append(Component.translatable("gui.exposure_catalog.catalog.mode.hotkey"));

            for (Mode v : Mode.values()) {
                component.append("\n ");
                component.append(Component.translatable("gui.exposure_catalog.catalog.mode." + v.getSerializedName())
                        .withStyle(Style.EMPTY.withColor(value == v ? 0x6677FF : 0x444444)));
            }

            return Tooltip.create(component);
        });
        addRenderableWidget(modeButton);

        refreshButton = new ImageButton(leftPos + 7, topPos + 247, 12, 12, 449, 36,
                12, TEXTURE, 512, 512, b -> refresh());
        refreshButton.setTooltip(Tooltip.create(Component.translatable("gui.exposure_catalog.catalog.refresh")
                .append(" ")
                .append(Component.translatable("gui.exposure_catalog.catalog.refresh.hotkey"))
                .append("\n")
                .append(Component.translatable("gui.exposure_catalog.catalog.refresh.tooltip"))));
        addRenderableWidget(refreshButton);

        exportButton = new ImageButton(leftPos + 26, topPos + 247, 12, 12, 473, 36,
                12, TEXTURE, 512, 512, b -> exportExposures()) {
            @Override
            public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                if (isHoveredOrFocused())
                    setTooltip(Tooltip.create(createExportButtonTooltip()));
                super.render(guiGraphics, mouseX, mouseY, partialTick);
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (!isHoveredOrFocused() || !this.active || !this.visible)
                    return super.mouseClicked(mouseX, mouseY, button);

                if (Screen.hasControlDown()) {
                    exportSize = ExposureSize.values()[(exportSize.ordinal() + 1) % ExposureSize.values().length];
                    playClickSound();
                    return true;
                }

                if (Screen.hasShiftDown()) {
                    exportLook = ExposureLook.values()[(exportLook.ordinal() + 1) % ExposureLook.values().length];
                    playClickSound();
                    return true;
                }

                return super.mouseClicked(mouseX, mouseY, button);
            }

            @Override
            public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
                if (!isHoveredOrFocused() || !this.active || !this.visible)
                    return super.mouseScrolled(mouseX, mouseY, delta);

                if (Screen.hasControlDown()) {
                    int newValue = exportSize.ordinal() - (int) delta;
                    if (newValue < 0)
                        newValue = ExposureSize.values().length - 1;
                    else if (newValue >= ExposureSize.values().length)
                        newValue = 0;

                    if (exportSize != ExposureSize.values()[newValue]) {
                        playClickSound();
                        exportSize = ExposureSize.values()[newValue];
                    }
                    return true;
                }

                if (Screen.hasShiftDown()) {
                    int newValue = exportLook.ordinal() - (int) delta;
                    if (newValue < 0)
                        newValue = ExposureLook.values().length - 1;
                    else if (newValue >= ExposureLook.values().length)
                        newValue = 0;

                    if (exportLook != ExposureLook.values()[newValue]) {
                        playClickSound();
                        exportLook = ExposureLook.values()[newValue];
                    }
                    return true;
                }

                return super.mouseScrolled(mouseX, mouseY, delta);
            }

            @Override
            public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
                if (!isHoveredOrFocused() || !this.active || !this.visible)
                    return super.keyPressed(keyCode, scanCode, modifiers);

                if (CommonInputs.selected(keyCode) && Screen.hasControlDown()) {
                    exportSize = ExposureSize.values()[(exportSize.ordinal() + 1) % ExposureSize.values().length];
                    playClickSound();
                    return true;
                }

                if (CommonInputs.selected(keyCode) && Screen.hasShiftDown()) {
                    exportLook = ExposureLook.values()[(exportLook.ordinal() + 1) % ExposureLook.values().length];
                    playClickSound();
                    return true;
                }

                return super.keyPressed(keyCode, scanCode, modifiers);
            }
        };
        exportButton.setTooltip(Tooltip.create(Component.translatable("gui.exposure_catalog.catalog.export")));
        addRenderableWidget(exportButton);

        deleteButton = new ImageButton(leftPos + 342, topPos + 247, 12, 12, 473, 0,
                12, TEXTURE, 512, 512, b -> deleteExposures());
        deleteButton.setTooltip(Tooltip.create(Component.translatable("gui.exposure_catalog.catalog.delete")
                .append(" ")
                .append(Component.translatable("gui.exposure_catalog.catalog.delete.hotkey"))
                .append("\n")
                .append(Component.translatable("gui.exposure_catalog.catalog.delete.tooltip"))));
        addRenderableWidget(deleteButton);

        if (!initialized) {
            loadState();
            ExposureClient.getExposureStorage().clear();
            Packets.sendToServer(new QueryExposuresC2SP(false));
            isLoading = true;
            initialized = true;
        }

        updateElements();
    }

    protected void playClickSound() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(
                Exposure.SoundEvents.CAMERA_DIAL_CLICK.get(), 1f, 0.8f));
    }

    protected Component createExportButtonTooltip() {
        MutableComponent tooltip = Component.translatable("gui.exposure_catalog.catalog.export." +
                        (selection.isEmpty() || selection.size() == exposures.size() ? "all" : "selected"))
                .append(" ")
                .append(Component.translatable("gui.exposure_catalog.catalog.export.hotkey"));

        tooltip.append("\n");
        tooltip.append(Component.translatable("gui.exposure_catalog.catalog.export.location_info"));

        tooltip.append("\n")
                .append("\n")
                .append(Component.translatable("gui.exposure_catalog.catalog.export.size"));

        for (ExposureSize size : ExposureSize.values()) {
            tooltip.append("\n");
            tooltip.append(Component.translatable("gui.exposure_catalog.catalog.export.size." + size.getSerializedName())
                    .withStyle(Style.EMPTY.withColor(exportSize == size ? 0x6677FF : 0x444444)));
        }

        tooltip.append("\n")
                .append("\n")
                .append(Component.translatable("gui.exposure_catalog.catalog.export.look"));

        for (ExposureLook look : ExposureLook.values()) {
            tooltip.append("\n")
                    .append(Component.translatable("gui.exposure_catalog.catalog.export.look." + look.getSerializedName())
                            .withStyle(Style.EMPTY.withColor(exportLook == look ? 0x6677FF : 0x444444)));
        }

        tooltip.append("\n")
                .append("\n")
                .append(Component.translatable("gui.exposure_catalog.catalog.export.control_info"));

        return tooltip;
    }

    protected boolean canRefresh() {
        if (mode == Mode.TEXTURES)
            return true;

        if (mode == Mode.EXPOSURES && isLoading)
            return false;

        return Util.getMillis() >= refreshCooldownExpireTime;
    }

    protected void refresh() {
        if (!canRefresh())
            return;

        if (mode == Mode.EXPOSURES) {
            ExposureClient.getExposureStorage().clear();
            boolean reload = Screen.hasShiftDown();
            Packets.sendToServer(new QueryExposuresC2SP(reload));
            if (reload) {
                isLoading = true;
                CatalogClient.clear();
            }
            refreshCooldownExpireTime = Util.getMillis() + (reload ? RELOAD_COOLDOWN_MS : REFRESH_COOLDOWN_MS);
        } else if (mode == Mode.TEXTURES) {
            Map<ResourceLocation, Resource> resources = Minecraft.getInstance().getResourceManager().listResources("textures", rl -> true);
            textures = resources.keySet().stream().map(ResourceLocation::toString).collect(Collectors.toCollection(ArrayList::new));
            orderTexturesList(this.order);
            refreshSearchResults();
            updateElements();
        }
    }

    protected void exportExposures() {
        if (!selection.isEmpty()) {
            for (int index : selection.get()) {
                String exposureId = filteredItems.get(index);
                Packets.sendToServer(new ExportExposureC2SP(exposureId, exportSize, exportLook));
            }
        } else if (filteredItems.size() > 100) {
            Component message = Component.translatable("gui.exposure_catalog.catalog.confirm.message.export_all", filteredItems.size());
            Screen confirmScreen = new ConfirmScreen(this, message, CommonComponents.GUI_YES,
                    b -> exportAllExposures(), CommonComponents.GUI_NO, b -> {
            });
            Minecraft.getInstance().setScreen(confirmScreen);
        } else {
            exportAllExposures();
        }
    }

    protected void exportAllExposures() {
        for (String exposureId : filteredItems) {
            Packets.sendToServer(new ExportExposureC2SP(exposureId, exportSize, exportLook));
        }
    }

    protected void deleteExposures() {
        if (mode != Mode.EXPOSURES || selection.isEmpty() || filteredItems.isEmpty())
            return;

        if (Screen.hasShiftDown())
            deleteExposuresNoConfirm();
        else {
            Component message;
            if (selection.size() == 1) {
                String exposureId = filteredItems.get(selection.get().iterator().next());
                message = Component.translatable("gui.exposure_catalog.catalog.confirm.message.delete_one", exposureId);
            } else {
                message = Component.translatable("gui.exposure_catalog.catalog.confirm.message.delete_many", selection.size());
            }

            Screen confirmScreen = new ConfirmScreen(this, message, CommonComponents.GUI_YES,
                    b -> deleteExposuresNoConfirm(), CommonComponents.GUI_NO, b -> {
            });
            Minecraft.getInstance().setScreen(confirmScreen);
        }
    }

    protected void deleteExposuresNoConfirm() {
        if (mode != Mode.EXPOSURES || selection.isEmpty() || filteredItems.isEmpty())
            return;

        ArrayList<String> removedIds = new ArrayList<>();

        for (Integer index : selection.get()) {
            if (index < 0 || index >= filteredItems.size())
                continue;

            String exposureId = filteredItems.get(index);
            Packets.sendToServer(new DeleteExposureC2SP(exposureId));
            removedIds.add(exposureId);
        }

        //noinspection RedundantOperationOnEmptyContainer
        for (String id : removedIds) {
            filteredItems.remove(id);
            CatalogClient.removeExposure(id);
        }

        selection.clear();

        if (isThumbnailsGridFocused) {
            int globalIndex = focusedThumbnailIndex + (topRowIndex * COLS);
            if (globalIndex >= 0 && globalIndex < filteredItems.size() - 1)
                selection.select(globalIndex);
        }

        updateElements();
    }

    protected void updateButtons() {
        orderButton.setState(order);
        sortingButton.setState(sorting);
        modeButton.setState(mode);

        sortingButton.active = mode == Mode.EXPOSURES;

        exportButton.active = mode == Mode.EXPOSURES;
        deleteButton.active = mode == Mode.EXPOSURES && !selection.isEmpty();

        refreshButton.active = canRefresh();
    }

    protected void changeMode(Mode mode) {
        this.mode = mode;

        if ((mode == Mode.EXPOSURES && exposures.isEmpty()) ||
                (mode == Mode.TEXTURES && textures.isEmpty())) {
            refresh();
        }

        updateButtons();
        refreshSearchResults();
    }

    protected void changeOrder(Order order) {
        this.order = order;

        if (mode == Mode.EXPOSURES) {
            orderAndSortExposuresList(this.order, this.sorting);
        } else {
            orderTexturesList(this.order);
        }

        updateButtons();
        refreshSearchResults();
    }

    protected void changeSorting(Sorting sorting) {
        this.sorting = sorting;

        if (mode == Mode.EXPOSURES) {
            orderAndSortExposuresList(this.order, this.sorting);
        }

        updateButtons();
        refreshSearchResults();
    }

    protected void orderAndSortExposuresList(Order order, Sorting sorting) {
        exposures.sort(Comparator.naturalOrder());

        if (sorting == Sorting.DATE) {
            Comparator<String> comparator = new Comparator<>() {
                @Override
                public int compare(String s1, String s2) {
                    return Long.compare(getTimestamp(s1), getTimestamp(s2));
                }

                private long getTimestamp(String exposureId) {
                    @Nullable ExposureInfo exposureData = CatalogClient.getExposures().get(exposureId);
                    return exposureData != null ? exposureData.getTimestampUnixSeconds() : 0L;
                }
            };

            exposures.sort(comparator);
        }

        if (order == Order.DESCENDING)
            Collections.reverse(exposures);
    }

    protected void orderTexturesList(Order order) {
        textures.sort(order == Order.ASCENDING ? Comparator.naturalOrder() : Comparator.reverseOrder());
    }

    @Override
    public void tick() {
        searchBox.tick();
    }

    @Override
    protected void rebuildWidgets() {
        String searchBoxValue = searchBox.getValue();
        super.rebuildWidgets();
        searchBox.setValue(searchBoxValue);
    }

    protected void refreshSearchResults() {
        filteredItems.clear();

        List<String> items = mode == Mode.EXPOSURES ? exposures : textures;

        String filter = searchBox != null ? searchBox.getValue() : "";
        if (filter.isEmpty()) {
            filteredItems.addAll(items);
        } else {
            PlainTextSearchTree<String> tree = PlainTextSearchTree.create(items, String::lines);
            filteredItems.addAll(tree.search(filter.toLowerCase(Locale.ROOT)));
        }

        this.totalRows = (int) Math.ceil(filteredItems.size() / (float) COLS);

        selection.clear();
        scroll(Integer.MIN_VALUE);
    }

    public void updateThumbnailsGrid() {
        thumbnails.clear();

        for (int row = 0; row < ROWS; row++) {
            for (int column = 0; column < COLS; column++) {
                int gridIndex = column + row * COLS;
                int idIndex = gridIndex + this.topRowIndex * COLS;

                if (idIndex >= filteredItems.size())
                    break;

                int thumbnailX = column * 54;
                int thumbnailY = row * 54;

                String item = filteredItems.get(idIndex);
                Either<String, ResourceLocation> idOrTexture;
                if (mode == Mode.EXPOSURES)
                    idOrTexture = Either.left(item);
                else
                    idOrTexture = Either.right(new ResourceLocation(item));

                Rect2i area = new Rect2i(thumbnailsArea.getX() + 5 + thumbnailX,
                        thumbnailsArea.getY() + 5 + thumbnailY, 48, 48);
                boolean isSelected = selection.get().contains(idIndex);
                Thumbnail thumbnail = new Thumbnail(idIndex, gridIndex, idOrTexture, area, isSelected);
                thumbnails.add(thumbnail);
            }
        }

        focusedThumbnailIndex = Mth.clamp(focusedThumbnailIndex, 0, thumbnails.size() - 1);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        refreshButton.active = canRefresh();

        renderBackground(guiGraphics);

        // Main texture
        guiGraphics.blit(TEXTURE, leftPos, topPos, imageWidth, imageHeight, 0, 0,
                imageWidth, imageHeight, TEX_SIZE, TEX_SIZE);

        super.render(guiGraphics, mouseX, mouseY, partialTick);

        renderScrollBar(guiGraphics, mouseX, mouseY, partialTick);
        renderThumbnailsGrid(guiGraphics, mouseX, mouseY, partialTick);
        renderLabels(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY, partialTick);
    }

    protected void renderThumbnailsGrid(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        for (Thumbnail thumbnail : thumbnails) {

            MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());

            if (Minecraft.getInstance().isSingleplayer()) { // Loading full size images is ok in singleplayer
                ExposureClient.getExposureRenderer().render(thumbnail.idOrTexture(), ExposurePixelModifiers.EMPTY,
                        guiGraphics.pose(), bufferSource, thumbnail.area().getX(), thumbnail.area().getY(),
                        thumbnail.area().getWidth(), thumbnail.area().getHeight());
            } else {
                thumbnail.idOrTexture
                        .ifLeft(exposureId ->
                                renderExposureThumbnail(guiGraphics, bufferSource, exposureId, thumbnail.area()))
                        .ifRight(texture ->
                                ExposureClient.getExposureRenderer().render(thumbnail.idOrTexture(), ExposurePixelModifiers.EMPTY,
                                        guiGraphics.pose(), bufferSource, thumbnail.area().getX(), thumbnail.area().getY(),
                                        thumbnail.area().getWidth(), thumbnail.area().getHeight()));
            }

            bufferSource.endBatch();

            int frameVOffset = thumbnail.selected() ? 108 :
                    thumbnail.isMouseOver(mouseX, mouseY) ? 54 : 0;

            RenderSystem.enableBlend();
            // Frame overlay
            guiGraphics.blit(TEXTURE, thumbnail.area().getX() - 3, thumbnail.area().getY() - 3, 371, frameVOffset,
                    54, 54, 512, 512);
            RenderSystem.disableBlend();
        }
    }

    protected void renderExposureThumbnail(GuiGraphics guiGraphics, MultiBufferSource bufferSource, String exposureId, Rect2i area) {
        @Nullable ExposureThumbnail thumbnail;
        if (Util.getMillis() - lastScrolledTime < 250)
            thumbnail = CatalogClient.getThumbnail(exposureId).orElse(null);
        else
            thumbnail = CatalogClient.getOrQueryThumbnail(exposureId).orElse(null);

        if (thumbnail == null) return;

        CatalogClient.getThumbnailRenderer().render(exposureId, thumbnail, ExposurePixelModifiers.EMPTY,
                guiGraphics.pose(), bufferSource, area.getX(), area.getY(), area.getWidth(), area.getHeight());
    }

    protected void renderTooltip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (searchBox.isMouseOver(mouseX, mouseY)) {
            guiGraphics.renderTooltip(font, Component.translatable("gui.exposure_catalog.searchbar.hotkey"), mouseX, mouseY);
            return;
        }

        for (Thumbnail thumbnail : thumbnails) {
            if (thumbnail.isMouseOver(mouseX, mouseY)) {
                List<Component> lines = getThumbnailTooltipLines(thumbnail);
                guiGraphics.renderTooltip(font, lines, Optional.empty(), mouseX, mouseY);
                break;
            }
            if (isThumbnailsGridFocused && thumbnail.gridIndex() == focusedThumbnailIndex) {
                List<Component> lines = getThumbnailTooltipLines(thumbnail);
                guiGraphics.renderTooltip(font, Lists.transform(lines, Component::getVisualOrderText),
                        new BelowOrAboveAreaTooltipPositioner(thumbnail.area()), mouseX, mouseY);
                break;
            }
        }
    }

    @NotNull
    private static List<Component> getThumbnailTooltipLines(Thumbnail thumbnail) {
        String idOrTextureStr = thumbnail.idOrTexture().map(s -> s, ResourceLocation::toString);

        List<Component> lines = new ArrayList<>();
        lines.add(Component.literal(idOrTextureStr));

        thumbnail.idOrTexture().ifLeft(exposureId -> {
            ExposureClient.getExposureStorage().getOrQuery(exposureId).ifPresent(data -> {
                CompoundTag properties = data.getProperties();

                long timestampSeconds = properties.getLong(ExposureSavedData.TIMESTAMP_PROPERTY);
                if (timestampSeconds > 0) {
                    Date date = new Date(timestampSeconds * 1000L);
                    String pattern = "yyyy-MM-dd HH:mm:ss";
                    SimpleDateFormat format = new SimpleDateFormat(pattern);
                    String format1 = format.format((date));
                    lines.add(Component.literal(format1).withStyle(ChatFormatting.GRAY));
                }

                lines.add(Component.literal(data.getWidth() + "x" + data.getHeight()).withStyle(ChatFormatting.GRAY));

                if (properties.getBoolean(ExposureSavedData.WAS_PRINTED_PROPERTY))
                    lines.add(Component.literal("Printed").withStyle(ChatFormatting.GRAY));
            });
        });

        lines.add(Component.empty());
        if (Screen.hasShiftDown()) {
            lines.add(Component.translatable("gui.exposure_catalog.thumbnail.tooltip.view"));
            lines.add(Component.translatable("gui.exposure_catalog.thumbnail.tooltip.selection"));
            lines.add(Component.translatable("gui.exposure_catalog.thumbnail.tooltip.selection.shift"));
        } else {
            lines.add(Component.translatable("gui.exposure_catalog.thumbnail.tooltip.control_info"));
        }
        return lines;
    }

    protected boolean isMouseOver(Rect2i rect, double mouseX, double mouseY) {
        return mouseX >= rect.getX() && mouseX < rect.getX() + rect.getWidth()
                && mouseY >= rect.getY() && mouseY < rect.getY() + rect.getHeight();
    }

    protected void renderScrollBar(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int state = 0;
        if (!canScroll())
            state = 2;
        else if (isDraggingScrollbar || isMouseOver(scrollThumb, mouseX, mouseY))
            state = 1;

        int thumbStateOffset = SCROLL_THUMB_TOP_HEIGHT + SCROLL_THUMB_MID_HEIGHT + SCROLL_THUMB_BOT_HEIGHT;

        // Top
        guiGraphics.blit(TEXTURE, scrollThumb.getX(), scrollThumb.getY(),
                361, state * thumbStateOffset,
                scrollThumb.getWidth(), SCROLL_THUMB_TOP_HEIGHT, 512, 512);

        // Middle
        int middleParts = (scrollThumb.getHeight() - 3 - 2) / 4;
        for (int i = 0; i < middleParts; i++) {
            guiGraphics.blit(TEXTURE, scrollThumb.getX(), scrollThumb.getY() + i * 4 + 3,
                    361, SCROLL_THUMB_TOP_HEIGHT + state * thumbStateOffset,
                    scrollThumb.getWidth(), SCROLL_THUMB_MID_HEIGHT, 512, 512);
        }

        // Bottom
        guiGraphics.blit(TEXTURE, scrollThumb.getX(), scrollThumb.getY() + (middleParts * 4) + 3,
                361, SCROLL_THUMB_TOP_HEIGHT + SCROLL_THUMB_MID_HEIGHT + state * thumbStateOffset,
                scrollThumb.getWidth(), SCROLL_THUMB_BOT_HEIGHT, 512, 512);
    }

    protected void renderLabels(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Title
        Component title = mode == Mode.EXPOSURES ? Component.translatable("gui.exposure_catalog.catalog.exposures")
                : Component.translatable("gui.exposure_catalog.catalog.textures");
        guiGraphics.drawString(font, title, leftPos + 8, topPos + 8, 0xFF414141, false);

        // Count
        if (isLoading && mode == Mode.EXPOSURES) {
            int dotAnimation = (int) (Util.getMillis() / 750 % 3) + 1;
            Component component = Component.translatable("gui.exposure_catalog.catalog.loading" + dotAnimation)
                    .withStyle(Style.EMPTY.withColor(0xFF414141));
            guiGraphics.drawString(font, component, leftPos + (imageWidth / 2) - (font.width(component) / 2),
                    topPos + 249, 0xFF414141, false);
        } else if (!filteredItems.isEmpty()) {
            String filteredCountStr = Integer.toString(filteredItems.size());

            Component countComponent = Component.literal(filteredCountStr).withStyle(Style.EMPTY.withColor(0xFF414141));
            if (!selection.isEmpty()) {
                String selectedCountStr = Integer.toString(selection.size());
                countComponent = Component.literal(selectedCountStr).withStyle(Style.EMPTY.withColor(0xFF3858db))
                        .append(Component.literal("/").withStyle(Style.EMPTY.withColor(0xFF414141)))
                        .append(countComponent);
            }

            guiGraphics.drawString(font, countComponent, leftPos + (imageWidth / 2) - (font.width(countComponent) / 2),
                    topPos + 249, 0xFF414141, false);
        }

        // SearchBox placeholder text
        if (searchBox.isVisible() && !searchBox.isFocused() && searchBox.getValue().isEmpty()) {
            guiGraphics.drawString(font, Component.translatable("gui.exposure_catalog.catalog.search_bar_placeholder_text"),
                    searchBarArea.getX() + 2, searchBarArea.getY() + 1, 0xFFBEBEBE, false);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (getFocused() == searchBox && !searchBox.isMouseOver(mouseX, mouseY))
            setFocused(null);

        if (isThumbnailsGridFocused) {
            setFocused(null);
            isThumbnailsGridFocused = false;
        }

        if (button == InputConstants.MOUSE_BUTTON_RIGHT && searchBox.isMouseOver(mouseX, mouseY)) {
            String value = searchBox.getValue();
            searchBox.setValue("");
            if (!value.equals(searchBox.getValue()))
                refreshSearchResults();

            setFocused(searchBox);

            return true;
        }

        if (super.mouseClicked(mouseX, mouseY, button))
            return true;

        if (button == InputConstants.MOUSE_BUTTON_MIDDLE)
            return false;

        for (Thumbnail thumbnail : thumbnails) {
            if (!thumbnail.isMouseOver(mouseX, mouseY))
                continue;

            if (Screen.hasControlDown() || button == InputConstants.MOUSE_BUTTON_RIGHT) {
                if (Screen.hasShiftDown()) {
                    int start = Math.min(thumbnail.index(), selection.getLastSelectedIndex());
                    int end = Math.max(thumbnail.index(), selection.getLastSelectedIndex());
                    for (int i = start; i <= end; i++) {
                        if (!selection.get().contains(i))
                            selection.select(i);
                    }
                } else {
                    if (selection.get().contains(thumbnail.index))
                        selection.remove(thumbnail.index());
                    else {
                        selection.select(thumbnail.index());
                    }
                }
                updateElements();
            } else {
                openPhotographView(thumbnail.index());
            }
            return true;
        }

        if (canScroll()) {
            if (isMouseOver(scrollThumb, mouseX, mouseY)) {
                this.setDragging(true);
                isDraggingScrollbar = true;
                dragDelta = 0;
                topRowIndexAtDragStart = topRowIndex;
                return true;
            } else if (isMouseOver(scrollBarArea, mouseX, mouseY)) {
                int direction = mouseY < scrollThumb.getY() ? -1 : 1;
                scroll(ROWS * direction);
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (!isDraggingScrollbar || button != InputConstants.MOUSE_BUTTON_LEFT)
            return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);

        dragDelta += dragY;

        double threshold = (double) scrollBarArea.getHeight() / Math.max(totalRows, 1);
        int rows = (int) (dragDelta / threshold);
        if (rows != 0 || topRowIndex != topRowIndexAtDragStart) {
            scrollTo(topRowIndexAtDragStart + rows);
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

        scroll((int) -delta);
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == InputConstants.KEY_ESCAPE && (searchBox.isFocused() || isThumbnailsGridFocused)) {
            setFocused(null);
            isThumbnailsGridFocused = false;
            return true;
        }

        if (isThumbnailsGridFocused) {
            if (keyCode == InputConstants.KEY_RETURN) {
                int focusedThumbnailGlobalIndex = focusedThumbnailIndex + (topRowIndex * COLS);
                if (!Screen.hasControlDown()) {
                    openPhotographView(thumbnails.get(focusedThumbnailIndex).index());
//                    if (selectedIndexes.contains(focusedThumbnailGlobalIndex))
//                        selectedIndexes.remove(Integer.valueOf(focusedThumbnailGlobalIndex));
//                    else {
//                        selectedIndexes.add(focusedThumbnailGlobalIndex);
//                        selectionStartIndex = focusedThumbnailGlobalIndex;
//                    }
//                    updateElements();
//                } else {
//                    openPhotographView(thumbnails.get(focusedThumbnailIndex).index());
                }
                return true;
            }
        }

        if (searchBox.canConsumeInput() && keyCode != InputConstants.KEY_TAB) {
            String string = searchBox.getValue();
            if (searchBox.keyPressed(keyCode, scanCode, modifiers)) {
                if (!string.equals(searchBox.getValue())) {
                    refreshSearchResults();
                }
                return true;
            } else if (keyCode != InputConstants.KEY_ESCAPE)
                return true;
        }

        if (tabKeyPressed(keyCode)) return true;

        if (arrowKeysPressed(keyCode)) return true;

        if (keyCode == InputConstants.KEY_HOME) {
            selection.clear();
            selection.select(0);
            focusedThumbnailIndex = 0;
            scroll(Integer.MIN_VALUE);
            return true;
        }
        if (keyCode == InputConstants.KEY_END) {
            selection.clear();
            selection.select(filteredItems.size() - 1);
            focusedThumbnailIndex = filteredItems.size() - 1;
            scroll(Integer.MAX_VALUE);
            return true;
        }

        if (keyCode == InputConstants.KEY_DELETE) {
            deleteExposures();
            return true;
        }

        if (Screen.hasControlDown()) {
            if (keyCode == InputConstants.KEY_F) {
                setFocused(searchBox);
                return true;
            }
            if (keyCode == InputConstants.KEY_M) {
                changeMode(Mode.values()[(mode.ordinal() + 1) % Mode.values().length]);
                playClickSound();
                return true;
            }
            if (keyCode == InputConstants.KEY_R) {
                if (canRefresh()) {
                    refresh();
                    playClickSound();
                }
                return true;
            }
            if (keyCode == InputConstants.KEY_E) {
                exportExposures();
                playClickSound();
                return true;
            }
            if (keyCode == InputConstants.KEY_A) {
                selection.clear();
                selection.select(IntStream.range(0, filteredItems.size()).boxed().toList());
                updateElements();
                playClickSound();
                return true;
            }
            if (keyCode == InputConstants.KEY_D) {
                if (!selection.isEmpty()) {
                    selection.clear();
                    updateElements();
                    playClickSound();
                }
                return true;
            }
        }

        if (Minecraft.getInstance().options.keyInventory.matches(keyCode, scanCode)) {
            this.onClose();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private boolean tabKeyPressed(int keyCode) {
        if (keyCode != InputConstants.KEY_TAB)
            return false;

        if (!filteredItems.isEmpty() && !Screen.hasShiftDown() && getFocused() == modeButton) {
            setFocused(null);
            isThumbnailsGridFocused = true;
            focusedThumbnailIndex = 0;

            selection.clear();
            selection.select(focusedThumbnailIndex + (topRowIndex * COLS));
            updateElements();

            return true;
        } else if (!filteredItems.isEmpty() && Screen.hasShiftDown() && getFocused() == refreshButton && !isThumbnailsGridFocused) {
            setFocused(null);
            isThumbnailsGridFocused = true;
            focusedThumbnailIndex = 0;

            selection.clear();
            selection.select(focusedThumbnailIndex + (topRowIndex * COLS));
            updateElements();

            return true;
        } else if (isThumbnailsGridFocused) {
            Button newFocusTarget = Screen.hasShiftDown() ? modeButton : refreshButton;
            setFocused(newFocusTarget);
            isThumbnailsGridFocused = false;

            if (selection.size() == 1 && selection.get().iterator().next() == focusedThumbnailIndex + (topRowIndex * COLS)) {
                selection.clear();
                updateElements();
            }

            return true;
        }
        return false;
    }

    private boolean arrowKeysPressed(int keyCode) {
        if (filteredItems.isEmpty() || !List.of(InputConstants.KEY_LEFT, InputConstants.KEY_RIGHT,
                InputConstants.KEY_UP, InputConstants.KEY_DOWN).contains(keyCode))
            return false;

        if (!isThumbnailsGridFocused) {
            setFocused(null);
            isThumbnailsGridFocused = true;
            focusedThumbnailIndex = 0;
            selection.select(focusedThumbnailIndex + (topRowIndex * COLS));
            updateElements();
            return true;
        }

        Map<Integer, Integer> keys = Map.of(
                InputConstants.KEY_LEFT, -1,
                InputConstants.KEY_RIGHT, 1,
                InputConstants.KEY_UP, -COLS,
                InputConstants.KEY_DOWN, COLS);
        int change = keys.get(keyCode);
        int oldIndex = focusedThumbnailIndex;
        int newIndex = focusedThumbnailIndex + change;

        if (newIndex < 0) {
            if (topRowIndex <= 0)
                return true;
            scroll(-1);
            newIndex += COLS;
        } else if (newIndex > (ROWS * COLS - 1)) {
            if (topRowIndex >= totalRows - ROWS)
                return true;
            scroll(1);
            newIndex -= COLS;
        }

        focusedThumbnailIndex = Mth.clamp(newIndex, 0, thumbnails.size() - 1);


//            if (keyCode == InputConstants.KEY_LEFT) {
//                int newIndex = focusedThumbnailIndex - 1;
//                if (newIndex < 0 && topRowIndex > 0) {
//                    scroll(-1);
//                    focusedThumbnailIndex = COLS - 1;
//                } else
//                    focusedThumbnailIndex = Mth.clamp(newIndex, 0, thumbnails.size() - 1);
//            } else if (keyCode == InputConstants.KEY_RIGHT) {
//                int newIndex = focusedThumbnailIndex + 1;
//                if (newIndex > thumbnails.size() - 1 && thumbnails.size() == ROWS * COLS) {
//                    scroll(1);
//                    focusedThumbnailIndex = ROWS * COLS - COLS;
//                } else
//                    focusedThumbnailIndex = Mth.clamp(newIndex, 0, thumbnails.size() - 1);
//            } else if (keyCode == InputConstants.KEY_UP) {
//                int newIndex = focusedThumbnailIndex - COLS;
//                if (newIndex < 0 && topRowIndex > 0) {
//                    scroll(-1);
//                } else
//                    focusedThumbnailIndex = Mth.clamp(newIndex, 0, thumbnails.size() - 1);
//            } else if (keyCode == InputConstants.KEY_DOWN) {
//                int newIndex = focusedThumbnailIndex + COLS;
//                if (newIndex > thumbnails.size() - 1 && thumbnails.size() == ROWS * COLS) {
//                    scroll(1);
//                } else
//                    focusedThumbnailIndex = Mth.clamp(newIndex, 0, thumbnails.size() - 1);
//            }

        if (!Screen.hasShiftDown())
            selection.clear();
        else {
            int lesser = Math.min(oldIndex, newIndex);
            int larger = Math.max(oldIndex, newIndex);
            for (int i = lesser; i <= larger; i++) {
                selection.select(i + (topRowIndex * COLS));
            }
        }

        selection.select(focusedThumbnailIndex + (topRowIndex * COLS));
        updateElements();

        return true;
    }

    protected void updateElements() {
        updateScrollThumb();
        updateButtons();
        updateThumbnailsGrid();
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (searchBox.canConsumeInput()) {
            String string = searchBox.getValue();
            if (searchBox.charTyped(codePoint, modifiers)) {
                if (!string.equals(searchBox.getValue())) {
                    refreshSearchResults();
                }
                return true;
            }
        }

        return false;
    }

    public boolean canScroll() {
        return totalRows > ROWS;
    }

    public void scroll(int rows) {
        scrollTo(topRowIndex + rows);
    }

    public void scrollTo(int row) {
        int maxRowWhenAtEnd = Math.max(0, (int) Math.ceil((filteredItems.size() - ROWS * COLS) / (float) COLS));
        topRowIndex = Mth.clamp(row, 0, maxRowWhenAtEnd);
        updateScrollThumb();
        updateThumbnailsGrid();
        lastScrolledTime = Util.getMillis();
    }

    protected void updateScrollThumb() {
        int minSize = SCROLL_THUMB_TOP_HEIGHT + SCROLL_THUMB_MID_HEIGHT + SCROLL_THUMB_BOT_HEIGHT;

        float ratio = ROWS / (float) Math.max(totalRows, 1);
        int size = Mth.clamp(Mth.ceil(scrollBarArea.getHeight() * ratio), minSize, scrollBarArea.getHeight());
        int midSize = size - SCROLL_THUMB_TOP_HEIGHT - SCROLL_THUMB_BOT_HEIGHT;
        int correctedMidSize = Math.max(midSize - (midSize % SCROLL_THUMB_MID_HEIGHT), SCROLL_THUMB_MID_HEIGHT);
        size = SCROLL_THUMB_TOP_HEIGHT + correctedMidSize + SCROLL_THUMB_BOT_HEIGHT;

        float topRowPos = (float) topRowIndex / Math.max(1, totalRows - ROWS);
        int pos = (int) Mth.map(topRowPos, 0f, 1f, 0f, scrollBarArea.getHeight() - size);

        scrollThumb = new Rect2i(scrollBarArea.getX(), scrollBarArea.getY() + pos, scrollBarArea.getWidth(), size);
    }

    protected void openPhotographView(int clickedIndex) {
        List<ItemAndStack<PhotographItem>> photographs = new ArrayList<>(filteredItems.stream().map(item -> {
            ItemStack stack = new ItemStack(Exposure.Items.PHOTOGRAPH.get());
            CompoundTag tag = new CompoundTag();

            tag.putString(mode == Mode.EXPOSURES ? FrameData.ID : FrameData.TEXTURE, item);

            stack.setTag(tag);
            return new ItemAndStack<PhotographItem>(stack);
        }).toList());

        Collections.rotate(photographs, -clickedIndex);

        //TODO: PhotographScreen queries all photographs when opened. We certainly don't want this when 15000 exposures is viewed.
        PhotographScreen screen = new AlbumPhotographScreen(this, photographs);
        Minecraft.getInstance().setScreen(screen);
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(
                Exposure.SoundEvents.PHOTOGRAPH_RUSTLE.get(),
                Objects.requireNonNull(Minecraft.getInstance().player).level().getRandom().nextFloat() * 0.2f + 1.3f, 0.75f));
    }

    @Override
    public void onClose() {
        saveState();
        CatalogClient.clear();
        ExposureClient.getExposureStorage().clear();
        Packets.sendToServer(new CatalogClosedC2SP());
        super.onClose();
    }

    protected void saveState() {
        JsonObject obj = new JsonObject();
        obj.addProperty("mode", mode.getSerializedName());
        obj.addProperty("order", order.getSerializedName());
        obj.addProperty("sorting", sorting.getSerializedName());
        obj.addProperty("export_size", exportSize.getSerializedName());
        obj.addProperty("export_look", exportLook.getSerializedName());

        try {
            try (FileWriter writer = new FileWriter(stateFile)) {
                new GsonBuilder().setPrettyPrinting().create().toJson(obj, writer);
            }
        } catch (Exception e) {
            LogUtils.getLogger().error("Cannot save catalog state: " + e);
        }
    }

    protected void loadState() {
        try {
            if (!Files.exists(stateFile.toPath()) || Files.size(stateFile.toPath()) == 0)
                return;

            try (FileReader reader = new FileReader(stateFile)) {
                JsonObject obj = GsonHelper.parse(reader);
                this.mode = Mode.fromSerializedString(obj.get("mode").getAsString());
                this.order = Order.fromSerializedString(obj.get("order").getAsString());
                this.sorting = Sorting.fromSerializedString(obj.get("sorting").getAsString());
                this.exportSize = ExposureSize.byName(obj.get("export_size").getAsString());
                this.exportLook = ExposureLook.byName(obj.get("export_look").getAsString());
            } catch (Exception e) {
                throw e;
            }
        } catch (Exception e) {
            LogUtils.getLogger().error("Cannot load catalog state: " + e);
        }
    }
}
