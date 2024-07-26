package de.fisch37.fischyfriends.api;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.UUID;

public interface FriendRequestManager {
    void addFriendRequest(FriendRequest request);

    void cancelFriendRequest(FriendRequest request);

    void acceptFriendRequest(FriendRequest request);

    void denyFriendRequest(FriendRequest request);

    Collection<FriendRequest> getOpenRequests();

    Collection<FriendRequest> getOpenRequestsForPlayer(UUID target);

    Collection<FriendRequest> getOpenRequestsByPlayer(UUID origin);

    void registerListener(@NotNull EventType eventType, @NotNull EventHandler listener);

    void unregisterListener(@NotNull EventType eventType, @NotNull EventHandler listener);

    default void registerGlobalListener(@NotNull GlobalEventHandler listener) {
        for (EventType type : EventType.values()) {
            registerListener(type, request -> listener.handle(type, request));
        }
    }

    default void unregisterGlobalListener(@NotNull GlobalEventHandler listener) {
        for (EventType type : EventType.values()) {
            unregisterListener(type, request -> listener.handle(type, request));
        }
    }

    enum EventType {
        CREATED, CANCELLED,
        ACCEPTED, DENIED
    }

    @FunctionalInterface
    interface EventHandler {
        void handle(FriendRequest request);
    }

    @FunctionalInterface
    interface GlobalEventHandler {
        void handle(EventType eventType, FriendRequest request);
    }
}
