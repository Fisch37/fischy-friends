package de.fisch37.fischyfriends.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import de.fisch37.fischyfriends.FischyFriends;
import de.fisch37.fischyfriends.api.CachedPlayer;
import de.fisch37.fischyfriends.api.FriendRequest;
import de.fisch37.fischyfriends.api.FriendRequestManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static de.fisch37.fischyfriends.FischyFriends.LOGGER;

public class FriendRequestSuggestionProvider implements SuggestionProvider<ServerCommandSource> {
    private final boolean forTarget;

    public FriendRequestSuggestionProvider(boolean forTarget) {
        this.forTarget = forTarget;
    }
    public FriendRequestSuggestionProvider() {
        this(false);
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(
            CommandContext<ServerCommandSource> context,
            SuggestionsBuilder builder
    ) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player != null) {
            FriendRequestManager requestManager = FischyFriends.getAPI().getRequestManager();
            List<FriendRequest> requests = forTarget
                    ? requestManager.getOpenRequestsForPlayer(player.getUuid())
                    : requestManager.getOpenRequestsByPlayer(player.getUuid());

            for (FriendRequest request : requests) {
                UUID suggestionUuid = forTarget ? request.origin() : request.target();
                CachedPlayer suggestion = FischyFriends.getAPI().getPlayer(suggestionUuid);
                if (suggestion == null) {
                    LOGGER.warn(
                            "Missed player cache on {} during FriendRequestSuggestionProvider."
                                    + "This shouldn't happen",
                            suggestionUuid
                    );
                    continue;
                }
                builder.suggest(suggestion.name());
            }
        }
        return builder.buildFuture();
    }
}
