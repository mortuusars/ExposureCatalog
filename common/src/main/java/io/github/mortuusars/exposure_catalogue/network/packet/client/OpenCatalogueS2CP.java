package io.github.mortuusars.exposure_catalogue.network.packet.client;

import io.github.mortuusars.exposure_catalogue.network.PacketDirection;
import io.github.mortuusars.exposure_catalogue.network.handler.ClientPacketsHandler;
import io.github.mortuusars.exposure_catalogue.network.packet.IPacket;
import io.github.mortuusars.exposure_catalogue.ExposureCatalogue;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public final class OpenCatalogueS2CP implements IPacket {
    public static final ResourceLocation ID = ExposureCatalogue.resource("open_catalogue");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public FriendlyByteBuf toBuffer(FriendlyByteBuf buffer) {
        return buffer;
    }

    public static OpenCatalogueS2CP fromBuffer(FriendlyByteBuf buffer) {
        return new OpenCatalogueS2CP();
    }

    @Override
    public boolean handle(PacketDirection direction, @Nullable Player player) {
        ClientPacketsHandler.openCatalogue(this);
        return true;
    }
}
