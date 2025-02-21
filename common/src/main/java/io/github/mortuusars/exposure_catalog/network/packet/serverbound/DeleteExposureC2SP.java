package io.github.mortuusars.exposure_catalog.network.packet.serverbound;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure_catalog.Permissions;
import io.github.mortuusars.exposure_catalog.data.server.Catalog;
import io.github.mortuusars.exposure_catalog.network.packet.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public record DeleteExposureC2SP(String id) implements Packet {
    public static final ResourceLocation ID = Exposure.resource("delete_exposure");
    public static final CustomPacketPayload.Type<DeleteExposureC2SP> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, DeleteExposureC2SP> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, DeleteExposureC2SP::id,
            DeleteExposureC2SP::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        Preconditions.checkArgument(player instanceof ServerPlayer, "Player is required for " + ID + " packet");
        ServerPlayer serverPlayer = (ServerPlayer) player;

        if (!Permissions.canUseCatalog(serverPlayer)) {
            return true;
        }

        boolean deleted = Catalog.deleteExposure(id);
        String message = deleted ?
                "gui.exposure_catalog.catalog.exposure_deleted" :
                "gui.exposure_catalog.catalog.exposure_delete_failed";
        player.displayClientMessage(Component.translatable(message, id), false);

        return true;
    }
}