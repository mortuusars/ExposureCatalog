package io.github.mortuusars.exposure_catalog.network.packet.server;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure_catalog.ExposureCatalog;
import io.github.mortuusars.exposure_catalog.data.ExposureInfo;
import io.github.mortuusars.exposure_catalog.data.server.CatalogExposureSender;
import io.github.mortuusars.exposure_catalog.data.server.ExposuresCache;
import io.github.mortuusars.exposure_catalog.network.PacketDirection;
import io.github.mortuusars.exposure_catalog.network.packet.IPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record QueryExposuresC2SP(boolean forceRebuild) implements IPacket {
    public static final ResourceLocation ID = ExposureCatalog.resource("query_exposures");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public FriendlyByteBuf toBuffer(FriendlyByteBuf buffer) {
        buffer.writeBoolean(forceRebuild);
        return buffer;
    }

    public static QueryExposuresC2SP fromBuffer(FriendlyByteBuf buffer) {
        return new QueryExposuresC2SP(buffer.readBoolean());
    }

    @Override
    public boolean handle(PacketDirection direction, @Nullable Player player) {
        Preconditions.checkArgument(player instanceof ServerPlayer, "Player is required for " + ID + " packet");
        ServerPlayer serverPlayer = (ServerPlayer) player;

        Runnable onFinished = () -> {
            List<ExposureInfo> exposuresList = ExposuresCache.INSTANCE.getExposuresInfo().values().stream().toList();
            CatalogExposureSender.sendExposuresListToPlayer(exposuresList, serverPlayer);
        };

        if (forceRebuild)
            ExposuresCache.INSTANCE.rebuild(onFinished);
        else
            ExposuresCache.INSTANCE.buildIfNeeded(onFinished);

        return true;
    }
}
