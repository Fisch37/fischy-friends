package de.fisch37.fischyfriends.networking;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Uuids;

import java.util.UUID;

import static de.fisch37.fischyfriends.networking.PacketTypes.*;

public record AcceptFriendRequest(UUID targetOrOrigin) implements CustomPayload {
    private static final PacketCodec<RegistryByteBuf, AcceptFriendRequest> PACKET_CODEC = PacketCodec.tuple(
            Uuids.PACKET_CODEC, AcceptFriendRequest::targetOrOrigin,
            AcceptFriendRequest::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return FRIEND_REQUEST_ACCEPT;
    }

    static void register() {
        PayloadTypeRegistry.playC2S().register(FRIEND_REQUEST_ACCEPT, PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(FRIEND_REQUEST_ACCEPT, PACKET_CODEC);
    }
}
