package de.fisch37.fischyfriends.api;

import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

public interface FriendsAPI {
    Set<UUID> getFriends(UUID player);
    default Collection<UUID> getFriends(ServerPlayerEntity player) {
        return getFriends(player.getUuid());
    }

    void addFriendship(UUID a, UUID b);

    boolean removeFriendship(UUID a, UUID b);

    boolean areFriends(UUID a, UUID b);

    Collection<CachedPlayer> getPlayers();

    FriendRequestManager getRequestManager();
}
