package io.github.mortuusars.exposure_catalog.forge;

import io.github.mortuusars.exposure_catalog.ExposureCatalog;
import net.minecraftforge.server.permission.nodes.PermissionNode;
import net.minecraftforge.server.permission.nodes.PermissionTypes;

public class ForgePermissions {
    public static final PermissionNode<Boolean> CATALOG_COMMAND = new PermissionNode<>(ExposureCatalog.ID,
            "command.catalog", PermissionTypes.BOOLEAN, (player, playerUUID, context) -> false);
}
