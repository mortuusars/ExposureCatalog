package io.github.mortuusars.exposure_catalogue.network.packet.server;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.ExposureServer;
import io.github.mortuusars.exposure_catalogue.ExposureCatalogue;
import io.github.mortuusars.exposure_catalogue.network.PacketDirection;
import io.github.mortuusars.exposure_catalogue.network.Packets;
import io.github.mortuusars.exposure_catalogue.network.packet.IPacket;
import io.github.mortuusars.exposure_catalogue.network.packet.client.SendIdsS2CP;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class QueryAllExposureIdsC2SP implements IPacket {
    public static final ResourceLocation ID = ExposureCatalogue.resource("query_all_exposure_ids");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public FriendlyByteBuf toBuffer(FriendlyByteBuf buffer) {
        return buffer;
    }

    public static QueryAllExposureIdsC2SP fromBuffer(FriendlyByteBuf buffer) {
        return new QueryAllExposureIdsC2SP();
    }

    @Override
    public boolean handle(PacketDirection direction, @Nullable Player player) {
        Preconditions.checkArgument(player instanceof ServerPlayer, "Player is required for QueryAllExposureIds packet");
        List<String> ids = ExposureServer.getExposureStorage().getAllIds();

        Packets.sendToClient(new SendIdsS2CP(ids), ((ServerPlayer) player));

        return true;
    }
}
