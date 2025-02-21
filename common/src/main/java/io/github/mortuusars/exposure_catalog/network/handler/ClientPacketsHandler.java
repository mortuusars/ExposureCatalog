package io.github.mortuusars.exposure_catalog.network.handler;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure_catalog.data.client.CatalogClient;
import io.github.mortuusars.exposure_catalog.client.gui.screen.CatalogScreen;
import io.github.mortuusars.exposure_catalog.network.packet.clientbound.*;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;

public class ClientPacketsHandler {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void openCatalog(OpenCatalogS2CP packet) {
        Minecraft.getInstance().setScreen(new CatalogScreen());
    }

    public static void receiveExposureInfos(SendExposureInfosS2CP packet) {
        CatalogClient.setExposures(packet.exposures());
    }

    public static void receiveExposureThumbnail(SendExposureThumbnailS2CP packet) {
        CatalogClient.setThumbnail(packet.id(), packet.thumbnail());
    }
}