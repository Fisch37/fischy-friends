package de.fisch37.fischyfriends.networking;

import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import static de.fisch37.fischyfriends.FischyFriends.MOD_ID;
import static net.minecraft.network.packet.CustomPayload.Id;

public class PacketTypes {
    public static final Id<GetFriends> GET_FRIENDS = id("get_friends");
    public static final Id<FriendList> FRIEND_LIST = id("friend_list");

    public static final Id<FriendRemoved> FRIEND_REMOVED = id("friend_removed");

    public static final Id<FriendRequestPacket> FRIEND_REQUEST = id("friend_request");
    public static final Id<CancelFriendRequest> FRIEND_REQUEST_CANCEL = id("friend_request_cancel");
    public static final Id<AcceptFriendRequest> FRIEND_REQUEST_ACCEPT = id("friend_request_accept");
    public static final Id<DenyFriendRequest> FRIEND_REQUEST_DENY = id("friend_request_deny");

    private static <T extends CustomPayload> Id<T> id(String name) {
        return new Id<>(Identifier.of(MOD_ID, name));
    }

    public static void register() {
        GetFriends.register();
        FriendList.register();

        FriendRemoved.register();

        FriendRequestPacket.register();
        CancelFriendRequest.register();
        AcceptFriendRequest.register();
        DenyFriendRequest.register();
    }
}
