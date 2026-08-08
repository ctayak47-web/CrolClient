package crol.client.ui.commands;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public abstract class Command {
   protected CommandInfo commandInfo;

   public Command(CommandInfo commandInfo) {
      this.commandInfo = commandInfo;
   }

   public abstract void execute(String[] var1);

   public CommandInfo getCommandInfo() {
      return this.commandInfo;
   }
}
