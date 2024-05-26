package io.github.mortuusars.exposure_catalog;

import com.google.common.base.Preconditions;
import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.ExposureServer;
import io.github.mortuusars.exposure_catalog.network.Packets;
import io.github.mortuusars.exposure_catalog.network.packet.client.NotifyPartSentS2CP;
import io.github.mortuusars.exposure_catalog.network.packet.client.NotifySendingStartS2CP;
import io.github.mortuusars.exposure_catalog.network.packet.client.SendExposuresCountS2CP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;

public class CatalogExposureSender {
    public static final int EXPOSURES_PER_TICK = 50;
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final Queue<ServerPlayer> playerQueue = new LinkedList<>();
    private static final Map<ServerPlayer, Integer> queriedPages = new HashMap<>();
    private static final Map<ServerPlayer, Integer> sentCounts = new HashMap<>();

    private static List<String> exposureIds = new ArrayList<>();
//    private static int sent = -1;

    public static void sendExposuresPage(ServerPlayer player, int page) {
        playerQueue.remove(player);

        playerQueue.add(player);
        queriedPages.put(player, page);
        sentCounts.put(player, -1);
    }

    public static void serverTick(MinecraftServer server) {
        @Nullable ServerPlayer player = playerQueue.peek();
        if (player == null)
            return;

        server.managedBlock(() -> {
            @Nullable Integer queriedPage = queriedPages.get(player);
            Preconditions.checkState(queriedPage != null);

            if (queriedPage < 0) {
                exposureIds = ExposureServer.getExposureStorage().getAllIds();
                Packets.sendToClient(new SendExposuresCountS2CP(exposureIds.size()), player);
                return true;
            }

            @Nullable Integer sent = sentCounts.get(player);
            Preconditions.checkState(sent != null);

            if (exposureIds.isEmpty()) {
                LOGGER.info("No exposures to send");
                stopSendingTo(player);
                return true;
            }

            int exposuresTotalCount = exposureIds.size();
            int pageStartIndex = Math.min(queriedPage * ExposureCatalog.EXPOSURES_PER_PAGE, exposuresTotalCount - 1);
            int pageEndIndexInclusive = Math.min(pageStartIndex + ExposureCatalog.EXPOSURES_PER_PAGE - 1, exposuresTotalCount - 1);
            int exposuresCountOnPage = (pageEndIndexInclusive + 1 - pageStartIndex);

            if (pageEndIndexInclusive - pageStartIndex <= 0) {
                LOGGER.info("Cannot send page '" + queriedPage + "'. Range contains no exposures.");
                stopSendingTo(player);
                return true;
            }

            if (sent == -1) {
                List<String> idsOnPage = exposureIds.subList(pageStartIndex, pageEndIndexInclusive + 1);
                Packets.sendToClient(new NotifySendingStartS2CP(queriedPage, idsOnPage), player);
                LOGGER.info("Sending " + exposuresCountOnPage  + " exposures of the " + queriedPage + " to " + player.getScoreboardName());
                sent = 0;
            }

            int partStartIndex = pageStartIndex + sent;
            int partEndIndex = Math.min(partStartIndex + EXPOSURES_PER_TICK, pageEndIndexInclusive + 1);

            List<String> sentIds = new ArrayList<>();

            for (int i = partStartIndex; i < partEndIndex; i++) {
                String exposureId = exposureIds.get(i);
                ExposureServer.getExposureStorage().getOrQuery(exposureId).ifPresent(data -> {
                    ExposureServer.getExposureSender().sendTo(player, exposureId, data);
                });
                sentIds.add(exposureId); // adding even if not sent. client will query it again.
            }

            LOGGER.info("Sent " + sentIds.size() + " exposures to " + player.getScoreboardName());
            Packets.sendToClient(new NotifyPartSentS2CP(queriedPage, ExposureCatalog.EXPOSURES_PER_PAGE, sentIds), player);

            sent += sentIds.size();
            sentCounts.put(player, sent);

            if (sent >= exposuresCountOnPage) {
                LOGGER.info("Finished sending " + sent + " exposures to " + player.getScoreboardName());
                stopSendingTo(player);
                Packets.sendToClient(new NotifyPartSentS2CP(queriedPage, -1, Collections.emptyList()), player);
            }

            return true;
        });
    }

    private static void stopSendingTo(@NotNull ServerPlayer player) {
        playerQueue.remove(player);
        queriedPages.remove(player);
        sentCounts.remove(player);
    }

//    public static void serverTick(MinecraftServer server) {
//        @Nullable ServerPlayer player = playerQueue.peek();
//        if (player == null)
//            return;
//
//        server.managedBlock(() -> {
//            if (sent == -1) {
//                exposureIds = ExposureServer.getExposureStorage().getAllIds();
////                if (exposureIds.size() > 5000) {
////                    player.displayClientMessage(Component.literal( exposureIds.size() + " exposures is too much to display. Only 5000 will be shown."), false);
////                }
////                sent = 0;
////                Packets.sendToClient(new NotifySendingStartS2CP(exposureIds), player);
////                LOGGER.info("Sending " + exposureIds.size() + " exposures to " + player.getScoreboardName());
//                return true;
//            }
//
//            int beforeSendingPart = sent;
//
//            List<String> sentIds = new ArrayList<>();
//
//            for (int i = sent; i < Math.min(beforeSendingPart + 40, exposureIds.size()); i++) {
//                String exposureId = exposureIds.get(i);
//                ExposureServer.getExposureStorage().getOrQuery(exposureId).ifPresent(data -> {
//                    ExposureServer.getExposureSender().sendTo(player, exposureId, data);
//                });
//                sentIds.add(exposureId); // adding even if not sent. client will query it again.
//                sent++;
//            }
//
//            int afterSendingPart = sent;
//            LOGGER.info("Sent " + (afterSendingPart - beforeSendingPart) + " exposures to " + player.getScoreboardName());
//            Packets.sendToClient(new NotifyPartSentS2CP(exposureIds.size(), sentIds), player);
//
//            if (sent >= exposureIds.size()) {
//                LOGGER.info("Finished sending " + sent + " exposures to " + player.getScoreboardName());
//                sent = -1;
//                playerQueue.remove();
//                Packets.sendToClient(new NotifyPartSentS2CP(-1, Collections.emptyList()), player);
//            }
//
//            return true;
//        });
//    }

    public static void sendExposuresCount(ServerPlayer player) {
        stopSendingTo(player);

        playerQueue.add(player);
        queriedPages.put(player, -1);


//        playerQueue.add()
//        exposureIds = ExposureServer.getExposureStorage().getAllIds();
//        Packets.sendToClient(new SendExposuresCountS2CP(exposureIds.size()), player);
    }
}
