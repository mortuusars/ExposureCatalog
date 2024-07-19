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
            CatalogClient.setThumbnail(packet.thumbnail());
        });
    }
}
