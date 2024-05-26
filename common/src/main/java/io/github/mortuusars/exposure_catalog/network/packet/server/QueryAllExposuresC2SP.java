package io.github.mortuusars.exposure_catalog.network.packet.server;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure_catalog.CatalogExposureSender;
import io.github.mortuusars.exposure_catalog.ExposureCatalog;
import io.github.mortuusars.exposure_catalog.network.PacketDirection;
import io.github.mortuusars.exposure_catalog.network.packet.IPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public record QueryAllExposuresC2SP(int page) implements IPacket {
    public static final ResourceLocation ID = ExposureCatalog.resource("query_all_exposures");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public FriendlyByteBuf toBuffer(FriendlyByteBuf buffer) {
        buffer.writeInt(page);
        return buffer;
    }

    public static QueryAllExposuresC2SP fromBuffer(FriendlyByteBuf buffer) {
        return new QueryAllExposuresC2SP(buffer.readInt());
    }

    @Override
    public boolean handle(PacketDirection direction, @Nullable Player player) {
        Preconditions.checkArgument(player instanceof ServerPlayer, "Player is required for " + ID + " packet");

        ServerPlayer serverPlayer = (ServerPlayer) player;
        if (page < 0) {
            serverPlayer.serverLevel().getServer().execute(() ->
                    CatalogExposureSender.sendExposuresCount(serverPlayer));
        }
        else {
            serverPlayer.serverLevel().getServer().execute(() ->
                    CatalogExposureSender.sendExposuresPage(serverPlayer, page));
        }

//        new Thread(() -> {
//            try {
//                List<String> ids = ExposureServer.getExposureStorage().getAllIds();
//                sendExposures(ids, ((ServerPlayer) player));
//            } catch (Exception e) {
//                LogUtils.getLogger().error("Failed to load and send all exposures: " + e);
//            }
//        }).start();

        return true;
    }

//    private void sendExposures(List<String> exposureIds, ServerPlayer player) {
//        List<CompoundTag> list = new ArrayList<>();
//        int size = 0;
//        int part = 0;
//
//        Packets.sendToClient(new NotifySendingStartS2CP(), player);
//
//        for (String exposureId : exposureIds) {
//            if (size > 1_000_000) {
//                Packets.sendToClient(new SendExposuresPartS2CP(part, false, list), player);
//                list = new ArrayList<>();
//                size = 0;
//                part++;
//            }
//
//            CompoundTag metadataTag = ExposureServer.getExposureStorage().getOrQuery(exposureId).map(data -> {
//                CompoundTag tag = data.getProperties().copy();
//                tag.putString("Id", exposureId);
//                tag.putInt("Width", data.getWidth());
//                tag.putInt("Height", data.getHeight());
//                return tag;
//            }).orElseGet(() -> {
//                CompoundTag tag = new CompoundTag();
//                tag.putString("Id", exposureId);
//                return tag;
//            });
//
//            size += metadataTag.sizeInBytes();
//            list.add(metadataTag);
//        }
//
//        Packets.sendToClient(new SendExposuresPartS2CP(part, true, list), player);
//    }
}
