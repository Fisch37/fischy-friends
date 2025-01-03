package de.fisch37.fischyfriends;

import de.fisch37.fischyfriends.api.FriendRequest;
import de.fisch37.fischyfriends.api.FriendRequestManager;
import org.jetbrains.annotations.NotNull;

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

    @Override
    public List<FriendRequest> getOpenRequests() {
        return state.getRequests();
    }

    @Override
    public List<FriendRequest> getOpenRequestsForPlayer(UUID target) {
        return new LinkedList<>(state.getRequestsTo(target));
    }

    @Override
    public List<FriendRequest> getOpenRequestsByPlayer(UUID origin) {
        return new LinkedList<>(state.getRequestsBy(origin));
    }

    private Set<EventHandler> getListenersForEvent(EventType eventType) {
        return eventListeners.computeIfAbsent(eventType, k -> new LinkedHashSet<>());
    }

    @Override
    public void registerListener(@NotNull EventType eventType, @NotNull EventHandler listener) {
        getListenersForEvent(eventType).add(listener);
    }

    @Override
    public void unregisterListener(@NotNull EventType eventType, @NotNull EventHandler listener) {
        getListenersForEvent(eventType).remove(listener);
    }

    private void callEvent(EventType eventType, FriendRequest request) {
        for (EventHandler listener : getListenersForEvent(eventType)) {
            listener.handle(request);
        }
    }
}
