package crol.client.ui.commands.impl;

import crol.client.CrolClient;
import crol.client.modules.Module;
import crol.client.ui.commands.Command;
import crol.client.ui.commands.CommandInfo;
import crol.client.util.other.BindUtill;
import crol.client.util.player.ChatUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class BindCommand extends Command {
   public BindCommand() {
      super(new CommandInfo(".bind all clear | .bind add Aura f | .bind remove Aura", "bind"));
   }

   public void execute(String[] args) {
      if (args.length >= 2) {
         if (args[0].equalsIgnoreCase("all") && args[1].equalsIgnoreCase("clear")) {
            for(Module m : CrolClient.INSTANCE.getModuleManager().getModules()) {
               m.setBind(0);
            }

            ChatUtil.addMessage("All binds cleared!");
         } else if (args[0].equalsIgnoreCase("add")) {
            Module module = this.getByName(args[1]);
            if (module != null && args[2] != null) {
               module.setBind(BindUtill.getBind(args[2].toLowerCase()));
               ChatUtil.addMessage("Successfully set bind " + args[2] + " to module " + args[1] + "!");
            }
         } else if (args[0].equalsIgnoreCase("remove")) {
            Module module = this.getByName(args[1]);
            if (module != null) {
               module.setBind(0);
               ChatUtil.addMessage("Successfully unbind module " + args[1] + "!");
            }
         }
      }

   }

   private Module getByName(String name) {
      return (Module)CrolClient.INSTANCE.getModuleManager().getModules().stream().filter((module) -> module.getModuleInfo().name().equalsIgnoreCase(name)).findFirst().orElse((Module) null);
   }
}
