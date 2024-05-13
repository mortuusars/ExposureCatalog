package io.github.mortuusars.exposure_catalogue.network.packet.client;

import io.github.mortuusars.exposure_catalogue.ExposureCatalogue;
import io.github.mortuusars.exposure_catalogue.network.PacketDirection;
import io.github.mortuusars.exposure_catalogue.network.handler.ClientPacketsHandler;
import io.github.mortuusars.exposure_catalogue.network.packet.IPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public record SendIdsS2CP(List<String> ids) implements IPacket {
    public static final ResourceLocation ID = ExposureCatalogue.resource("send_ids");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public FriendlyByteBuf toBuffer(FriendlyByteBuf buffer) {
        buffer.writeInt(ids.size());
        for (String id : ids) {
            buffer.writeUtf(id);
        }
        return buffer;
    }

    public static SendIdsS2CP fromBuffer(FriendlyByteBuf buffer) {
        int count = buffer.readInt();
        List<String> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(buffer.readUtf());
        }
        return new SendIdsS2CP(list);
    }

    @Override
    public boolean handle(PacketDirection direction, @Nullable Player player) {
        ClientPacketsHandler.populateIds(this);
        return true;
    }
}
