package io.github.mortuusars.exposure_catalog.data;

public class Thumbnail {
    protected int width, height;
    protected byte[] pixels;

    public Thumbnail(int width, int height, byte[] pixels) {
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
}
