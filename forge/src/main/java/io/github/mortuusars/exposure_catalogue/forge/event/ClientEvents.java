package io.github.mortuusars.exposure_catalogue.forge.event;

import io.github.mortuusars.exposure_catalogue.ExposureCatalogueClient;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientEvents {
    public static class ModBus {
        @SubscribeEvent
        public static void clientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                ExposureCatalogueClient.init();
            });
        }
    }

    public static class ForgeBus {
    }
}
