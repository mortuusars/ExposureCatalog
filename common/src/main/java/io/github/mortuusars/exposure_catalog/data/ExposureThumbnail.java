package io.github.mortuusars.exposure_catalog.data;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record ExposureThumbnail(int width, int height, byte[] pixels, ResourceLocation paletteId) {
    public void toBuffer(FriendlyByteBuf buffer) {
        buffer.writeInt(width);
        buffer.writeInt(height);
        buffer.writeByteArray(pixels);
        buffer.writeResourceLocation(paletteId);
    }

    public static ExposureThumbnail fromBuffer(FriendlyByteBuf buffer) {
        return new ExposureThumbnail(buffer.readInt(), buffer.readInt(), buffer.readByteArray(),buffer.readResourceLocation());
    }
}