package de.fisch37.fischyfriends;

import de.fisch37.fischyfriends.api.FriendRequestManager;
import de.fisch37.fischyfriends.api.FriendsAPI;
import de.fisch37.fischyfriends.command.FriendCommand;
import de.fisch37.fischyfriends.networking.PacketTypes;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FischyFriends implements DedicatedServerModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger(FischyFriends.class);
    public static final String MOD_ID = "fischy_friends";
    static FriendsState STATE;
    static FriendRequestManager requestManager;
    private static FriendsAPI api;

    @Override
    public void onInitializeServer() {
        PacketTypes.register();
        FriendCommand.register();
        api = new FriendsAPIImpl();
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            STATE = FriendsState.getServerState(server);
            requestManager = new FriendRequestManagerImpl(STATE);
            addDefaultHandlers(server);
        });
    }

    private void addDefaultHandlers(MinecraftServer server) {
        ChatEventHandlers.registerEventHandlers(server);
    }

    public static FriendsAPI getAPI() {
        return api;
    }
}
