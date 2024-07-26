package de.fisch37.fischyfriends.api;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public interface FriendRequestManager {
    void addFriendRequest(FriendRequest request);

    void cancelFriendRequest(FriendRequest request);

    void acceptFriendRequest(FriendRequest request);

    void denyFriendRequest(FriendRequest request);

    List<FriendRequest> getOpenRequests();

    List<FriendRequest> getOpenRequestsForPlayer(UUID target);

    List<FriendRequest> getOpenRequestsByPlayer(UUID origin);

    void registerListener(@Nullable EventType eventType, EventHandler listener);

    void unregisterListener(@Nullable EventType eventType, EventHandler listener);

    enum EventType {
        CREATED, CANCELLED,
        ACCEPTED, DENIED
    }

    @FunctionalInterface
    interface EventHandler {
        void handle(EventType eventType, FriendRequest request);
    }
}
