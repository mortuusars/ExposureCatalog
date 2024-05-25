package io.github.mortuusars.exposure_catalog.network.packet.client;

import io.github.mortuusars.exposure_catalog.ExposureCatalog;
import io.github.mortuusars.exposure_catalog.network.PacketDirection;
import io.github.mortuusars.exposure_catalog.network.handler.ClientPacketsHandler;
import io.github.mortuusars.exposure_catalog.network.packet.IPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public final class NotifySendingStartS2CP implements IPacket {
    public static final ResourceLocation ID = ExposureCatalog.resource("notify_sending_start");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public static NotifySendingStartS2CP fromBuffer(FriendlyByteBuf buffer) {
        return new NotifySendingStartS2CP();
    }

    @Override
    public boolean handle(PacketDirection direction, @Nullable Player player) {
        ClientPacketsHandler.notifyLoadingStart(this);
        return true;
    }
}
