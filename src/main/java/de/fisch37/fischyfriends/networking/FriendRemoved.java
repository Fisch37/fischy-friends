package de.fisch37.fischyfriends.networking;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Uuids;

import java.util.UUID;

import static de.fisch37.fischyfriends.networking.PacketTypes.FRIEND_REMOVED;

public record FriendRemoved(UUID friend) implements CustomPayload {
    private static final PacketCodec<RegistryByteBuf, FriendRemoved> PACKET_CODEC = PacketCodec.tuple(
            Uuids.PACKET_CODEC, FriendRemoved::friend,
            FriendRemoved::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return FRIEND_REMOVED;
    }

    static void register() {
        PayloadTypeRegistry.playS2C().register(FRIEND_REMOVED, PACKET_CODEC);
    }
}
