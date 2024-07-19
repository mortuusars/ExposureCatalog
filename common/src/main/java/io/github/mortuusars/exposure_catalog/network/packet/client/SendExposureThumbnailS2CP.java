package io.github.mortuusars.exposure_catalog.network.packet.client;

import io.github.mortuusars.exposure_catalog.ExposureCatalog;
import io.github.mortuusars.exposure_catalog.data.ExposureThumbnail;
import io.github.mortuusars.exposure_catalog.network.PacketDirection;
import io.github.mortuusars.exposure_catalog.network.handler.ClientPacketsHandler;
import io.github.mortuusars.exposure_catalog.network.packet.IPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public record SendExposureThumbnailS2CP(ExposureThumbnail thumbnail) implements IPacket {
    public static final ResourceLocation ID = ExposureCatalog.resource("send_exposure_thumbnail");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public FriendlyByteBuf toBuffer(FriendlyByteBuf buffer) {
        thumbnail.toBuffer(buffer);
        return buffer;
    }

    public static SendExposureThumbnailS2CP fromBuffer(FriendlyByteBuf buffer) {
        return new SendExposureThumbnailS2CP(ExposureThumbnail.fromBuffer(buffer));
    }

    @Override
    public boolean handle(PacketDirection direction, @Nullable Player player) {
        ClientPacketsHandler.receiveExposureThumbnail(this);
        return true;
    }
}
