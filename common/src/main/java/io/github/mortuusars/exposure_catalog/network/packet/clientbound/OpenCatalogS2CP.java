package io.github.mortuusars.exposure_catalog.network.packet.clientbound;

import io.github.mortuusars.exposure_catalog.network.handler.ClientPacketsHandler;
import io.github.mortuusars.exposure_catalog.ExposureCatalog;
import io.github.mortuusars.exposure_catalog.network.packet.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public final class OpenCatalogS2CP implements Packet {
    public static final OpenCatalogS2CP INSTANCE = new OpenCatalogS2CP();

    public static final ResourceLocation ID = ExposureCatalog.resource("open_catalog");
    public static final CustomPacketPayload.Type<OpenCatalogS2CP> TYPE = new CustomPacketPayload.Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, OpenCatalogS2CP> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    private OpenCatalogS2CP() { }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        ClientPacketsHandler.openCatalog(this);
        return true;
    }
}
