package io.github.mortuusars.exposure_catalogue.network.handler;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure_catalogue.gui.screen.CatalogueScreen;
import io.github.mortuusars.exposure_catalogue.gui.screen.OverlayScreen;
import io.github.mortuusars.exposure_catalogue.network.packet.client.OpenCatalogueS2CP;
import io.github.mortuusars.exposure_catalogue.network.packet.client.SendExposuresPartS2CP;
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

    public static void openCatalogue(OpenCatalogueS2CP packet) {
        executeOnMainThread(() -> {
            Minecraft.getInstance().setScreen(new CatalogueScreen());
        });
    }

    private static List<CompoundTag> exposuresMetadata = new ArrayList<>();
    private static int lastPart = -1;

    public static void receiveExposuresPart(SendExposuresPartS2CP packet) {
        executeOnMainThread(() -> {
            if (!(Minecraft.getInstance().screen instanceof CatalogueScreen catalogueScreen)) {
                LOGGER.warn("Received " + packet.getId() + " packet when not ");
            }

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

                if (openedScreen instanceof CatalogueScreen catalogueScreen) {
                    catalogueScreen.setExposures(exposuresMetadata);
                }
                else {
                    LOGGER.warn("Catalogue Screen is not opened. Received exposures would be discarded.");
                    exposuresMetadata = new ArrayList<>();
                    lastPart = -1;
                }
            }
        });
    }
}
