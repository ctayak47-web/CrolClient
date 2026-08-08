
package crol.client.base.comand.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.CommandSource;
import crol.client.base.comand.api.CommandAbstract;
import crol.client.base.comand.impl.args.CoordinateArgumentType;
import crol.client.modules.impl.utility.GPS;
import crol.client.utility.game.other.MessageUtil;

public class GpsCommand
extends CommandAbstract {
    public GpsCommand() {
        super("gps");
        if (!GPS.INSTANCE.isEnabled()) {
            GPS.INSTANCE.setToggled(true);
        }
    }

    @Override
    public void execute(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(GpsCommand.literal("off").executes(context -> {
            GPS.clearTarget();
            MessageUtil.displayInfo("§aGPS метка снята");
            return 1;
        }));
        builder.then(GpsCommand.arg("x", CoordinateArgumentType.create("X")).then(GpsCommand.arg("z", CoordinateArgumentType.create("Z")).executes(context -> {
            double x = (Double)context.getArgument("x", Double.class);
            double z = (Double)context.getArgument("z", Double.class);
            double y = GpsCommand.mc.player != null ? GpsCommand.mc.player.getY() : 0.0;
            GPS.setTarget(x, y, z, "Manual");
            if (!GPS.INSTANCE.isEnabled()) {
                GPS.INSTANCE.setToggled(true);
            }
            MessageUtil.displayInfo("§aGPS метка установлена на §e[" + x + " " + z + "]");
            return 1;
        })));
        builder.executes(context -> {
            MessageUtil.displayInfo("§e.gps <X> <Z> §7- установить метку");
            MessageUtil.displayInfo("§e.gps off §7- убрать метку");
            return 1;
        });
    }
}

