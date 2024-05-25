package io.github.mortuusars.exposure_catalog;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.ExposureServer;
import io.github.mortuusars.exposure_catalog.network.Packets;
import io.github.mortuusars.exposure_catalog.network.packet.client.NotifySendingStartS2CP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;

public class CatalogExposureSender {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final Queue<ServerPlayer> playerQueue = new LinkedList<>();

    private static List<String> exposureIds = new ArrayList<>();
    private static int sent;

    public static void sendAllExposures(ServerPlayer player) {
        playerQueue.add(player);
        Packets.sendToClient(new NotifySendingStartS2CP(), player);
    }

    public static void serverTick(MinecraftServer server) {
        @Nullable ServerPlayer player = playerQueue.peek();
        if (player == null)
            return;

        if (sent == -1) {
            exposureIds = ExposureServer.getExposureStorage().getAllIds();
            sent = 0;
            LOGGER.info("Sending " + exposureIds.size() + " exposures to " + player.getScoreboardName());
            return;
        }

        int beforeSendingPart = sent;

        for (int i = sent; i < Math.min(100, exposureIds.size()); i++) {
            String exposureId = exposureIds.get(i);
            ExposureServer.getExposureStorage().getOrQuery(exposureId).ifPresent(data -> {
                ExposureServer.getExposureSender().sendTo(player, exposureId, data);
            });
            sent++;
        }

        int afterSendingPart = sent;
        LOGGER.info("Sent " + (afterSendingPart - beforeSendingPart) + " exposures to " + player.getScoreboardName());

        if (sent >= exposureIds.size()) {
            LOGGER.info("Finished sending " + sent + " exposures to " + player.getScoreboardName());
            sent = -1;
            playerQueue.remove();
        }
    }
}
