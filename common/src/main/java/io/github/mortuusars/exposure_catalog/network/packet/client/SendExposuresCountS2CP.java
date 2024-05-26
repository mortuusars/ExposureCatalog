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

public record SendExposuresCountS2CP(int count) implements IPacket {
    public static final ResourceLocation ID = ExposureCatalog.resource("send_exposures_count");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public FriendlyByteBuf toBuffer(FriendlyByteBuf buffer) {
        buffer.writeInt(count);
        return buffer;
    }

    public static SendExposuresCountS2CP fromBuffer(FriendlyByteBuf buffer) {
        int count = buffer.readInt();
        return new SendExposuresCountS2CP(count);
    }

    @Override
    public boolean handle(PacketDirection direction, @Nullable Player player) {
        ClientPacketsHandler.onExposuresCountReceived(this);
        return true;
    }
}
