package io.github.mortuusars.exposure_catalog.neoforge;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.server.permission.PermissionAPI;

import java.util.function.Consumer;

public class PlatformHelperImpl {
    public static void openMenu(ServerPlayer serverPlayer, MenuProvider menuProvider, Consumer<RegistryFriendlyByteBuf> extraDataWriter) {
        serverPlayer.openMenu(menuProvider, extraDataWriter);
    }

    public static boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    public static boolean isInDevEnv() {
        return !FMLEnvironment.production;
    }

    public static boolean checkCatalogCommandPermission(ServerPlayer player) {
        return PermissionAPI.getPermission(player, NeoForgePermissions.CATALOG_COMMAND);
    }
}
