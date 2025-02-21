package io.github.mortuusars.exposure_catalog.network.packet.clientbound;

import io.github.mortuusars.exposure_catalog.ExposureCatalog;
import io.github.mortuusars.exposure_catalog.data.ExposureThumbnail;
import io.github.mortuusars.exposure_catalog.network.handler.ClientPacketsHandler;
import io.github.mortuusars.exposure_catalog.network.packet.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public record SendExposureThumbnailS2CP(String id, ExposureThumbnail thumbnail) implements Packet {
    public static final ResourceLocation ID = ExposureCatalog.resource("send_exposure_thumbnail");
    public static final Type<SendExposureThumbnailS2CP> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, SendExposureThumbnailS2CP> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, SendExposureThumbnailS2CP::id,
            ExposureThumbnail.STREAM_CODEC, SendExposureThumbnailS2CP::thumbnail,
            SendExposureThumbnailS2CP::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        ClientPacketsHandler.receiveExposureThumbnail(this);
        return true;
    }
}