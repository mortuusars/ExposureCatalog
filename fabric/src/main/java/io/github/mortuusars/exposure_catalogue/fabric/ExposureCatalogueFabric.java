package io.github.mortuusars.exposure_catalogue.fabric;

import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry;
import io.github.mortuusars.exposure_catalogue.Config;
import io.github.mortuusars.exposure_catalogue.ExposureCatalogue;
import io.github.mortuusars.exposure_catalogue.network.fabric.PacketsImpl;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.fml.config.ModConfig;

public class ExposureCatalogueFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ExposureCatalogue.init();

        ForgeConfigRegistry.INSTANCE.register(ExposureCatalogue.ID, ModConfig.Type.COMMON, Config.Common.SPEC);
        ForgeConfigRegistry.INSTANCE.register(ExposureCatalogue.ID, ModConfig.Type.CLIENT, Config.Client.SPEC);

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(content -> {

        });

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(content -> {

        });

        ExposureCatalogue.Advancements.register();
        ExposureCatalogue.Stats.register();

        ServerLifecycleEvents.SERVER_STARTING.register(PacketsImpl::onServerStarting);
        ServerLifecycleEvents.SERVER_STOPPED.register(PacketsImpl::onServerStopped);

        PacketsImpl.registerC2SPackets();
    }
}
