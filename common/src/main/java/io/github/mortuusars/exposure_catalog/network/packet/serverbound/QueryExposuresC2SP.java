package io.github.mortuusars.exposure_catalog.network.packet.serverbound;

import io.github.mortuusars.exposure_catalog.ExposureCatalog;
import io.github.mortuusars.exposure_catalog.Permissions;
import io.github.mortuusars.exposure_catalog.data.server.Catalog;
import io.github.mortuusars.exposure_catalog.network.packet.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public record QueryExposuresC2SP(boolean rebuildCache) implements Packet {
    public static final ResourceLocation ID = ExposureCatalog.resource("query_exposures");
    public static final CustomPacketPayload.Type<QueryExposuresC2SP> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, QueryExposuresC2SP> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, QueryExposuresC2SP::rebuildCache,
            QueryExposuresC2SP::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        ServerPlayer serverPlayer = ((ServerPlayer) player);
        if (!Permissions.canUseCatalog(serverPlayer)) {
            return false;
        }

        Catalog.queryExposures(serverPlayer, rebuildCache);
        return true;
    }
}