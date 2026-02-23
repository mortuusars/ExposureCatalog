package io.github.mortuusars.exposure_catalog.data;

import io.github.mortuusars.exposure.data.ColorPalettes;
import io.github.mortuusars.exposure.world.level.storage.ExposureData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record ExposureInfo(String id, int width, int height, ResourceLocation palette, ExposureData.Tag tag) {
    public static ExposureInfo empty(String exposureId) {
        return new ExposureInfo(exposureId, 0, 0, ColorPalettes.DEFAULT.location(), ExposureData.Tag.EMPTY);
    }

    public boolean isEmpty() {
        return width() == 0 && height() == 0 && tag().unixTimestamp() == 0L;
    }

    public FriendlyByteBuf toBuffer(FriendlyByteBuf buffer) {
        buffer.writeUtf(id);
        buffer.writeInt(width);
        buffer.writeInt(height);
        buffer.writeResourceLocation(palette);
        tag.toPacket(buffer);
        return buffer;
    }

    public static ExposureInfo fromBuffer(FriendlyByteBuf buffer) {
        return new ExposureInfo(
              buffer.readUtf(),
              buffer.readInt(),
              buffer.readInt(),
              buffer.readResourceLocation(),
              ExposureData.Tag.fromPacket(buffer));
    }
}
