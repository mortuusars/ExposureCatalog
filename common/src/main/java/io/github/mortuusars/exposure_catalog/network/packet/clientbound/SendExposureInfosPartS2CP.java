package io.github.mortuusars.exposure_catalog.network.packet.clientbound;

import io.github.mortuusars.exposure_catalog.ExposureCatalog;
import io.github.mortuusars.exposure_catalog.data.ExposureInfo;
import io.github.mortuusars.exposure_catalog.network.PacketDirection;
import io.github.mortuusars.exposure_catalog.network.handler.ClientPacketsHandler;
import io.github.mortuusars.exposure_catalog.network.packet.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public record SendExposureInfosPartS2CP(int partIndex, boolean isLastPart, List<ExposureInfo> exposures) implements Packet {
    public static final ResourceLocation ID = ExposureCatalog.resource("send_exposures_data_part");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public FriendlyByteBuf toBuffer(FriendlyByteBuf buffer) {
        buffer.writeInt(partIndex);
        buffer.writeBoolean(isLastPart);
        buffer.writeInt(exposures.size());
        for (ExposureInfo data : exposures) {
            data.toBuffer(buffer);
        }
        return buffer;
    }

    public static SendExposureInfosPartS2CP fromBuffer(FriendlyByteBuf buffer) {
        int part = buffer.readInt();
        boolean isLastPart = buffer.readBoolean();
        int count = buffer.readInt();
        List<ExposureInfo> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(ExposureInfo.fromBuffer(buffer));
        }
        return new SendExposureInfosPartS2CP(part, isLastPart, list);
    }

    @Override
    public boolean handle(PacketDirection direction, @Nullable Player player) {
        ClientPacketsHandler.receiveExposureInfosPart(this);
        return true;
    }
}
