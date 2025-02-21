package io.github.mortuusars.exposure_catalog.fabric;

import io.github.mortuusars.exposure_catalog.ExposureCatalog;
import io.github.mortuusars.exposure_catalog.command.CatalogCommand;
import io.github.mortuusars.exposure_catalog.network.fabric.FabricC2SPackets;
import io.github.mortuusars.exposure_catalog.network.fabric.FabricS2CPackets;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class ExposureCatalogFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ExposureCatalog.init();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            CatalogCommand.register(dispatcher);
        });

        ExposureCatalog.Stats.register();

        FabricC2SPackets.register();
        FabricS2CPackets.register();
    }
}
