package de.fisch37.fischyfriends.api;

import java.util.UUID;

public record FriendRequest(UUID origin, UUID target) { }
