package de.fisch37.fischyfriends;

import de.fisch37.fischyfriends.api.CachedPlayer;
import de.fisch37.fischyfriends.api.FriendRequest;
import de.fisch37.fischyfriends.api.FriendRequestManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

import static de.fisch37.fischyfriends.FischyFriends.getAPI;
import static de.fisch37.fischyfriends.api.FriendRequestManager.EventType;

abstract class ChatEventHandlers {

    private static PlayerManager playerManager;

    static void registerEventHandlers(MinecraftServer server) {
        playerManager = server.getPlayerManager();
        registerListener(EventType.CREATED, ChatEventHandlers::newRequest);
        registerListener(EventType.CANCELLED, ChatEventHandlers::requestCancelled);
        registerListener(EventType.DENIED, ChatEventHandlers::requestDenied);
        registerListener(EventType.ACCEPTED, ChatEventHandlers::requestAccepted);
    }

    private static void registerListener(EventType type, FriendRequestManager.EventHandler listener) {
        getAPI().getRequestManager().registerListener(type, listener);
    }

    private static void sendIfAvailable(UUID target, Text message) {
        ServerPlayerEntity player = playerManager.getPlayer(target);
        if (player != null)
            player.sendMessage(
                    Text.literal("[").formatted(Formatting.BLUE)
                            .append(Text.literal("FF").formatted(Formatting.WHITE))
                            .append(Text.literal("] ").formatted(Formatting.BLUE))
                            .append(message)
            );
    }

    private static String playerName(@Nullable CachedPlayer player) {
        return player == null ? "UNKNOWN PLAYER" : player.name();
    }

    private static void newRequest(FriendRequest request) {
        CachedPlayer origin = getAPI().getPlayer(request.origin());
        sendIfAvailable(
                request.target(),
                Text.translatableWithFallback(
                        "fischy_friends.request_incoming",
                        "%s has sent you a friend request!",
                        playerName(origin)
                ).formatted(Colors.PRIMARY)
                .append(" ")
                .append(TextFormatter.makeAcceptDenyText(origin == null ? null : origin.name()))
        );
    }

    private static void requestCancelled(FriendRequest request) {
        CachedPlayer target = getAPI().getPlayer(request.target());
        sendIfAvailable(
                request.origin(),
                Text.translatableWithFallback(
                        "fischy_friends.request_cancelled",
                        "You have cancelled your friend request to %s",
                        playerName(target)
                ).formatted(Colors.PRIMARY)
        );
    }

    private static void requestDenied(FriendRequest request) {
        CachedPlayer target = getAPI().getPlayer(request.target());
        sendIfAvailable(
                request.origin(),
                Text.translatableWithFallback(
                        "fischy_friends.request_denied",
                        "%s has denied your friend request",
                        playerName(target)
                ).formatted(Colors.PRIMARY)
        );
    }

    private static void requestAccepted(FriendRequest request) {
        CachedPlayer target = getAPI().getPlayer(request.target());
        sendIfAvailable(
                request.origin(),
                Text.translatableWithFallback(
                        "fischy_friends.request_accepted",
                        "%s has accepted your friend request",
                        playerName(target)
                ).formatted(Colors.PRIMARY)
        );
    }
}
