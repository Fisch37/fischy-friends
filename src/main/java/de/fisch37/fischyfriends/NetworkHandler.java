package de.fisch37.fischyfriends;

import de.fisch37.fischyfriends.api.CachedPlayer;
import de.fisch37.fischyfriends.api.FriendRequest;
import de.fisch37.fischyfriends.api.FriendRequestManager;
import de.fisch37.fischyfriends.api.FriendsAPI;
import de.fisch37.fischyfriends.networking.*;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiConsumer;

import static de.fisch37.fischyfriends.FischyFriends.LOGGER;
import static de.fisch37.fischyfriends.networking.PacketTypes.*;
import static de.fisch37.fischyfriends.FischyFriends.getAPI;
import static de.fisch37.fischyfriends.api.FriendRequestManager.EventType;

abstract class NetworkHandler {
    private static MinecraftServer server;

    static void register() {
        ServerPlayNetworking.registerGlobalReceiver(GET_FRIENDS, NetworkHandler::sendFriendList);
        ServerPlayNetworking.registerGlobalReceiver(FRIEND_REQUEST, NetworkHandler::friendRequest);
        ServerPlayNetworking.registerGlobalReceiver(FRIEND_REQUEST_CANCEL, NetworkHandler::cancelFriendRequest);
        ServerPlayNetworking.registerGlobalReceiver(FRIEND_REQUEST_ACCEPT, NetworkHandler::acceptFriendRequest);
        ServerPlayNetworking.registerGlobalReceiver(FRIEND_REQUEST_DENY, NetworkHandler::denyFriendRequest);
    }

    static void registerEventHandlers(MinecraftServer server) {
        NetworkHandler.server = server;

        getAPI().registerFriendRemovedListener(NetworkHandler::onFriendRemoved);

        registerListener(EventType.CREATED, NetworkHandler::onFriendRequest);
        registerListener(EventType.CANCELLED, NetworkHandler::onFriendRequestCancelled);
        registerListener(EventType.ACCEPTED, NetworkHandler::onFriendRequestAccepted);
        registerListener(EventType.DENIED, NetworkHandler::onFriendRequestDenied);
    }

    private static void registerListener(
            EventType type,
            FriendRequestManager.EventHandler listener
    ) {
        getAPI().getRequestManager().registerListener(type, listener);
    }

    private static Optional<ServerPlayerEntity> getPlayer(UUID uuid) {
        return Optional.ofNullable(server.getPlayerManager().getPlayer(uuid));
    }


    private static void sendFriendList(GetFriends packet, ServerPlayNetworking.Context context) {
        Collection<UUID> friendUuids = getAPI().getFriends(context.player());
        ArrayList<@NotNull CachedPlayer> friends = new ArrayList<>(friendUuids.size());
        for (UUID uuid : friendUuids) {
            CachedPlayer player = getAPI().getPlayer(uuid);
            if (player == null) {
                LOGGER.warn("Cache miss for {} in NetworkHandler.sendFriendList", uuid);
                continue;
            }
            friends.add(player);
        }

        context.responseSender().sendPacket(new FriendList(friends));
    }

    /**
     * Spot the Python programmer
     */
    private static void friendRequestAction(
            BiConsumer<FriendRequestManager, FriendRequest> action,
            ServerPlayNetworking.Context context,
            UUID target
    ) {
        action.accept(getAPI().getRequestManager(), new FriendRequest(context.player().getUuid(), target));
    }

    private static void friendRequest(FriendRequestPacket packet, ServerPlayNetworking.Context context) {
        friendRequestAction(FriendRequestManager::addFriendRequest, context, packet.targetOrOrigin());
    }

    private static void cancelFriendRequest(CancelFriendRequest packet, ServerPlayNetworking.Context context) {
        friendRequestAction(FriendRequestManager::cancelFriendRequest, context, packet.targetOrOrigin());
    }

    private static void acceptFriendRequest(AcceptFriendRequest packet, ServerPlayNetworking.Context context) {
        friendRequestAction(FriendRequestManager::acceptFriendRequest, context, packet.targetOrOrigin());
    }

    private static void denyFriendRequest(DenyFriendRequest packet, ServerPlayNetworking.Context context) {
        friendRequestAction(FriendRequestManager::denyFriendRequest, context, packet.targetOrOrigin());
    }


    private static void maySend(UUID receiver, CustomPayload packet) {
        getPlayer(receiver).ifPresent(player -> ServerPlayNetworking.send(
                player,
                packet
        ));
    }

    private static void onFriendRemoved(FriendsAPI.Friendship friendship) {
        final UUID a = friendship.a(), b = friendship.b();
        maySend(a, new FriendRemoved(b));
        maySend(b, new FriendRemoved(a));
    }

    private static void onFriendRequest(FriendRequest request) {
        maySend(request.target(), new FriendRequestPacket(request.origin()));
    }

    private static void onFriendRequestCancelled(FriendRequest request) {
        maySend(request.target(), new CancelFriendRequest(request.origin()));
    }

    private static void onFriendRequestAccepted(FriendRequest request) {
        maySend(request.origin(), new AcceptFriendRequest(request.target()));
    }

    private static void onFriendRequestDenied(FriendRequest request) {
        maySend(request.origin(), new DenyFriendRequest(request.target()));
    }
}
