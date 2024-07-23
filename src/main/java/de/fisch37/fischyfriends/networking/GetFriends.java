package de.fisch37.fischyfriends.networking;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

import static de.fisch37.fischyfriends.networking.PacketTypes.GET_FRIENDS;

record GetFriends() implements CustomPayload {
    private static final PacketCodec<RegistryByteBuf, GetFriends> PACKET_CODEC =
            PacketCodec.of((b, n) -> {}, n -> new GetFriends());

    @Override
    public Id<? extends CustomPayload> getId() {
        return GET_FRIENDS;
    }

    static void register() {
        PayloadTypeRegistry.playC2S().register(GET_FRIENDS, PACKET_CODEC);
    }
}
