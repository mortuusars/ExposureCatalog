package io.github.mortuusars.exposure_catalog.network.fabric;

import io.github.mortuusars.exposure_catalog.network.PacketDirection;
import io.github.mortuusars.exposure_catalog.network.packet.IPacket;
import io.github.mortuusars.exposure_catalog.network.packet.client.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;

import java.util.function.Function;

public class ClientPackets {
    public static void registerS2CPackets() {
        ClientPlayNetworking.registerGlobalReceiver(OpenCatalogS2CP.ID, new ClientHandler(OpenCatalogS2CP::fromBuffer));
        ClientPlayNetworking.registerGlobalReceiver(SendExposuresDataPartS2CP.ID, new ClientHandler(SendExposuresDataPartS2CP::fromBuffer));
        ClientPlayNetworking.registerGlobalReceiver(NotifySendingStartS2CP.ID, new ClientHandler(NotifySendingStartS2CP::fromBuffer));
        ClientPlayNetworking.registerGlobalReceiver(NotifyPartSentS2CP.ID, new ClientHandler(NotifyPartSentS2CP::fromBuffer));
        ClientPlayNetworking.registerGlobalReceiver(SendExposuresCountS2CP.ID, new ClientHandler(SendExposuresCountS2CP::fromBuffer));
        ClientPlayNetworking.registerGlobalReceiver(SendExposureThumbnailS2CP.ID, new ClientHandler(SendExposureThumbnailS2CP::fromBuffer));
    }

    public static void sendToServer(IPacket packet) {
        ClientPlayNetworking.send(packet.getId(), packet.toBuffer(PacketByteBufs.create()));
    }

    private record ClientHandler(Function<FriendlyByteBuf, IPacket> decodeFunction) implements ClientPlayNetworking.PlayChannelHandler {
        @Override
        public void receive(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender responseSender) {
            IPacket packet = decodeFunction.apply(buf);
            packet.handle(PacketDirection.TO_CLIENT, null);
        }
    }
}
