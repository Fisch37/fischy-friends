package de.fisch37.fischyfriends.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.ServerCommandSource;

import java.util.concurrent.CompletableFuture;

public class FriendRequestSuggestionProvider implements SuggestionProvider<ServerCommandSource> {
    private final boolean referenceTarget;

    public FriendRequestSuggestionProvider(boolean referenceTarget) {
        this.referenceTarget = referenceTarget;
    }
    public FriendRequestSuggestionProvider() {
        this(false);
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(
            CommandContext<ServerCommandSource> context,
            SuggestionsBuilder builder
    ) {
        return null;
    }
}
