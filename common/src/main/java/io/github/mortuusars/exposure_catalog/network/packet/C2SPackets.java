package io.github.mortuusars.exposure_catalog.network.packet;

import io.github.mortuusars.exposure_catalog.network.packet.serverbound.CatalogClosedC2SP;
import io.github.mortuusars.exposure_catalog.network.packet.serverbound.DeleteExposureC2SP;
import io.github.mortuusars.exposure_catalog.network.packet.serverbound.QueryExposureThumbnailC2SP;
import io.github.mortuusars.exposure_catalog.network.packet.serverbound.QueryExposuresC2SP;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.List;

public class C2SPackets {
    public static List<CustomPacketPayload.TypeAndCodec<? extends FriendlyByteBuf, ? extends CustomPacketPayload>> getDefinitions() {
        return List.of(
                new CustomPacketPayload.TypeAndCodec<>(CatalogClosedC2SP.TYPE, CatalogClosedC2SP.STREAM_CODEC),
                new CustomPacketPayload.TypeAndCodec<>(QueryExposureThumbnailC2SP.TYPE, QueryExposureThumbnailC2SP.STREAM_CODEC),
                new CustomPacketPayload.TypeAndCodec<>(QueryExposuresC2SP.TYPE, QueryExposuresC2SP.STREAM_CODEC),
                new CustomPacketPayload.TypeAndCodec<>(DeleteExposureC2SP.TYPE, DeleteExposureC2SP.STREAM_CODEC)
        );
    }
}