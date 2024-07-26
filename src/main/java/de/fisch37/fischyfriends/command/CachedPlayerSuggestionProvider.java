package de.fisch37.fischyfriends.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import de.fisch37.fischyfriends.FischyFriends;
import de.fisch37.fischyfriends.api.CachedPlayer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

class CachedPlayerSuggestionProvider implements SuggestionProvider<ServerCommandSource> {
    private final boolean excludeSelf;

    public CachedPlayerSuggestionProvider(boolean excludeSelf) {
        this.excludeSelf = excludeSelf;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(
            CommandContext<ServerCommandSource> context,
            SuggestionsBuilder builder
    ) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        UUID playerUuid;
        if (player == null) playerUuid = null;
        else playerUuid = player.getUuid();

        for (CachedPlayer suggestedPlayer : FischyFriends.getAPI().getPlayers()) {
            if (suggestedPlayer.uuid().equals(playerUuid)) continue;
            builder.suggest(suggestedPlayer.name());
        }

        return builder.buildFuture();
    }
}
