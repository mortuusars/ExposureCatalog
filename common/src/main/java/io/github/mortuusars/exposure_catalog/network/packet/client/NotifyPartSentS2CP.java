package io.github.mortuusars.exposure_catalog.network.packet.client;

import io.github.mortuusars.exposure_catalog.ExposureCatalog;
import io.github.mortuusars.exposure_catalog.network.PacketDirection;
import io.github.mortuusars.exposure_catalog.network.handler.ClientPacketsHandler;
import io.github.mortuusars.exposure_catalog.network.packet.IPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public record NotifyPartSentS2CP(int page, int allCount, List<String> exposureIds) implements IPacket {
    public static final ResourceLocation ID = ExposureCatalog.resource("notify_part_sent");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public FriendlyByteBuf toBuffer(FriendlyByteBuf buffer) {
        buffer.writeInt(page);
        buffer.writeInt(allCount);
        buffer.writeInt(exposureIds.size());
        for (int i = 0; i < exposureIds.size(); i++) {
            buffer.writeUtf(exposureIds.get(i));
        }
        return buffer;
    }

    public static NotifyPartSentS2CP fromBuffer(FriendlyByteBuf buffer) {
        int page = buffer.readInt();
        int all = buffer.readInt();
        int count = buffer.readInt();
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ids.add(buffer.readUtf());
        }
        return new NotifyPartSentS2CP(page, all, ids);
    }

    @Override
    public boolean handle(PacketDirection direction, @Nullable Player player) {
        ClientPacketsHandler.notifyPartSent(this);
        return true;
    }
}
