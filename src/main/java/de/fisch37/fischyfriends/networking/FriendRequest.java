package de.fisch37.fischyfriends.networking;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Uuids;

import java.util.UUID;

import static de.fisch37.fischyfriends.networking.PacketTypes.FRIEND_REQUEST;

public record FriendRequest(UUID targetOrOrigin) implements CustomPayload {
    private static final PacketCodec<RegistryByteBuf, FriendRequest> PACKET_CODEC = PacketCodec.tuple(
            Uuids.PACKET_CODEC, FriendRequest::targetOrOrigin,
            FriendRequest::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return FRIEND_REQUEST;
    }

    static void register() {
        PayloadTypeRegistry.playC2S().register(FRIEND_REQUEST, PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(FRIEND_REQUEST, PACKET_CODEC);
    }
}
