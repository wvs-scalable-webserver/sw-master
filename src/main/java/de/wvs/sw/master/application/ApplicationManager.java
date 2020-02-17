package de.wvs.sw.master.application;

import de.progme.athena.Athena;
import de.progme.athena.db.serialization.SerializationManager;
import de.progme.athena.query.core.CreateQuery;
import de.progme.athena.query.core.SelectQuery;
import de.progme.iris.IrisConfig;
import de.progme.iris.config.Header;
import de.wvs.sw.master.Master;
import de.wvs.sw.shared.application.Application;
import de.wvs.sw.shared.application.SWSlave;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by Marvin Erkes on 12.02.20.
 */
public class ApplicationManager {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationManager.class);

    private Athena athena;
    private SerializationManager serializationManager;
    private List<Application> applications;
    private Map<String, SWSlave> slaves;

    public ApplicationManager() {

        this.applications = new ArrayList<>();
        this.slaves = new HashMap<>();

        this.athena = Master.getInstance().getAthena();
        this.serializationManager = this.athena.serializationManager();
        this.initialize();

        this.loadApplications();
        Master.getInstance().getScheduledExecutorService().scheduleAtFixedRate(this::checkSlaves, 0, 10, TimeUnit.SECONDS);
    }

    private void initialize() {
        this.serializationManager.create(Application.class);
    }

    public void integrateSlave(SWSlave slave) {
        this.slaves.put(slave.getUuid().toString(), slave);
    }

    public void excludeSlave(SWSlave slave) {
        this.slaves.remove(slave.getUuid().toString());
    }

    public void onSlaveHeartbeat(SWSlave slave) {
        SWSlave matchedSlave = this.slaves.get(slave.getUuid().toString());
        if(matchedSlave == null) return;
        matchedSlave.setLastHeartbeat(new Date());
    }

    public void startApplication(Application application) {

    }

    private void loadApplications() {
        this.applications = this.serializationManager.select(Application.class);
    }

    private void checkSlaves() {
        logger.debug("Checking slaves..");
        this.slaves.values().forEach((slave) -> {
            logger.debug("Checking slave[{}]", slave.getUuid().toString());
            if (!slave.isAlive()) {
                logger.error("Slave[{}] is not alive", slave.getUuid().toString());
            }
        });
    }
}
