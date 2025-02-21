package io.github.mortuusars.exposure_catalog;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

public class Permissions {
    public final static String CATALOG_COMMAND = "exposure_catalog.command.catalog";

    public static boolean canUseCatalog(CommandSourceStack stack) {
        try {
            return canUseCatalog(stack.getPlayerOrException());
        } catch (CommandSyntaxException e) {
            return false;
        }
    }

    public static boolean canUseCatalog(ServerPlayer player) {
        //noinspection ConstantValue
        return player.hasPermissions(3) || PlatformHelper.checkCatalogCommandPermission(player);
    }
}