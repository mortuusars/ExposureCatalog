package io.github.mortuusars.exposure_catalog.network.packet.serverbound;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure_catalog.ExposureCatalog;
import io.github.mortuusars.exposure_catalog.data.server.Catalog;
import io.github.mortuusars.exposure_catalog.network.PacketDirection;
import io.github.mortuusars.exposure_catalog.network.packet.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public record CatalogClosedC2SP() implements Packet {
    public static final ResourceLocation ID = ExposureCatalog.resource("catalog_closed");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public static CatalogClosedC2SP fromBuffer(FriendlyByteBuf buffer) {
        return new CatalogClosedC2SP();
    }

    @Override
    public boolean handle(PacketDirection direction, @Nullable Player player) {
        Preconditions.checkArgument(player instanceof ServerPlayer, "Player is required for " + ID + " packet");
        ServerPlayer serverPlayer = (ServerPlayer) player;

        serverPlayer.server.execute(() -> Catalog.removeWatchingPlayer(serverPlayer));

        return true;
    }
}
