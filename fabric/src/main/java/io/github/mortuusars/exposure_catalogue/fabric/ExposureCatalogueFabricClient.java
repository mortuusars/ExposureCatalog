package io.github.mortuusars.exposure_catalogue.fabric;

import io.github.mortuusars.exposure_catalogue.ExposureCatalogueClient;
import io.github.mortuusars.exposure_catalogue.network.fabric.PacketsImpl;
import net.fabricmc.api.ClientModInitializer;

public class ExposureCatalogueFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ExposureCatalogueClient.init();
        PacketsImpl.registerS2CPackets();
    }
}
