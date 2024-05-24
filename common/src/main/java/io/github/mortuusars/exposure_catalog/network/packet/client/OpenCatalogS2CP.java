package io.github.mortuusars.exposure_catalog.network.packet.client;

import io.github.mortuusars.exposure_catalog.network.PacketDirection;
import io.github.mortuusars.exposure_catalog.network.handler.ClientPacketsHandler;
import io.github.mortuusars.exposure_catalog.network.packet.IPacket;
import io.github.mortuusars.exposure_catalog.ExposureCatalog;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public final class OpenCatalogS2CP implements IPacket {
    public static final ResourceLocation ID = ExposureCatalog.resource("open_catalog");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public static OpenCatalogS2CP fromBuffer(FriendlyByteBuf buffer) {
        return new OpenCatalogS2CP();
    }

    @Override
    public boolean handle(PacketDirection direction, @Nullable Player player) {
        ClientPacketsHandler.openCatalog(this);
        return true;
    }
}
