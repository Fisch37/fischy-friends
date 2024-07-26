package de.fisch37.fischyfriends.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import de.fisch37.fischyfriends.api.CachedPlayer;
import de.fisch37.fischyfriends.api.FriendRequest;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Collection;
import java.util.UUID;

import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static de.fisch37.fischyfriends.FischyFriends.LOGGER;
import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandManager.argument;

import static de.fisch37.fischyfriends.FischyFriends.getAPI;

public abstract class FriendCommand {
    public static final DynamicCommandExceptionType PLAYER_NOT_FOUND = new DynamicCommandExceptionType(o ->
            Text.translatableWithFallback(
                    "fischy_friends.player_not_found",
                    "Could not find a player named %s",
                    o
    ));
    public static final DynamicCommandExceptionType NOT_A_FRIEND = new DynamicCommandExceptionType(o ->
            Text.translatableWithFallback(
                    "fischy_friends.not_a_friend",
                    "Player %s is not a friend of yours",
                    o
    ));

    public static void register() {
        CommandRegistrationCallback.EVENT.register(FriendCommand::register);
    }

    private static void register(
            CommandDispatcher<ServerCommandSource> dispatcher,
            CommandRegistryAccess registry,
            CommandManager.RegistrationEnvironment registrationEnvironment
    ) {
        dispatcher.register(literal("friend")
                .then(literal("add")
                        .then(argument("player", word())
                                .suggests(new CachedPlayerSuggestionProvider(true))
                                .executes(FriendCommand::addFriend)
                ))
                .then(literal("remove")
                        .then(argument("friend", word())
                                .suggests(new FriendSuggestionProvider())
                                .executes(FriendCommand::remFriend)
                ))
                .then(literal("list")
                        .executes(FriendCommand::listFriends)
                        .then(argument("player", word())
                                .requires(source -> source.hasPermissionLevel(3))
                                .suggests(new CachedPlayerSuggestionProvider(false))
                ))
                .then(literal("requests")
                        .then(literal("accept")
                                .then(argument("origin", word())
                                        .suggests(new FriendRequestSuggestionProvider(true))
                                        .executes(requestCommand(
                                                "origin",
                                                false,
                                                FriendCommand::acceptRequest
                                        ))
                        ))
                        .then(literal("deny")
                                .then(argument("origin", word())
                                        .suggests(new FriendRequestSuggestionProvider(true))
                                        .executes(requestCommand(
                                                "origin",
                                                false,
                                                FriendCommand::denyRequest
                                        ))
                        ))
                        .then(literal("cancel")
                                .then(argument("target", word())
                                        .suggests(new FriendRequestSuggestionProvider(false))
                                        .executes(requestCommand(
                                                "target",
                                                true,
                                                FriendCommand::cancelRequest
                                        ))
                        ))
                        .then(literal("list")
                                .executes(FriendCommand::listRequestsBoth)
                                .then(literal("pending")
                                        .executes(FriendCommand::listRequestsPending)
                                )
                                .then(literal("incoming")
                                        .executes(FriendCommand::listRequestsIncoming)
                        ))
                )
        );
    }

    private static CachedPlayer getPlayerByArgument(
            CommandContext<ServerCommandSource> context,
            String argumentName
    ) throws CommandSyntaxException {
        String targetName = StringArgumentType.getString(context, argumentName);
        CachedPlayer target = getAPI().getPlayer(targetName);
        if (target == null) {
            throw PLAYER_NOT_FOUND.create(targetName);
        }
        return target;
    }

    private static int addFriend(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        CachedPlayer target = getPlayerByArgument(context, "player");
        getAPI().getRequestManager().addFriendRequest(new FriendRequest(
                player.getUuid(),
                target.uuid()
        ));
        context.getSource().sendFeedback(() -> Text.translatableWithFallback(
                "fischy_friends.friend_request_sent",
                "Sent a friend request to %s",
                target.name()
        ), false);
        return 0;
    }

    private static int remFriend(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        CachedPlayer target = getPlayerByArgument(context, "friend");
        if (!getAPI().getFriends(player.getUuid()).contains(target.uuid())) {
            throw NOT_A_FRIEND.create(target.name());
        }
        context.getSource().sendFeedback(() -> Text.translatableWithFallback(
                "fischy_friends.friend_removed",
                "Removed %s from your list of friends",
                target.name()
        ), false);
        return 0;
    }

    private static int listFriends(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayerOrThrow();
        Collection<UUID> friends = getAPI().getFriends(player);
        source.sendFeedback(
                () -> Text.translatableWithFallback(
                        "fischy_friends.friends_header",
                        "You have %s friends",
                        friends.size()
                ).formatted(Formatting.GOLD)
                ,
                false
        );
        source.sendFeedback(
                () -> Text.literal("-=-=-=-=-=-=-=-=-=-=-=-=-=-=-")
                        .formatted(Formatting.RED)
                ,
                false
        );
        for(UUID friendUuid : friends) {
            CachedPlayer friend = getAPI().getPlayer(friendUuid);
            if (friend == null) {
                LOGGER.warn("Cache miss for {} in listFriends", friendUuid);
                continue;
            }
            source.sendFeedback(
                    () -> Text.literal("- ")
                            .formatted(Formatting.RED)
                            .append(Text.literal(friend.name())
                                .formatted(Formatting.GOLD)
                            )
                    ,
                    false
            );
        }
        return 0;
    }

    private static Command<ServerCommandSource> requestCommand(
            String playerArgument,
            boolean fromOrigin,
            RequestCommandHandler handler
    ) {
        return context -> {
            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
            CachedPlayer otherPlayer = getPlayerByArgument(context, playerArgument);
            FriendRequest request;
            if (fromOrigin)
                request = new FriendRequest(player.getUuid(), otherPlayer.uuid());
            else
                request = new FriendRequest(otherPlayer.uuid(), player.getUuid());
            return handler.execute(context, player, otherPlayer, request);
        };
    }

    private static int acceptRequest(
            CommandContext<ServerCommandSource> context,
            ServerPlayerEntity player,
            CachedPlayer origin,
            FriendRequest request
    ) {
        getAPI().getRequestManager().acceptFriendRequest(request);
        context.getSource().sendFeedback(() -> Text.translatableWithFallback(
                "fischy_friends.request_accepted",
                "You are now friends with %s",
                origin.name()
        ), false);
        return 0;
    }

    private static int denyRequest(
            CommandContext<ServerCommandSource> context,
            ServerPlayerEntity player,
            CachedPlayer origin,
            FriendRequest request
    ) {
        getAPI().getRequestManager().denyFriendRequest(request);
        context.getSource().sendFeedback(() -> Text.translatableWithFallback(
                "fischy_friends.request_denied",
                "You have denied %s's friend request",
                origin.name()
        ), false);
        return 0;
    }

    private static int cancelRequest(
            CommandContext<ServerCommandSource> context,
            ServerPlayerEntity player,
            CachedPlayer target,
            FriendRequest request
    ) {
        getAPI().getRequestManager().cancelFriendRequest(request);
        context.getSource().sendFeedback(() -> Text.translatableWithFallback(
                "fischy_friends.request_cancelled",
                "Friend request with %s has been cancelled",
                target.name()
        ), false);
        return 0;
    }

    private static int listRequestsBoth(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return Math.max(listRequestsPending(context), listRequestsIncoming(context));
    }

    private static int listRequestsPending(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        Collection<FriendRequest> requests = getAPI().getRequestManager().getOpenRequestsByPlayer(
                source.getPlayerOrThrow().getUuid()
        );
        source.sendFeedback(
                () -> Text.translatableWithFallback(
                        "fischy_friends.pending_requests_header",
                        "You have %s friend requests pending",
                        requests.size()
                ).formatted(Formatting.GOLD)
                ,
                false
        );
        source.sendFeedback(
                () -> Text.literal("-=-=-=-=-=-=-=-=-=-=-=-=-=-=-")
                        .formatted(Formatting.RED)
                ,
                false
        );
        for (FriendRequest request : requests) {
            CachedPlayer requestTarget = getAPI().getPlayer(request.target());
            if (requestTarget == null) {
                LOGGER.warn("Cache miss for {} in listRequestsPending", request.target());
                continue;
            }

            source.sendFeedback(
                    () -> Text.literal("- ")
                            .formatted(Formatting.RED)
                            .append(Text.literal(requestTarget.name()).formatted(Formatting.GOLD))
                    ,
                    false
            );
        }

        return 0;
    }

    private static int listRequestsIncoming(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        Collection<FriendRequest> requests = getAPI().getRequestManager().getOpenRequestsForPlayer(
                source.getPlayerOrThrow().getUuid()
        );
        source.sendFeedback(
                () -> Text.translatableWithFallback(
                    "fischy_friends.incoming_requests_header",
                    "You have %s incoming friend requests:",
                    requests.size()
                ).formatted(Formatting.GOLD)
                ,
                false
        );
        source.sendFeedback(
                () -> Text.literal("-=-=-=-=-=-=-=-=-=-=-=-=-=-=-")
                        .formatted(Formatting.RED)
                ,
                false
        );
        for (FriendRequest request : requests) {
            CachedPlayer player = getAPI().getPlayer(request.target());
            if (player == null) {
                LOGGER.warn("Cache miss for {} in listRequestsIncoming", request.target());
                continue;
            }

            source.sendFeedback(
                    () -> Text.literal("- ")
                            .formatted(Formatting.RED)
                            .append(
                                    Text.literal(player.name())
                                            .formatted(Formatting.GOLD)
                            )
                    ,
                    false
            );
        }

        return 0;
    }


    @FunctionalInterface
    private interface RequestCommandHandler {
        int execute(
                CommandContext<ServerCommandSource> context,
                ServerPlayerEntity player,
                CachedPlayer target,
                FriendRequest request
        );
    }
}
