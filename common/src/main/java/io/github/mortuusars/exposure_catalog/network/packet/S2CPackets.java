package io.github.mortuusars.exposure_catalog.network.packet;

import io.github.mortuusars.exposure_catalog.network.packet.clientbound.OpenCatalogS2CP;
import io.github.mortuusars.exposure_catalog.network.packet.clientbound.SendExposureInfosS2CP;
import io.github.mortuusars.exposure_catalog.network.packet.clientbound.SendExposureThumbnailS2CP;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.List;

public class S2CPackets {
    public static List<CustomPacketPayload.TypeAndCodec<? extends FriendlyByteBuf, ? extends CustomPacketPayload>> getDefinitions() {
        return List.of(
            new CustomPacketPayload.TypeAndCodec<>(OpenCatalogS2CP.TYPE, OpenCatalogS2CP.STREAM_CODEC),
                new CustomPacketPayload.TypeAndCodec<>(SendExposureInfosS2CP.TYPE, SendExposureInfosS2CP.STREAM_CODEC),
            new CustomPacketPayload.TypeAndCodec<>(SendExposureThumbnailS2CP.TYPE, SendExposureThumbnailS2CP.STREAM_CODEC)
        );
    }
}