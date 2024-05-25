package io.github.mortuusars.exposure_catalog.fabric;

//import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry;
import io.github.mortuusars.exposure_catalog.CatalogExposureSender;
import io.github.mortuusars.exposure_catalog.Config;
import io.github.mortuusars.exposure_catalog.ExposureCatalog;
import io.github.mortuusars.exposure_catalog.command.CatalogCommand;
import io.github.mortuusars.exposure_catalog.network.fabric.PacketsImpl;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

//import net.minecraftforge.fml.config.ModConfig;

public class ExposureCatalogFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ExposureCatalog.init();

//        ForgeConfigRegistry.INSTANCE.register(ExposureCatalog.ID, ModConfig.Type.COMMON, Config.Common.SPEC);
//        ForgeConfigRegistry.INSTANCE.register(ExposureCatalog.ID, ModConfig.Type.CLIENT, Config.Client.SPEC);

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            CatalogCommand.register(dispatcher);
        });

        ExposureCatalog.Advancements.register();
        ExposureCatalog.Stats.register();

        ServerTickEvents.END_SERVER_TICK.register(CatalogExposureSender::serverTick);
        ServerLifecycleEvents.SERVER_STARTING.register(PacketsImpl::onServerStarting);
        ServerLifecycleEvents.SERVER_STOPPED.register(PacketsImpl::onServerStopped);

        PacketsImpl.registerC2SPackets();
    }
}
