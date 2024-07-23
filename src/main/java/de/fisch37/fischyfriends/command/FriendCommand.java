package de.fisch37.fischyfriends.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import de.fisch37.fischyfriends.api.FriendsAPI;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandManager.argument;

public abstract class FriendCommand {
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
                        .then(argument("player", EntityArgumentType.player())
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
                                .suggests(new CachedPlayerSuggestionProvider())
                        )
                )
        );
    }


    private static int addFriend(CommandContext<ServerCommandSource> context) {
        return 0;
    }

    private static int remFriend(CommandContext<ServerCommandSource> context) {
        return 0;
    }

    private static int listFriends(CommandContext<ServerCommandSource> context) {
        return 0;
    }
}
