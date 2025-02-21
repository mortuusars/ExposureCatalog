package io.github.mortuusars.exposure_catalog.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.mortuusars.exposure_catalog.Permissions;
import io.github.mortuusars.exposure_catalog.network.Packets;
import io.github.mortuusars.exposure_catalog.network.packet.clientbound.OpenCatalogS2CP;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class CatalogCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("exposure")
                        .then(Commands.literal("catalog")
                                .requires(Permissions::canUseCatalog)
                                .executes(CatalogCommand::openCatalog)));
    }

    private static int openCatalog(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Packets.sendToClient(OpenCatalogS2CP.INSTANCE, context.getSource().getPlayerOrException());
        return 0;
    }
}
