package de.fisch37.fischyfriends.networking;

import de.fisch37.fischyfriends.api.CachedPlayer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

import java.util.ArrayList;
import java.util.List;

import static de.fisch37.fischyfriends.networking.PacketTypes.FRIEND_LIST;

record FriendList(List<CachedPlayer> friends) implements CustomPayload {
    private static final PacketCodec<RegistryByteBuf, FriendList> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.collection(ArrayList::new, CachedPlayer.PACKET_CODEC),
            FriendList::friends,
            FriendList::new
    );


    @Override
    public Id<? extends CustomPayload> getId() {
        return FRIEND_LIST;
    }

    static void register() {
        PayloadTypeRegistry.playS2C().register(FRIEND_LIST, PACKET_CODEC);
    }
}
