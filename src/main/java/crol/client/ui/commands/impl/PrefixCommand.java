package crol.client.ui.commands.impl;

import crol.client.CrolClient;
import crol.client.ui.commands.Command;
import crol.client.ui.commands.CommandInfo;
import crol.client.util.player.ChatUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru_crol.crol_meta.sdk.Compile;

@Environment(EnvType.CLIENT)
public class PrefixCommand extends Command {
   public PrefixCommand() {
      super(new CommandInfo(".prefix symbol", "prefix"));
   }

   @Compile
   public void execute(String[] args) {
      if (args.length != 0 && !args[0].isEmpty()) {
         if (args[0].length() == 1) {
            char s = args[0].charAt(0);
            CrolClient.INSTANCE.getCommandManager().setCommandPrefix(s);
            ChatUtil.addMessage("Prefix set to " + s);
         } else {
            ChatUtil.addMessage("Invalid prefix value");
         }

      } else {
         ChatUtil.addMessage(".prefix symbol");
      }
   }
}
