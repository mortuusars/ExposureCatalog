package io.github.mortuusars.exposure_catalog.network.packet.clientbound;

import io.github.mortuusars.exposure_catalog.ExposureCatalog;
import io.github.mortuusars.exposure_catalog.data.ExposureInfo;
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

import java.util.List;

public record SendExposureInfosS2CP(List<ExposureInfo> exposures) implements Packet {
    public static final ResourceLocation ID = ExposureCatalog.resource("send_exposure_infos");
    public static final Type<SendExposureInfosS2CP> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, SendExposureInfosS2CP> STREAM_CODEC = StreamCodec.composite(
            ExposureInfo.STREAM_CODEC.apply(ByteBufCodecs.list()), SendExposureInfosS2CP::exposures,
            SendExposureInfosS2CP::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        ClientPacketsHandler.receiveExposureInfos(this);
        return true;
    }
}