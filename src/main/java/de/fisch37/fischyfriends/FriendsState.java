package de.fisch37.fischyfriends;

import de.fisch37.fischyfriends.api.CachedPlayer;
import de.fisch37.fischyfriends.api.FriendRequest;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

import java.util.*;

class FriendsState extends PersistentState {
    private boolean hasRegisteredEvents = false;
    private final Map<UUID, CachedPlayer> players = new HashMap<>();
    private final Hashtable<UUID, Set<UUID>> friendsMap = new Hashtable<>();
    private final Hashtable<UUID, Set<FriendRequest>> outboundRequest = new Hashtable<>();
    private final Hashtable<UUID, Set<FriendRequest>> inboundRequests = new Hashtable<>();

    private void setPlayer(CachedPlayer player) {
        players.put(player.uuid(), player);
    }

    Set<UUID> getAllFriends(UUID key) {
        return friendsMap.computeIfAbsent(key, k -> new HashSet<>());
    }

    boolean areFriends(UUID a, UUID b) {
        return getAllFriends(a).contains(b);
    }

    void addFriendship(UUID a, UUID b) {
        getAllFriends(a).add(b);
        getAllFriends(b).add(a);
    }

    boolean removeFriendship(UUID a, UUID b) {
        return getAllFriends(a).remove(b)
                || getAllFriends(b).remove(a)
                ;
    }

    private Set<FriendRequest> getFriendRequests(Map<UUID, Set<FriendRequest>> map, UUID key) {
        return map.computeIfAbsent(key, k -> new HashSet<>());
    }

    void addFriendRequest(FriendRequest request) {
        getFriendRequests(outboundRequest, request.origin()).add(request);
        getFriendRequests(inboundRequests, request.target()).add(request);
    }

    void removeFriendRequest(FriendRequest request) {
        getFriendRequests(outboundRequest, request.origin()).remove(request);
        getFriendRequests(inboundRequests, request.target()).remove(request);
    }

    Collection<CachedPlayer> getPlayers() {
        return players.values();
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        NbtList playerList = new NbtList();
        for (CachedPlayer player : players.values()) {
            playerList.add(player.toNbt());
        }
        nbt.put("players", playerList);

        NbtCompound playerFriends = new NbtCompound();
        for (Map.Entry<UUID, Set<UUID>> friendsForPlayer : friendsMap.entrySet()) {
            NbtList friendList = new NbtList();
            for (UUID friend : friendsForPlayer.getValue()) {
                friendList.add(NbtHelper.fromUuid(friend));
            }

            playerFriends.put(friendsForPlayer.getKey().toString(), friendList);
        }
        nbt.put("friends", playerFriends);

        NbtList friendRequests = new NbtList();
        for (Set<FriendRequest> requests : outboundRequest.values()) {
            for (FriendRequest request : requests) {
                friendRequests.add(requestToNbt(request));
            }
        }
        nbt.put("requests", friendRequests);

        return nbt;
    }

    private static FriendsState createFromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        FriendsState state = new FriendsState();
        for (NbtElement element : nbt.getList("players", NbtElement.COMPOUND_TYPE)) {
            state.setPlayer(CachedPlayer.fromNbt((NbtCompound)element));
        }

        NbtCompound friendNbt = nbt.getCompound("friends");
        for (String playerUuidString : friendNbt.getKeys()) {
            Set<UUID> friends = state.getAllFriends(UUID.fromString(playerUuidString));
            NbtList nbtList = friendNbt.getList(playerUuidString, NbtElement.INT_ARRAY_TYPE);
            for (NbtElement nbtFriend : nbtList) {
                friends.add(NbtHelper.toUuid(nbtFriend));
            }
        }

        NbtList friendRequestsNbt = nbt.getList("requests", NbtElement.COMPOUND_TYPE);
        for (NbtElement requestNbt : friendRequestsNbt) {
            state.addFriendRequest(requestFromNbt((NbtCompound) requestNbt));
        }

        return state;
    }

    // Not very nice but I want to avoid exposing redundant API
    private static NbtCompound requestToNbt(FriendRequest request) {
        NbtCompound nbt = new NbtCompound();
        nbt.putUuid("origin", request.origin());
        nbt.putUuid("target", request.target());
        return nbt;
    }

    private static FriendRequest requestFromNbt(NbtCompound nbt) {
        return new FriendRequest(
                nbt.getUuid("origin"),
                nbt.getUuid("target")
        );
    }

    private static final Type<FriendsState> TYPE = new Type<>(
            FriendsState::new,
            FriendsState::createFromNbt,
            null
    );

    static FriendsState getServerState(MinecraftServer server) {
        FriendsState state = Objects.requireNonNull(server.getWorld(World.OVERWORLD))
                .getPersistentStateManager()
                .getOrCreate(TYPE, FischyFriends.MOD_ID)
                ;
        state.markDirty();
        state.register();
        return state;
    }

    private void register() {
        if (hasRegisteredEvents) return;
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            players.put(player.getUuid(), new CachedPlayer(
                    player.getUuid(),
                    player.getGameProfile().getName()
            ));
        });
        hasRegisteredEvents = true;
    }
}
