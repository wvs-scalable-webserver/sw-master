package de.wvs.sw.master;

import de.progme.iris.IrisConfig;

/**
 * Created by Marvin Erkes on 05.02.20.
 */
public class MasterFactory {

    public MasterFactory() {}

    public static Master create(IrisConfig config) {

        return new Master(config);
    }
}
