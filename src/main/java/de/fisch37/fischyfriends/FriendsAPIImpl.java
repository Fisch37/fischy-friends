package de.fisch37.fischyfriends;

import de.fisch37.fischyfriends.api.CachedPlayer;
import de.fisch37.fischyfriends.api.FriendRequestManager;
import de.fisch37.fischyfriends.api.FriendsAPI;

import java.util.*;
import java.util.function.Consumer;

import static de.fisch37.fischyfriends.FischyFriends.STATE;

class FriendsAPIImpl implements FriendsAPI {
    private final LinkedHashSet<Consumer<Friendship>> friendshipRemovedListeners = new LinkedHashSet<>();

    @Override
    public Set<UUID> getFriends(UUID player) {
        return new HashSet<>(STATE.getAllFriends(player));
    }

    @Override
    public CachedPlayer getPlayer(UUID uuid) {
        return STATE.getPlayer(uuid);
    }
    @Override
    public CachedPlayer getPlayer(String name) {
        return STATE.getPlayer(name);
    }

    @Override
    public void addFriendship(UUID a, UUID b) {
        STATE.addFriendship(a, b);
    }

    @Override
    public boolean removeFriendship(UUID a, UUID b) {
        boolean hasDeleted = STATE.removeFriendship(a, b);
        if (hasDeleted) {
            for (Consumer<Friendship> listener : friendshipRemovedListeners) {
                listener.accept(new Friendship(a,b));
            }
        }
        return hasDeleted;
    }

    @Override
    public boolean areFriends(UUID a, UUID b) {
        return STATE.areFriends(a, b);
    }

    @Override
    public Collection<CachedPlayer> getPlayers() {
        return new ArrayList<>(STATE.getPlayers());
    }

    @Override
    public FriendRequestManager getRequestManager() {
        return FischyFriends.requestManager;
    }

    @Override
    public void registerFriendRemovedListener(Consumer<Friendship> handler) {
        friendshipRemovedListeners.add(handler);
    }

    @Override
    public void unregisterFriendRemovedListener(Consumer<Friendship> handler) {
        friendshipRemovedListeners.remove(handler);
    }
}
