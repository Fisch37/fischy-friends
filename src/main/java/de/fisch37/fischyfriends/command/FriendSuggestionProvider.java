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

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

class FriendSuggestionProvider implements SuggestionProvider<ServerCommandSource> {
    @Override
    public CompletableFuture<Suggestions> getSuggestions(
            CommandContext<ServerCommandSource> context,
            SuggestionsBuilder builder
    ) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player != null) {
            Set<UUID> friends = FischyFriends.getAPI().getFriends(player.getUuid());
            for (UUID friend : friends) {
                CachedPlayer friendPlayer = FischyFriends.getAPI().getPlayer(friend);
                if (friendPlayer != null)
                    builder.suggest(friendPlayer.name());
            }
        }

        return builder.buildFuture();
    }
}
