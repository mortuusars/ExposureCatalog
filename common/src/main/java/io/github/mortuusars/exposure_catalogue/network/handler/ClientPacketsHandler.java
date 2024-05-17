package io.github.mortuusars.exposure_catalogue.network.handler;

import io.github.mortuusars.exposure_catalogue.gui.screen.CatalogueScreen;
import io.github.mortuusars.exposure_catalogue.network.packet.client.OpenCatalogueS2CP;
import io.github.mortuusars.exposure_catalogue.network.packet.client.SendIdsS2CP;
import net.minecraft.client.Minecraft;

public class ClientPacketsHandler {
    private static void executeOnMainThread(Runnable runnable) {
        Minecraft.getInstance().execute(runnable);
    }

    public static void openCatalogue(OpenCatalogueS2CP packet) {
        executeOnMainThread(() -> {
            Minecraft.getInstance().setScreen(new CatalogueScreen());
        });
    }

    public static void populateIds(SendIdsS2CP packet) {
        executeOnMainThread(() -> {
            if (Minecraft.getInstance().screen instanceof CatalogueScreen catalogueScreen) {
                catalogueScreen.setExposureIds(packet.ids());
            }
        });
    }
}
