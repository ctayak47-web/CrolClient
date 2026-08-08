package crol.client.managers;

import crol.client.util.other.Friend;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru_crol.crol_meta.sdk.Compile;

@Environment(EnvType.CLIENT)
public class FriendManager {
   private final List<Friend> friends = new ArrayList();

   @Compile
   public void addFriend(String name) {
      this.friends.add(new Friend(name));
   }

   public List<Friend> getFriends() {
      return this.friends;
   }

   public boolean isFriend(String name) {
      for(Friend friend : this.friends) {
         if (friend.name().equals(name)) {
            return true;
         }
      }

      return false;
   }

   @Compile
   public void removeFriend(String name) {
      this.friends.removeIf((friend) -> friend.name().equalsIgnoreCase(name));
   }
}
