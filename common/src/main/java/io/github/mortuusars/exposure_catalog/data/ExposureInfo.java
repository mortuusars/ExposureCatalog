package io.github.mortuusars.exposure_catalog.data;

import io.github.mortuusars.exposure.data.ColorPalettes;
import io.github.mortuusars.exposure.world.level.storage.ExposureData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record ExposureInfo(String id, int width, int height, ResourceLocation paletteId, ExposureData.Tag tag) {
    public static final ExposureInfo EMPTY = new ExposureInfo("", 0, 0, ColorPalettes.DEFAULT.location(), ExposureData.Tag.EMPTY);

    public static final StreamCodec<FriendlyByteBuf, ExposureInfo> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ExposureInfo::id,
            ByteBufCodecs.VAR_INT, ExposureInfo::width,
            ByteBufCodecs.VAR_INT, ExposureInfo::height,
            ResourceLocation.STREAM_CODEC, ExposureInfo::paletteId,
            ExposureData.Tag.STREAM_CODEC, ExposureInfo::tag,
            ExposureInfo::new
    );

    public static ExposureInfo empty(String exposureId) {
        return new ExposureInfo(exposureId, 0, 0, ColorPalettes.DEFAULT.location(), ExposureData.Tag.EMPTY);
    }
}