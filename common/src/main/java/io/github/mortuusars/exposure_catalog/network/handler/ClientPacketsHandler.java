package io.github.mortuusars.exposure_catalog.network.handler;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure_catalog.data.client.CatalogReceiver;
import io.github.mortuusars.exposure_catalog.data.client.CatalogClient;
import io.github.mortuusars.exposure_catalog.gui.screen.CatalogScreen;
import io.github.mortuusars.exposure_catalog.gui.screen.OverlayScreen;
import io.github.mortuusars.exposure_catalog.network.packet.client.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.slf4j.Logger;

import java.util.Optional;

public class ClientPacketsHandler {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static void executeOnMainThread(Runnable runnable) {
        Minecraft.getInstance().execute(runnable);
    }

    public static void openCatalog(OpenCatalogS2CP packet) {
        executeOnMainThread(() -> {
            Minecraft.getInstance().setScreen(new CatalogScreen());
        });
    }

    public static void receiveExposuresPart(SendExposuresDataPartS2CP packet) {
        executeOnMainThread(() -> {
            CatalogReceiver.receivePart(packet.exposures(), packet.partIndex(), packet.isLastPart());
        });
    }

    public static void receiveExposureThumbnail(SendExposureThumbnailS2CP packet) {
        executeOnMainThread(() -> {
            CatalogClient.setThumbnail(packet.exposureId(), packet.thumbnail());
        });
    }

    public static void notifySendingStart(NotifySendingStartS2CP packet) {
//        executeOnMainThread(() ->
//                getCatalogScreen().ifPresent(screen ->
//                        screen.onReceivingStartNotification(packet.page(), packet.exposureIds())));
    }

    public static void notifyPartSent(NotifyPartSentS2CP packet) {
//        executeOnMainThread(() ->
//                getCatalogScreen().ifPresent(screen ->
//                        screen.onPartSent(packet.page(), packet.exposureIds(), packet.allCount())));
    }

    public static void onExposuresCountReceived(SendExposuresCountS2CP packet) {
//        executeOnMainThread(() ->
//                getCatalogScreen().ifPresent(screen ->
//                        screen.onTotalCountReceived(packet.count())));
    }

    private static Optional<CatalogScreen> getCatalogScreen() {
        Screen openedScreen = Minecraft.getInstance().screen instanceof OverlayScreen overlayScreen ?
                overlayScreen.getParent() : Minecraft.getInstance().screen;

        return openedScreen instanceof CatalogScreen catalogScreen ? Optional.of(catalogScreen) : Optional.empty();
    }
}
