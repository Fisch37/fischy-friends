package de.fisch37.fischyfriends;

import de.fisch37.fischyfriends.api.CachedPlayer;
import de.fisch37.fischyfriends.api.FriendRequest;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

import java.util.*;

class FriendsState extends PersistentState {
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

        return nbt;
    }

    private static FriendsState createFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        FriendsState state = new FriendsState();
        for (NbtElement element : tag.getList("players", NbtElement.COMPOUND_TYPE)) {
            state.setPlayer(CachedPlayer.fromNbt((NbtCompound)element));
        }

        return state;
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
        return state;
    }
}
