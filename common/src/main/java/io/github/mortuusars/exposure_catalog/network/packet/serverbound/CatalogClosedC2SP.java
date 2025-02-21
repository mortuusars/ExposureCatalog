package io.github.mortuusars.exposure_catalog.network.packet.serverbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure_catalog.data.server.Catalog;
import io.github.mortuusars.exposure_catalog.network.packet.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class CatalogClosedC2SP implements Packet {
    public static final CatalogClosedC2SP INSTANCE = new CatalogClosedC2SP();

    public static final ResourceLocation ID = Exposure.resource("catalog_closed");
    public static final CustomPacketPayload.Type<CatalogClosedC2SP> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, CatalogClosedC2SP> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    private CatalogClosedC2SP() { }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        Catalog.removeWatchingPlayer(((ServerPlayer) player));
        return true;
    }
}