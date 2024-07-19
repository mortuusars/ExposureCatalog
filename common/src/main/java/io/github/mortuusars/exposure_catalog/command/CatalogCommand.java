package io.github.mortuusars.exposure_catalog.command;

import com.google.common.base.Preconditions;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import io.github.mortuusars.exposure_catalog.Permissions;
import io.github.mortuusars.exposure_catalog.PlatformHelper;
import io.github.mortuusars.exposure_catalog.network.Packets;
import io.github.mortuusars.exposure_catalog.network.packet.client.OpenCatalogS2CP;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public class CatalogCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        //noinspection ConstantValue
        dispatcher.register(
                Commands.literal("exposure")
                        .requires(stack ->
                                stack.hasPermission(3)
                                        || PlatformHelper.checkPermission(stack.getPlayer(), Permissions.CATALOG_COMMAND))
                        .then(Commands.literal("catalog")
                                .executes(CatalogCommand::openCatalog)));
    }

    private static int openCatalog(CommandContext<CommandSourceStack> context) {
        @Nullable ServerPlayer player = context.getSource().getPlayer();
        Preconditions.checkState(player != null, "This command should be executed by a player.");

        Packets.sendToClient(new OpenCatalogS2CP(), player);
        return 0;
    }
}
