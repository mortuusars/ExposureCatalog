package io.github.mortuusars.exposure_catalog.data;

import io.github.mortuusars.exposure.render.image.IImage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.material.MapColor;

import java.util.Arrays;
import java.util.Objects;

public class ExposureThumbnail implements IImage {
    private final String id;
    protected int width;
    protected int height;
    protected byte[] pixels;

    public ExposureThumbnail(String id, int width, int height, byte[] pixels) {
        this.id = id;
        this.width = width;
        this.height = height;
        this.pixels = pixels;
    }

    @Override
    public String getImageId() {
        return id;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public byte[] getPixels() {
        return pixels;
    }

    public byte getPixel(int x, int y) {
        return pixels[y * width + x];
    }

    public int getPixelABGR(int x, int y) {
        return MapColor.getColorFromPackedId(getPixel(x, y));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExposureThumbnail thumbnail = (ExposureThumbnail) o;
        return width == thumbnail.width && height == thumbnail.height && Arrays.equals(pixels, thumbnail.pixels);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(width, height);
        result = 31 * result + Arrays.hashCode(pixels);
        return result;
    }

    public void toBuffer(FriendlyByteBuf buffer) {
        buffer.writeUtf(id);
        buffer.writeInt(width);
        buffer.writeInt(height);
        buffer.writeByteArray(pixels);
    }

    public static ExposureThumbnail fromBuffer(FriendlyByteBuf buffer) {
        return new ExposureThumbnail(buffer.readUtf(), buffer.readInt(), buffer.readInt(), buffer.readByteArray());
    }
}
