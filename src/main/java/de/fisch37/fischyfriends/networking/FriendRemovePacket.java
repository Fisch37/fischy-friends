package de.fisch37.fischyfriends.networking;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Uuids;

import java.util.UUID;

import static de.fisch37.fischyfriends.networking.PacketTypes.FRIEND_REMOVE_REQ;

public record FriendRemovePacket(UUID friend) implements CustomPayload {
    private static final PacketCodec<RegistryByteBuf, FriendRemovePacket> PACKET_CODEC = PacketCodec.tuple(
            Uuids.PACKET_CODEC, FriendRemovePacket::friend,
            FriendRemovePacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return FRIEND_REMOVE_REQ;
    }

    static void register() {
        PayloadTypeRegistry.playC2S().register(FRIEND_REMOVE_REQ, PACKET_CODEC);
    }
}
