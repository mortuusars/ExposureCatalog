package io.github.mortuusars.exposure_catalog.forge.event;

import io.github.mortuusars.exposure_catalog.ExposureCatalog;
import io.github.mortuusars.exposure_catalog.command.CatalogCommand;
import io.github.mortuusars.exposure_catalog.network.forge.PacketsImpl;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class CommonEvents {
    public static class ModBus {
        @SubscribeEvent
        public static void commonSetup(FMLCommonSetupEvent event) {
            event.enqueueWork(() -> {
                PacketsImpl.register();
                ExposureCatalog.Advancements.register();
                ExposureCatalog.Stats.register();
            });
        }

//        @SubscribeEvent
//        public static void onCreativeTabsBuild(BuildCreativeModeTabContentsEvent event) {
//            if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
//
//            }
//
//            if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
//            }
//        }
    }

    public static class ForgeBus {
        @SubscribeEvent
        public static void registerCommands(RegisterCommandsEvent event) {
            CatalogCommand.register(event.getDispatcher());
        }

//        @SubscribeEvent
//        public static void onServerTick(TickEvent.ServerTickEvent event) {
//            if (event.phase == TickEvent.Phase.END) {
//                CatalogExposureSender.serverTick(event.getServer());
//            }
//        }
    }
}
