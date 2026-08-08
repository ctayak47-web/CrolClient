package crol.client.ui.commands.impl;

import crol.client.CrolClient;
import crol.client.ui.commands.Command;
import crol.client.ui.commands.CommandInfo;
import crol.client.util.player.ChatUtil;
import java.io.File;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Util;
import ru_crol.crol_meta.sdk.Compile;

@Environment(EnvType.CLIENT)
public class CfgCommand extends Command {
   public CfgCommand() {
      super(new CommandInfo(".cfg save test | .cfg load test | .cfg dir", "cfg"));
   }

   @Compile
   public void execute(String[] args) {
      if (args.length == 2) {
         String cfgName = args[1];
         if (args[0].equalsIgnoreCase("load")) {
            CrolClient.INSTANCE.getConfigManager().loadConfig(cfgName);
            ChatUtil.addMessage("Success load config " + cfgName);
         } else if (args[0].equalsIgnoreCase("save")) {
            CrolClient.INSTANCE.getConfigManager().saveConfig(cfgName);
            ChatUtil.addMessage("Success saved config " + cfgName);
         } else if (args[0].equalsIgnoreCase("delete")) {
            CrolClient.INSTANCE.getConfigManager().deleteConfig(cfgName);
            ChatUtil.addMessage("Success delete config " + cfgName);
         }
      } else if (args.length == 1 && args[0].equalsIgnoreCase("dir")) {
         File folder = new File("CrolClient\\configs\\");
         Util.getOperatingSystem().open(folder.toURI().toString());
         ChatUtil.addMessage("Success open config directory");
      }

   }
}
