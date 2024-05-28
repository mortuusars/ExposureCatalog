package io.github.mortuusars.exposure_catalog.network.packet.server;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure_catalog.ExposureCatalog;
import io.github.mortuusars.exposure_catalog.data.server.Catalog;
import io.github.mortuusars.exposure_catalog.network.PacketDirection;
import io.github.mortuusars.exposure_catalog.network.packet.IPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public record DeleteExposureC2SP(String exposureId) implements IPacket {
    public static final ResourceLocation ID = ExposureCatalog.resource("delete_exposure");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public FriendlyByteBuf toBuffer(FriendlyByteBuf buffer) {
        buffer.writeUtf(exposureId);
        return buffer;
    }

    public static DeleteExposureC2SP fromBuffer(FriendlyByteBuf buffer) {
        return new DeleteExposureC2SP(buffer.readUtf());
    }

    @Override
    public boolean handle(PacketDirection direction, @Nullable Player player) {
        Preconditions.checkArgument(player instanceof ServerPlayer, "Player is required for " + ID + " packet");
        ServerPlayer serverPlayer = (ServerPlayer) player;

        if (!player.hasPermissions(3))
            return true;

        serverPlayer.server.execute(() -> {
            boolean deleted = Catalog.deleteExposure(exposureId);
            String message = deleted ?
                    "gui.exposure_catalog.catalog.exposure_deleted" :
                    "gui.exposure_catalog.catalog.exposure_delete_failed";
            player.displayClientMessage(Component.translatable(message, exposureId), false);
        });

        return true;
    }
}
