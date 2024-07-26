package de.fisch37.fischyfriends;

import de.fisch37.fischyfriends.api.CachedPlayer;
import de.fisch37.fischyfriends.api.FriendRequestManager;
import de.fisch37.fischyfriends.api.FriendsAPI;

import java.util.*;

import static de.fisch37.fischyfriends.FischyFriends.STATE;

class FriendsAPIImpl implements FriendsAPI {
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
        return STATE.removeFriendship(a, b);
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
}
