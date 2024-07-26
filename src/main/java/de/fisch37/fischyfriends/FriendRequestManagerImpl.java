package de.fisch37.fischyfriends;

import de.fisch37.fischyfriends.api.FriendRequest;
import de.fisch37.fischyfriends.api.FriendRequestManager;
import org.jetbrains.annotations.Nullable;

import java.util.*;

class FriendRequestManagerImpl implements FriendRequestManager {
    private final FriendsState state;
    // Hashing a function is probably not the best idea... Anyway
    private final Map<EventType, LinkedHashSet<EventHandler>> eventListeners = new HashMap<>();

    FriendRequestManagerImpl(FriendsState state) {
        this.state = state;
    }

    @Override
    public void addFriendRequest(FriendRequest request) {
        state.addFriendRequest(request);
        callEvent(EventType.CREATED, request);
    }

    @Override
    public void cancelFriendRequest(FriendRequest request) {
        state.removeFriendRequest(request);
        callEvent(EventType.CANCELLED, request);
    }

    @Override
    public void acceptFriendRequest(FriendRequest request) {
        state.removeFriendRequest(request);
        state.addFriendship(request.origin(), request.target());
        callEvent(EventType.ACCEPTED, request);
    }

    @Override
    public void denyFriendRequest(FriendRequest request) {
        state.removeFriendRequest(request);
        callEvent(EventType.DENIED, request);
    }

    private Set<EventHandler> getListenersForEvent(EventType eventType) {
        return eventListeners.computeIfAbsent(eventType, k -> new LinkedHashSet<>());
    }

    @Override
    public void registerListener(@Nullable EventType eventType, EventHandler listener) {
        if (eventType == null) {
            for (EventType t : EventType.values()) {
                registerListener(t, listener);
            }
        } else {
            getListenersForEvent(eventType).add(listener);
        }
    }

    @Override
    public void unregisterListener(@Nullable EventType eventType, EventHandler listener) {
        if (eventType == null) {
            for (EventType t : EventType.values()) {
                unregisterListener(t, listener);
            }
        } else {
            getListenersForEvent(eventType).remove(listener);
        }
    }

    private void callEvent(EventType eventType, FriendRequest request) {
        for (EventHandler listener : getListenersForEvent(eventType)) {
            listener.handle(eventType, request);
        }
    }
}
