package crol.client.managers;

import crol.client.CrolClient;
import crol.client.ui.commands.Command;
import crol.client.ui.commands.impl.BindCommand;
import crol.client.ui.commands.impl.CfgCommand;
import crol.client.ui.commands.impl.GpsCommand;
import crol.client.ui.commands.impl.HelpCommand;
import crol.client.ui.commands.impl.PrefixCommand;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru_crol.crol_meta.sdk.Compile;

@Environment(EnvType.CLIENT)
public class CommandManager {
   private final List<Command> commands = new ArrayList();
   private char commandPrefix;

   public CommandManager() {
      this.init();
      this.commandPrefix = '.';
   }

   @Compile
   private void init() {
      this.addCommands(new HelpCommand(), new PrefixCommand(), new GpsCommand(), new CfgCommand(), new BindCommand());
   }

   private void addCommands(Command... commands) {
      this.commands.addAll(Arrays.asList(commands));
   }

   public List<Command> getCommands() {
      return this.commands;
   }

   public char getCommandPrefix() {
      return this.commandPrefix;
   }

   public void setCommandPrefix(char commandPrefix) {
      this.commandPrefix = commandPrefix;
   }

   @Compile
   public boolean executeCommand(String content) {
      boolean send = true;
      String prefix = String.valueOf(CrolClient.INSTANCE.getCommandManager().getCommandPrefix());

      for(Command command : CrolClient.INSTANCE.getCommandManager().getCommands()) {
         if (content.toLowerCase().startsWith(prefix + command.getCommandInfo().prefix().toLowerCase())) {
            command.execute(content.substring((prefix + command.getCommandInfo().prefix()).length()).trim().split(" "));
            send = false;
         }
      }

      return send;
   }
}
