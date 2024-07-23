package de.fisch37.fischyfriends.api;

import net.minecraft.nbt.NbtCompound;

import java.util.UUID;

public record CachedPlayer(UUID uuid, String name) {
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
