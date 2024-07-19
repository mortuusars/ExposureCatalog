package io.github.mortuusars.exposure_catalog.fabric;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.level.ServerPlayer;

public class LuckPermsIntegration {
    public static boolean checkPermission(ServerPlayer player, String permission) {
        return Permissions.check(player, permission);
    }
}
