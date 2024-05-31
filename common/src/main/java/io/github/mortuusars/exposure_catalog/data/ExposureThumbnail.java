package io.github.mortuusars.exposure_catalog.data;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.material.MapColor;

import java.util.Arrays;
import java.util.Objects;

public class ExposureThumbnail {
    protected int width, height;
    protected byte[] pixels;

    public ExposureThumbnail(int width, int height, byte[] pixels) {
        this.width = width;
        this.height = height;
        this.pixels = pixels;
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

    public boolean isEmpty() {
        return width <= 0 || height <= 0 || pixels.length == 0;
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
        buffer.writeInt(width);
        buffer.writeInt(height);
        buffer.writeByteArray(pixels);
    }

    public static ExposureThumbnail fromBuffer(FriendlyByteBuf buffer) {
        return new ExposureThumbnail(buffer.readInt(), buffer.readInt(), buffer.readByteArray());
    }
}
