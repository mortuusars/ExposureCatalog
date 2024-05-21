package io.github.mortuusars.exposure_catalogue.network.packet.server;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.ExposureServer;
import io.github.mortuusars.exposure_catalogue.ExposureCatalogue;
import io.github.mortuusars.exposure_catalogue.network.PacketDirection;
import io.github.mortuusars.exposure_catalogue.network.Packets;
import io.github.mortuusars.exposure_catalogue.network.packet.IPacket;
import io.github.mortuusars.exposure_catalogue.network.packet.client.SendExposuresPartS2CP;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class QueryAllExposuresC2SP implements IPacket {
    public static final ResourceLocation ID = ExposureCatalogue.resource("query_all_exposures");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public static QueryAllExposuresC2SP fromBuffer(FriendlyByteBuf buffer) {
        return new QueryAllExposuresC2SP();
    }

    @Override
    public boolean handle(PacketDirection direction, @Nullable Player player) {
        Preconditions.checkArgument(player instanceof ServerPlayer, "Player is required for " + ID + " packet");

        List<String> ids = ExposureServer.getExposureStorage().getAllIds();
        sendExposures(ids, ((ServerPlayer) player));

        return true;
    }

    private void sendExposures(List<String> exposureIds, ServerPlayer player) {
        List<CompoundTag> list = new ArrayList<>();
        int size = 0;
        int part = 0;

        for (String exposureId : exposureIds) {
            if (size > 7_500_000) {
                Packets.sendToClient(new SendExposuresPartS2CP(part, false, list), player);
                list = new ArrayList<>();
                size = 0;
                part++;
            }

            CompoundTag metadataTag = ExposureServer.getExposureStorage().getOrQuery(exposureId).map(data -> {
                CompoundTag tag = data.getProperties().copy();
                tag.putString("Id", exposureId);
                tag.putInt("Width", data.getWidth());
                tag.putInt("Height", data.getHeight());
                return tag;
            }).orElseGet(() -> {
                CompoundTag tag = new CompoundTag();
                tag.putString("Id", exposureId);
                return tag;
            });

            size += metadataTag.sizeInBytes();
            list.add(metadataTag);
        }

        Packets.sendToClient(new SendExposuresPartS2CP(part, true, list), player);
    }
}
