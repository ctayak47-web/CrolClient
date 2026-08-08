package crol.client.ui.commands.impl;

import crol.client.CrolClient;
import crol.client.ui.commands.Command;
import crol.client.ui.commands.CommandInfo;
import crol.client.util.player.ChatUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru_crol.crol_meta.sdk.Compile;

@Environment(EnvType.CLIENT)
public class HelpCommand extends Command {
   public HelpCommand() {
      super(new CommandInfo("list of commands", "help"));
   }

   @Compile
   public void execute(String[] args) {
      ChatUtil.addMessage("WILD CLIENT BETA TEST");
      String prefix = String.valueOf(CrolClient.INSTANCE.getCommandManager().getCommandPrefix());

      for(Command command : CrolClient.INSTANCE.getCommandManager().getCommands()) {
         CommandInfo commandInfo1 = command.getCommandInfo();
         ChatUtil.addMessage(prefix + commandInfo1.prefix() + " - " + commandInfo1.desc());
      }

   }
}
