package de.fisch37.fischyfriends.api;

import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public interface FriendsAPI {
    Set<UUID> getFriends(UUID player);
    default Collection<UUID> getFriends(ServerPlayerEntity player) {
        return getFriends(player.getUuid());
    }

    @Nullable CachedPlayer getPlayer(UUID uuid);
    @Nullable CachedPlayer getPlayer(String name);

    void addFriendship(UUID a, UUID b);

    boolean removeFriendship(UUID a, UUID b);

    boolean areFriends(UUID a, UUID b);

    Collection<CachedPlayer> getPlayers();

    FriendRequestManager getRequestManager();

    void registerFriendRemovedListener(Consumer<Friendship> handler);

    void unregisterFriendRemovedListener(Consumer<Friendship> handler);

    record Friendship(@NotNull UUID a, @NotNull UUID b) {
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Friendship other))
                return false;
            return (other.a.equals(a) && other.b.equals(b))
                    || (other.b.equals(a) && other.a.equals(b));
        }
    }
}
