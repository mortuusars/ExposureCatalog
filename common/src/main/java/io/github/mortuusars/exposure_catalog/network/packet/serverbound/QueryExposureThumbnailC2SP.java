package io.github.mortuusars.exposure_catalog.network.packet.serverbound;

import io.github.mortuusars.exposure_catalog.ExposureCatalog;
import io.github.mortuusars.exposure_catalog.data.ExposureThumbnail;
import io.github.mortuusars.exposure_catalog.data.server.Catalog;
import io.github.mortuusars.exposure_catalog.network.Packets;
import io.github.mortuusars.exposure_catalog.network.packet.Packet;
import io.github.mortuusars.exposure_catalog.network.packet.clientbound.SendExposureThumbnailS2CP;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record QueryExposureThumbnailC2SP(String id) implements Packet {
    public static final ResourceLocation ID = ExposureCatalog.resource("query_exposure_thumbnail");
    public static final CustomPacketPayload.Type<QueryExposureThumbnailC2SP> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, QueryExposureThumbnailC2SP> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, QueryExposureThumbnailC2SP::id,
            QueryExposureThumbnailC2SP::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        ServerPlayer serverPlayer = (ServerPlayer) player;

        @Nullable ExposureThumbnail thumbnail = Catalog.getCache().getThumbnails().get(id);
        if (thumbnail != null) {
            Packets.sendToClient(new SendExposureThumbnailS2CP(id, thumbnail), serverPlayer);
        }

        return true;
    }
}