package io.github.mortuusars.exposure_catalog.fabric;

import io.github.mortuusars.exposure_catalog.ExposureCatalogClient;
import io.github.mortuusars.exposure_catalog.network.fabric.FabricS2CPacketHandler;
import net.fabricmc.api.ClientModInitializer;

public class ExposureCatalogFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ExposureCatalogClient.init();
        FabricS2CPacketHandler.register();
    }
}
