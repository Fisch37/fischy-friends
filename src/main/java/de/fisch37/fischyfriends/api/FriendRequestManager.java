package de.fisch37.fischyfriends.api;

import org.jetbrains.annotations.Nullable;

public interface FriendRequestManager {
    void addFriendRequest(FriendRequest request);

    void cancelFriendRequest(FriendRequest request);

    void acceptFriendRequest(FriendRequest request);

    void denyFriendRequest(FriendRequest request);

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
