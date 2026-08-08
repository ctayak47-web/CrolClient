package ru_crol.crol_meta.profile;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class Profile {
   public static String username = "by Badluck && soezsoft && fix src WEXTER5";
   public static int uid = 1;
   public static Role role;
   public static String hwid;
   public static String subscriptionEndDate;

   private Profile() {
   }

   public static String getUsername() {
      return username;
   }

   public static int getUid() {
      return uid;
   }

   public static Role getRole() {
      return role;
   }

   public static String getHwid() {
      return hwid;
   }

   public static String getSubscriptionEndDate() {
      return subscriptionEndDate;
   }

   static {
      role = Role.PASTER;
      subscriptionEndDate = "Xyeta<3";
   }
}
