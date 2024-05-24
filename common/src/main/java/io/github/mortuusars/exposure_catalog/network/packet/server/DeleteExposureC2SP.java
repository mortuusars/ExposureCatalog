package io.github.mortuusars.exposure_catalog.network.packet.server;

import com.google.common.base.Preconditions;
import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.ExposureServer;
import io.github.mortuusars.exposure_catalog.ExposureCatalog;
import io.github.mortuusars.exposure_catalog.mixin.ServersideExposureStorageAccessor;
import io.github.mortuusars.exposure_catalog.network.PacketDirection;
import io.github.mortuusars.exposure_catalog.network.packet.IPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public record DeleteExposureC2SP(String exposureId) implements IPacket {
    public static final ResourceLocation ID = ExposureCatalog.resource("delete_exposure");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public FriendlyByteBuf toBuffer(FriendlyByteBuf buffer) {
        buffer.writeUtf(exposureId);
        return buffer;
    }

    public static DeleteExposureC2SP fromBuffer(FriendlyByteBuf buffer) {
        return new DeleteExposureC2SP(buffer.readUtf());
    }

    @Override
    public boolean handle(PacketDirection direction, @Nullable Player player) {
        Preconditions.checkArgument(player instanceof ServerPlayer, "Player is required for " + ID + " packet");
        Logger logger = LogUtils.getLogger();

        if (!player.hasPermissions(3))
            return true;

        try {
            Path path = ((ServersideExposureStorageAccessor) ExposureServer.getExposureStorage()).getWorldPathSupplier().get()
                    .resolve("data/exposures/" + exposureId + ".dat");
            if (Files.deleteIfExists(path))
                logger.info(exposureId + " deleted.");
            return true;
        } catch (IOException e) {
            logger.error("Deleting exposure failed: " + e);
            return false;
        }
    }
}
