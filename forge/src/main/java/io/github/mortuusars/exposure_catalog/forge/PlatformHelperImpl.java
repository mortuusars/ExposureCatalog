package io.github.mortuusars.exposure_catalog.forge;

import io.github.mortuusars.exposure_catalog.ExposureCatalog;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.server.permission.PermissionAPI;
import net.minecraftforge.server.permission.nodes.PermissionNode;
import net.minecraftforge.server.permission.nodes.PermissionTypes;

import java.util.function.Consumer;

public class PlatformHelperImpl {
    public static void openMenu(ServerPlayer serverPlayer, MenuProvider menuProvider, Consumer<FriendlyByteBuf> extraDataWriter) {
        NetworkHooks.openScreen(serverPlayer, menuProvider, extraDataWriter);
    }

    public static boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    public static final PermissionNode<Boolean> PERMISSION = new PermissionNode<>(ExposureCatalog.ID, "command.catalog", PermissionTypes.BOOLEAN, (player, playerUUID, context) -> false);

    public static boolean checkPermission(ServerPlayer player, String permission) {
        return PermissionAPI.getPermission(player, PERMISSION);
    }
}
