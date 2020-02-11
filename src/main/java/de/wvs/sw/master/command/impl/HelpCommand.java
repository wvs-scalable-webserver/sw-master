package de.wvs.sw.master.command.impl;

import de.wvs.sw.master.Master;
import de.wvs.sw.master.command.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Marvin Erkes on 04.02.2020.
 */
public class HelpCommand extends Command {

    private static Logger logger = LoggerFactory.getLogger(HelpCommand.class);

    public HelpCommand(String name, String description, String... aliases) {

        super(name, description, aliases);
    }

    @Override
    public boolean execute(String[] args) {

        logger.info("Available Commands:");
        for (Command command : Master.getInstance().getCommandManager().getCommands()) {
            logger.info("{} [{}] - {}", command.getName(), String.join(", ", command.getAliases()), command.getDescription());
        }

        return true;
    }
}
