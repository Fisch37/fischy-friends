package de.fisch37.fischyfriends;

import de.fisch37.fischyfriends.api.CachedPlayer;
import de.fisch37.fischyfriends.api.FriendRequest;
import de.fisch37.fischyfriends.api.FriendRequestManager;
import de.fisch37.fischyfriends.api.FriendsAPI;
import de.fisch37.fischyfriends.command.FriendCommand;
import de.fisch37.fischyfriends.networking.PacketTypes;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class FischyFriends implements DedicatedServerModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger(FischyFriends.class);
    public static final String MOD_ID = "fischy_friends";

    static FriendsState STATE;
    static FriendRequestManager requestManager;
    private static FriendsAPI api;


    @Override
    public void onInitializeServer() {
        PacketTypes.register();
        NetworkHandler.register();
        FriendCommand.register();
        api = new FriendsAPIImpl();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            STATE = FriendsState.getServerState(server);
            requestManager = new FriendRequestManagerImpl(STATE);
            addDefaultHandlers(server);
        });
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> onJoinStatusMessage(handler));
    }

    private void onJoinStatusMessage(ServerPlayNetworkHandler handler) {
        ServerPlayerEntity player = handler.player;
        Collection<FriendRequest> requests = requestManager.getOpenRequestsForPlayer(player.getUuid());
        if (!requests.isEmpty()) {
            player.sendMessage(Text.translatableWithFallback(
                    "fischy_friends.welcome_open_requests",
                    "Hello! You have %s requests open:",
                    requests.size()
            ).formatted(Colors.PRIMARY));
            for (FriendRequest request : requests) {
                CachedPlayer origin = getAPI().getPlayer(request.origin());
                player.sendMessage(Text.literal("")
                        .append(Text.literal("- ").formatted(Colors.SECONDARY))
                        .append(Text.literal(origin == null ? "UNKNOWN PLAYER" : origin.name())
                                .formatted(Colors.PRIMARY)
                        )
                        .append(" ")
                        .append(TextFormatter.makeAcceptDenyText(origin == null ? null : origin.name()))
                );
            }
        }
    }

    private void addDefaultHandlers(MinecraftServer server) {
        ChatEventHandlers.registerEventHandlers(server);
        NetworkHandler.registerEventHandlers(server);
    }

    public static FriendsAPI getAPI() {
        return api;
    }
}
