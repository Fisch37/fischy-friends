package de.fisch37.fischyfriends;

import de.fisch37.fischyfriends.api.CachedPlayer;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class TextFormatter {
    private static final String REQ_ACCEPT_COMMAND = "/friend requests accept %s",
            REQ_DENY_COMMAND = "/friend requests deny %s",
            REM_FRIEND_COMMAND = "/friend remove %s";

    public static Text makeAcceptDenyText(@Nullable String target) {
        if (target == null)
            return Text.literal("UNKNOWN PLAYER");
        return Text.literal("")
                .append(Text.literal("[✔]")
                        .setStyle(Style.EMPTY
                                .withClickEvent(new ClickEvent(
                                        ClickEvent.Action.RUN_COMMAND,
                                        REQ_ACCEPT_COMMAND.formatted(target)
                                ))
                                .withHoverEvent(new HoverEvent(
                                        HoverEvent.Action.SHOW_TEXT,
                                        Text.translatableWithFallback(
                                                "fischy_friends.request_accept_button",
                                                "Click to accept the friend request"
                                        )
                                ))
                        )
                        .formatted(Colors.SUCCESS)
                )
                .append(" ")
                .append(Text.literal("[✖]")
                        .setStyle(Style.EMPTY
                                .withClickEvent(new ClickEvent(
                                        ClickEvent.Action.RUN_COMMAND,
                                        REQ_DENY_COMMAND.formatted(target)
                                ))
                                .withHoverEvent(new HoverEvent(
                                        HoverEvent.Action.SHOW_TEXT,
                                        Text.translatableWithFallback(
                                                "fischy_friends.request_deny_button",
                                                "Click to deny the friend request"
                                        )
                                ))
                        )
                        .formatted(Colors.FAILURE)
                );
    }

    public static Text makeFriendListing(@NotNull CachedPlayer friend) {
        return Text.literal("")
                .append(Text.literal("- ").formatted(Colors.SECONDARY))
                .append(" ")
                .append(Text.literal("[-]")
                        .formatted(Colors.FAILURE)
                        .styled(style -> style
                                .withHoverEvent(new HoverEvent(
                                        HoverEvent.Action.SHOW_TEXT,
                                        Text.translatableWithFallback(
                                                "fischy_friends.remove_friend_button",
                                                "Click to remove from your friend list"
                                        )
                                ))
                                .withClickEvent(new ClickEvent(
                                        ClickEvent.Action.SUGGEST_COMMAND,
                                        REM_FRIEND_COMMAND.formatted(friend.name())
                                ))
                        )
                );
    }
}
