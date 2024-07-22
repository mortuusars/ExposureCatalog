package io.github.mortuusars.exposure_catalog.forge.event;

import io.github.mortuusars.exposure_catalog.ExposureCatalog;
import io.github.mortuusars.exposure_catalog.command.CatalogCommand;
import io.github.mortuusars.exposure_catalog.forge.ForgePermissions;
import io.github.mortuusars.exposure_catalog.network.forge.PacketsImpl;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.server.permission.events.PermissionGatherEvent;

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
    }

    public static class ForgeBus {
        @SubscribeEvent
        public static void registerCommands(RegisterCommandsEvent event) {
            CatalogCommand.register(event.getDispatcher());
        }

        @SubscribeEvent
        public static void registerPermissions(PermissionGatherEvent.Nodes event) {
            event.addNodes(ForgePermissions.CATALOG_COMMAND);
        }
    }
}
