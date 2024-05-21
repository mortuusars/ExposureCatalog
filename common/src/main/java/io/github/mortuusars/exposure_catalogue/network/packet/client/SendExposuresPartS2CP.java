package io.github.mortuusars.exposure_catalogue.network.packet.client;

import io.github.mortuusars.exposure_catalogue.ExposureCatalogue;
import io.github.mortuusars.exposure_catalogue.network.PacketDirection;
import io.github.mortuusars.exposure_catalogue.network.handler.ClientPacketsHandler;
import io.github.mortuusars.exposure_catalogue.network.packet.IPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public record SendExposuresPartS2CP(int partIndex, boolean isLastPart, List<CompoundTag> exposuresMetadataList) implements IPacket {
    public static final ResourceLocation ID = ExposureCatalogue.resource("send_exposures_part");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public FriendlyByteBuf toBuffer(FriendlyByteBuf buffer) {
        buffer.writeInt(partIndex);
        buffer.writeBoolean(isLastPart);
        buffer.writeInt(exposuresMetadataList.size());
        for (CompoundTag tag : exposuresMetadataList) {
            buffer.writeNbt(tag);
        }
        return buffer;
    }

    public static SendExposuresPartS2CP fromBuffer(FriendlyByteBuf buffer) {
        int part = buffer.readInt();
        boolean isLastPart = buffer.readBoolean();
        int count = buffer.readInt();
        List<CompoundTag> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(buffer.readAnySizeNbt());
        }
        return new SendExposuresPartS2CP(part, isLastPart, list);
    }

    @Override
    public boolean handle(PacketDirection direction, @Nullable Player player) {
        ClientPacketsHandler.receiveExposuresPart(this);
        return true;
    }
}
