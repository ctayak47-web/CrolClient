package ru.crolclient.api.system.logger.implement;

import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import ru.crolclient.api.system.logger.Logger;

public class ConsoleLogger implements Logger {
    private final org.apache.logging.log4j.Logger logger = LogManager.getLogger("Extra");

    @Override
    public void log(Object message) {
        logger.info("[" + "Extra" + "] " + message);
    }

    @Override
    public void minecraftLog(Text... components) {

    }
}
