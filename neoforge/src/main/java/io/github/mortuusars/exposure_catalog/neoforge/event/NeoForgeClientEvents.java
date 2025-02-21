package io.github.mortuusars.exposure_catalog.neoforge.event;

import io.github.mortuusars.exposure_catalog.neoforge.ExposureCatalogNeoForgeClient;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

public class NeoForgeClientEvents {
    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(ExposureCatalogNeoForgeClient::init);
    }
}