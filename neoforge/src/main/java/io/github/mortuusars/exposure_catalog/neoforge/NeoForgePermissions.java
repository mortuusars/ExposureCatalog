package io.github.mortuusars.exposure_catalog.neoforge;

import io.github.mortuusars.exposure_catalog.ExposureCatalog;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;
import net.neoforged.neoforge.server.permission.nodes.PermissionTypes;

public class NeoForgePermissions {
    public static final PermissionNode<Boolean> CATALOG_COMMAND = new PermissionNode<>(ExposureCatalog.ID,
            "command.catalog", PermissionTypes.BOOLEAN, (player, playerUUID, context) -> false);
}