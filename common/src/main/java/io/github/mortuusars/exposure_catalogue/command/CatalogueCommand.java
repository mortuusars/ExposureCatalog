package io.github.mortuusars.exposure_catalogue.command;

import com.google.common.base.Preconditions;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import io.github.mortuusars.exposure_catalogue.network.Packets;
import io.github.mortuusars.exposure_catalogue.network.packet.client.OpenCatalogueS2CP;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public class CatalogueCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("exposure")
                        .requires((commandSourceStack -> commandSourceStack.hasPermission(3)))
                        .then(Commands.literal("catalogue")
                                .executes(CatalogueCommand::openCatalogue)));
    }

    private static int openCatalogue(CommandContext<CommandSourceStack> context) {
        @Nullable ServerPlayer player = context.getSource().getPlayer();
        Preconditions.checkState(player != null, "This command should be executed by a player.");

        Packets.sendToClient(new OpenCatalogueS2CP(), player);
        return 0;
    }
}
