package de.fisch37.fischyfriends.api;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Uuids;

import java.util.UUID;

public record CachedPlayer(UUID uuid, String name) {
    public static final PacketCodec<RegistryByteBuf, CachedPlayer> PACKET_CODEC = PacketCodec.tuple(
            Uuids.PACKET_CODEC, CachedPlayer::uuid,
            PacketCodecs.STRING, CachedPlayer::name,
            CachedPlayer::new
    );

    public static CachedPlayer fromNbt(NbtCompound nbt) {
        return new CachedPlayer(
                nbt.getUuid("uuid"),
                nbt.getString("name")
        );
    }

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putUuid("uuid", uuid);
        nbt.putString("name", name);
        return nbt;
    }
}
