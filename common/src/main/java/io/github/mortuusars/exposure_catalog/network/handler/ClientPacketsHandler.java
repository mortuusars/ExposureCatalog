package io.github.mortuusars.exposure_catalog.network.handler;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure_catalog.gui.screen.CatalogScreen;
import io.github.mortuusars.exposure_catalog.gui.screen.OverlayScreen;
import io.github.mortuusars.exposure_catalog.network.packet.client.OpenCatalogS2CP;
import io.github.mortuusars.exposure_catalog.network.packet.client.SendExposuresPartS2CP;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

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

    private static List<CompoundTag> exposuresMetadata = new ArrayList<>();
    private static int lastPart = -1;

    public static void receiveExposuresPart(SendExposuresPartS2CP packet) {
        executeOnMainThread(() -> {
            if (packet.partIndex() == 0) {
                exposuresMetadata = new ArrayList<>();
                lastPart = 0;
            } else if (packet.partIndex() < lastPart) {
                LOGGER.warn("Incorrect order of exposures metadata parts detected. Exposures might not populate correctly.");
            }

            exposuresMetadata.addAll(packet.exposuresMetadataList());
            lastPart = packet.partIndex();

            if (packet.isLastPart()) {
                Screen openedScreen = Minecraft.getInstance().screen instanceof OverlayScreen overlayScreen ?
                        overlayScreen.getParent() : Minecraft.getInstance().screen;

                if (openedScreen instanceof CatalogScreen catalogScreen) {
                    catalogScreen.setExposures(exposuresMetadata);
                }
                else {
                    LOGGER.warn("Catalog Screen is not opened. Received exposures would be discarded.");
                    exposuresMetadata = new ArrayList<>();
                    lastPart = -1;
                }
            }
        });
    }
}
