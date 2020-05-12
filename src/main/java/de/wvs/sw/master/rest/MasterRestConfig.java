package de.wvs.sw.master.rest;

import de.progme.hermes.server.impl.HermesConfig;
import de.wvs.sw.master.rest.resource.ApplicationResource;
import de.wvs.sw.master.rest.resource.StatsResource;

/**
 * Created by Marvin Erkes on 04.02.2020.
 */
public class MasterRestConfig extends HermesConfig {

    public MasterRestConfig(String host, int port) {

        host(host);
        port(port);
        corePoolSize(2);
        maxPoolSize(4);
        backLog(50);
        register(StatsResource.class);
        register(ApplicationResource.class);
    }
}
