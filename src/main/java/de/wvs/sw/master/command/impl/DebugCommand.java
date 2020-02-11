package de.wvs.sw.master.command.impl;

import de.wvs.sw.master.Master;
import de.wvs.sw.master.command.Command;

/**
 * Created by Marvin Erkes on 04.02.2020.
 */
public class DebugCommand extends Command {

    public DebugCommand(String name, String description, String... aliases) {

        super(name, description, aliases);
    }

    @Override
    public boolean execute(String[] args) {

        Master.getInstance().changeDebug();

        return true;
    }
}
