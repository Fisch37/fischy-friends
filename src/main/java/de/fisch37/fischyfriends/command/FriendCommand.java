package de.fisch37.fischyfriends.command;

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

import static com.mojang.brigadier.arguments.StringArgumentType.word;
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

    private static int listFriends(CommandContext<ServerCommandSource> context) {
        return 0;
    }
}
