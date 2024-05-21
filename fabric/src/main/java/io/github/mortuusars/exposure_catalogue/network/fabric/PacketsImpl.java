package io.github.mortuusars.exposure_catalogue.network.fabric;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure_catalogue.network.PacketDirection;
import io.github.mortuusars.exposure_catalogue.network.packet.IPacket;
import io.github.mortuusars.exposure_catalogue.network.packet.server.DeleteExposureC2SP;
import io.github.mortuusars.exposure_catalogue.network.packet.server.ExportExposureC2SP;
import io.github.mortuusars.exposure_catalogue.network.packet.server.QueryAllExposuresC2SP;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class PacketsImpl {
    @Nullable
    private static MinecraftServer server;

    public static void registerC2SPackets() {
        ServerPlayNetworking.registerGlobalReceiver(QueryAllExposuresC2SP.ID, new ServerHandler(QueryAllExposuresC2SP::fromBuffer));
        ServerPlayNetworking.registerGlobalReceiver(DeleteExposureC2SP.ID, new ServerHandler(DeleteExposureC2SP::fromBuffer));
        ServerPlayNetworking.registerGlobalReceiver(ExportExposureC2SP.ID, new ServerHandler(ExportExposureC2SP::fromBuffer));
    }

    public static void registerS2CPackets() {
        ClientPackets.registerS2CPackets();
    }

    public static void sendToServer(IPacket packet) {
        ClientPackets.sendToServer(packet);
    }

    public static void sendToClient(IPacket packet, ServerPlayer player) {
        ServerPlayNetworking.send(player, packet.getId(), packet.toBuffer(PacketByteBufs.create()));
    }

    public static void sendToAllClients(IPacket packet) {
        if (server == null) {
            LogUtils.getLogger().error("Cannot send a packet to all players. Server is not present.");
            return;
        }

        FriendlyByteBuf packetBuffer = packet.toBuffer(PacketByteBufs.create());
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            ServerPlayNetworking.send(player, packet.getId(), packetBuffer);
        }
    }

    public static void onServerStarting(MinecraftServer server) {
        // Store server to access from static context:
        PacketsImpl.server = server;
    }

    public static void onServerStopped(MinecraftServer server) {
        PacketsImpl.server = null;
    }

    private record ServerHandler(Function<FriendlyByteBuf, IPacket> decodeFunction) implements ServerPlayNetworking.PlayChannelHandler {
        @Override
        public void receive(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender) {
            IPacket packet = decodeFunction.apply(buf);
            packet.handle(PacketDirection.TO_SERVER, player);
        }
    }
}
