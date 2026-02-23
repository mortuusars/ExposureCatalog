package io.github.mortuusars.exposure_catalog.network.handler;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure_catalog.data.client.CatalogReceiver;
import io.github.mortuusars.exposure_catalog.data.client.CatalogClient;
import io.github.mortuusars.exposure_catalog.gui.screen.CatalogScreen;
import io.github.mortuusars.exposure_catalog.network.packet.clientbound.*;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;

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

    public static void receiveExposureInfosPart(SendExposureInfosPartS2CP packet) {
        executeOnMainThread(() -> {
            CatalogReceiver.receivePart(packet.exposures(), packet.partIndex(), packet.isLastPart());
        });
    }

    public static void receiveExposureThumbnail(SendExposureThumbnailS2CP packet) {
        executeOnMainThread(() -> {
            CatalogClient.setThumbnail(packet.id(), packet.thumbnail());
        });
    }
}
