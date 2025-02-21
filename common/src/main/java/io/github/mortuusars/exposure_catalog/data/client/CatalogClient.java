package io.github.mortuusars.exposure_catalog.data.client;

import io.github.mortuusars.exposure_catalog.data.ExposureInfo;
import io.github.mortuusars.exposure_catalog.data.ExposureThumbnail;
import io.github.mortuusars.exposure_catalog.client.gui.screen.CatalogScreen;
import io.github.mortuusars.exposure_catalog.client.gui.screen.OverlayScreen;
import io.github.mortuusars.exposure_catalog.network.Packets;
import io.github.mortuusars.exposure_catalog.network.packet.serverbound.QueryExposureThumbnailC2SP;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CatalogClient {
    private static final Map<String, ExposureInfo> exposures = new HashMap<>();
    private static final Map<String, ExposureThumbnail> thumbnails = new HashMap<>();
    private static final List<String> queriedThumbnails = new ArrayList<>();

    public static Map<String, ExposureInfo> getExposures() {
        return exposures;
    }

    public static Map<String, ExposureThumbnail> getThumbnails() {
        return thumbnails;
    }

    public static Optional<ExposureThumbnail> getThumbnail(String exposureId) {
        @Nullable ExposureThumbnail thumbnail = thumbnails.get(exposureId);
        return Optional.ofNullable(thumbnail);
    }

    public static Optional<ExposureThumbnail> getOrQueryThumbnail(String exposureId) {
        @Nullable ExposureThumbnail thumbnail = thumbnails.get(exposureId);

        if (thumbnail == null) {
            if (!queriedThumbnails.contains(exposureId)) {
                queriedThumbnails.add(exposureId);
                Packets.sendToServer(new QueryExposureThumbnailC2SP(exposureId));
            }
            return Optional.empty();
        }

        return Optional.of(thumbnail);
    }

    public static void setThumbnail(String id, ExposureThumbnail thumbnail) {
        thumbnails.put(id, thumbnail);
        queriedThumbnails.remove(id);
    }

    public static void setExposures(List<ExposureInfo> exposureInfos) {
        exposures.clear();

        for (ExposureInfo data : exposureInfos) {
            exposures.put(data.id(), data);
        }

        getCatalogScreen().ifPresent(catalogScreen -> catalogScreen.onExposuresReceived(exposures));
    }

    public static Optional<CatalogScreen> getCatalogScreen() {
        Screen openedScreen = Minecraft.getInstance().screen instanceof OverlayScreen overlayScreen ?
                overlayScreen.getParent() : Minecraft.getInstance().screen;

        return openedScreen instanceof CatalogScreen catalogScreen ? Optional.of(catalogScreen) : Optional.empty();
    }

    public static void removeExposure(String exposureId) {
        exposures.remove(exposureId);
        thumbnails.remove(exposureId);
        queriedThumbnails.remove(exposureId);
    }

    public static void clear() {
        exposures.clear();
        thumbnails.clear();
        queriedThumbnails.clear();
    }
}
