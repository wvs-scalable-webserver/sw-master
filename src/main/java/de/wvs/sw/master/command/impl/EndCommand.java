package de.wvs.sw.master.command.impl;

import de.wvs.sw.master.Master;
import de.wvs.sw.master.command.Command;

/**
 * Created by Marvin Erkes on 04.02.2020.
 */
public class EndCommand extends Command {

    public EndCommand(String name, String description, String... aliases) {

        super(name, description, aliases);
    }

    @Override
    public boolean execute(String[] args) {

        Master.getInstance().stop();

        return true;
    }
}
