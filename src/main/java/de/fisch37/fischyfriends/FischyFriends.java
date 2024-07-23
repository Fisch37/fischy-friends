package de.fisch37.fischyfriends;

import de.fisch37.fischyfriends.api.FriendsAPI;
import de.fisch37.fischyfriends.command.FriendCommand;
import de.fisch37.fischyfriends.networking.PacketTypes;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class FischyFriends implements DedicatedServerModInitializer {
    public static final String MOD_ID = "fischy_friends";
    static FriendsState STATE;
    private static FriendsAPI api;

    @Override
    public void onInitializeServer() {
        PacketTypes.register();
        FriendCommand.register();
        api = new FriendsAPIImpl();
        ServerLifecycleEvents.SERVER_STARTED.register(server -> STATE = FriendsState.getServerState(server));
    }

    public static FriendsAPI getAPI() {
        return api;
    }
}
