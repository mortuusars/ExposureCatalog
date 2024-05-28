package io.github.mortuusars.exposure_catalog.network.packet.server;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure_catalog.ExposureCatalog;
import io.github.mortuusars.exposure_catalog.data.ExposureThumbnail;
import io.github.mortuusars.exposure_catalog.data.server.Catalog;
import io.github.mortuusars.exposure_catalog.network.PacketDirection;
import io.github.mortuusars.exposure_catalog.network.packet.IPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public record QueryThumbnailC2SP(String exposureId) implements IPacket {
    public static final ResourceLocation ID = ExposureCatalog.resource("query_thumbnail");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public FriendlyByteBuf toBuffer(FriendlyByteBuf buffer) {
        buffer.writeUtf(exposureId);
        return buffer;
    }

    public static QueryThumbnailC2SP fromBuffer(FriendlyByteBuf buffer) {
        return new QueryThumbnailC2SP(buffer.readUtf());
    }

    @Override
    public boolean handle(PacketDirection direction, @Nullable Player player) {
        Preconditions.checkArgument(player instanceof ServerPlayer, "Player is required for " + ID + " packet");
        ServerPlayer serverPlayer = (ServerPlayer) player;

        serverPlayer.server.execute(() -> {
            @Nullable ExposureThumbnail thumbnail = Catalog.getCache().getThumbnails().get(exposureId);

        });

        return true;
    }
}
