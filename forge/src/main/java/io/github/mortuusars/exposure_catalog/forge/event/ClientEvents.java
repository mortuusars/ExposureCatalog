package io.github.mortuusars.exposure_catalog.forge.event;

import io.github.mortuusars.exposure_catalog.ExposureCatalogClient;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientEvents {
    public static class ModBus {
        @SubscribeEvent
        public static void clientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                ExposureCatalogClient.init();
            });
        }
    }

    public static class ForgeBus {
    }
}
