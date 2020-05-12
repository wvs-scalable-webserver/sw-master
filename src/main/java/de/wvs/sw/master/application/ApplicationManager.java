package de.wvs.sw.master.application;

import com.google.gson.Gson;
import de.progme.athena.Athena;
import de.progme.athena.db.serialization.Condition;
import de.progme.athena.db.serialization.SerializationManager;
import de.wvs.sw.master.Master;
import de.wvs.sw.master.application.module.LoadBalancerModule;
import de.wvs.sw.master.channel.packets.application.DeployPacket;
import de.wvs.sw.shared.application.Application;
import de.wvs.sw.shared.application.Deployment;
import de.wvs.sw.shared.application.SWSlave;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by Marvin Erkes on 12.02.20.
 */
public class ApplicationManager {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationManager.class);

    private Athena athena;
    private SerializationManager serializationManager;
    private CopyOnWriteArrayList<Application> applications;
    private CopyOnWriteArrayList<Deployment> deployments;
    private final ConcurrentHashMap<String, SWSlave> slaves;
    private LoadBalancerModule loadBalancerModule;

    public ApplicationManager() {

        this.slaves = new ConcurrentHashMap<>();

        this.initialize();
    }

    private void initialize() {

        this.athena = Master.getInstance().getAthena();
        this.serializationManager = this.athena.serializationManager();
        this.serializationManager.create(Application.class);
        this.serializationManager.create(Deployment.class);
        this.loadApplications();

        this.loadBalancerModule = new LoadBalancerModule("http://localhost:5000");

        Master.getInstance().getScheduledExecutorService().scheduleAtFixedRate(this::updateLoadBalancerEndpoints, 0, 10, TimeUnit.SECONDS);
        Master.getInstance().getScheduledExecutorService().scheduleAtFixedRate(this::checkDeployments, 0, 1, TimeUnit.SECONDS);
        Master.getInstance().getScheduledExecutorService().scheduleAtFixedRate(this::checkSlaves, 0, 10, TimeUnit.SECONDS);
    }

    public void integrateSlave(SWSlave slave) {
        slave.setLastHeartbeat(new Date());
        slave.setStatus(SWSlave.Status.HEALTHY);
        this.slaves.put(slave.getUuid().toString(), slave);
        logger.info("Integrated slave[{}]", slave.getUuid().toString());
    }

    public void excludeSlave(SWSlave slave) {
        logger.info("Excluding slave[{}]", slave.getUuid().toString());

        this.slaves.remove(slave.getUuid().toString());

        this.deployments = this.deployments
                .stream()
                .filter(deployment -> {
                    boolean filter = !deployment.getSlave().equals(slave.getUuid().toString());
                    if(!filter) {
                        this.changeDeploymentStatus(deployment, Deployment.Status.TERMINATED);
                    }
                    return filter;
                })
                .collect(Collectors.toCollection(CopyOnWriteArrayList::new));
        this.checkDeployments();
    }

    public void onSlaveHeartbeat(SWSlave slave) {
        SWSlave matchedSlave = this.slaves.get(slave.getUuid().toString());
        if(matchedSlave == null) return;

        matchedSlave.setLastHeartbeat(new Date());
    }

    public SWSlave getSlaveWithLowestUsage() {
        Map<String, Integer> slavesWithDeployments = this.slaves
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> 0));
        for (Deployment deployment : this.deployments) {
            String slave = deployment.getSlave();
            int deployments = slavesWithDeployments.containsKey(slave) ? slavesWithDeployments.get(slave) +1 : 1;
            slavesWithDeployments.put(deployment.getSlave(), deployments);
        }

        if (slavesWithDeployments.size() <= 0)
            return null;
        else
            return this.slaves.get(Collections.min(slavesWithDeployments.entrySet(), Map.Entry.comparingByValue()).getKey());
    }

    public void checkDeployments() {
        this.applications.forEach(application -> {
            List<Deployment> deployments = this.deployments.stream()
                    .filter(deployment -> deployment.getStatus() != Deployment.Status.TERMINATED)
                    .filter(deployment -> deployment.getApplication() == application.getId())
                    .collect(Collectors.toList());
            int requiredAmount = application.getMinimum() - deployments.size();
            if (requiredAmount <= 0) return;

            logger.info("About to create {} deployments of {}.", requiredAmount, application.getName());

            this.startApplication(application, requiredAmount);
        });
    }

    public void startApplication(Application application, int amount) {
        for (int i = 0; i < amount; i++) {
            this.deployApplication(application);
        }
    }

    public void deployApplication(Application application) {
        SWSlave slave = this.getSlaveWithLowestUsage();

        if (slave == null) {
            logger.error("No slave found");
            return;
        }

        Deployment deployment = new Deployment(0, UUID.randomUUID().toString(), application.getId(), slave.getUuid().toString(), Deployment.Status.WAITING, Deployment.Usage.LOW, slave.getHost(), this.getAvailablePort(slave));
        this.serializationManager.insert(deployment);
        deployment.setApplicationRef(application);
        this.deployments.add(deployment);

        Master.getInstance().getChannelManager().send(new DeployPacket(slave, deployment));
    }

    private int getAvailablePort(SWSlave slave) {
        List<Integer> usedPorts = this.deployments
                .stream()
                .filter(deployment -> slave.getUuid().toString().equals(deployment.getSlave()))
                .map(Deployment::getPort)
                .collect(Collectors.toList());
        int random = slave.getMinPort() + new Random().nextInt(slave.getMaxPort() - slave.getMinPort() + 1 - usedPorts.size());
        for (int ex : usedPorts) {
            if (random < ex) {
                break;
            }
            random++;
        }
        return random;
    }

    private void loadApplications() {
        this.applications = new CopyOnWriteArrayList<>(this.serializationManager.select(Application.class));
        this.deployments = new CopyOnWriteArrayList<>(this.serializationManager.select(Deployment.class, new Condition("status", Condition.Operator.NOT_EQUAL, String.valueOf(Deployment.Status.TERMINATED.getValue()))));
        //Map<String, List<Deployment>> deploymentsGroupedBySlave = this.deployments.stream().collect(Collectors.groupingBy(Deployment::getSlave));
        this.deployments.forEach(deployment -> {
            if (this.slaves.containsKey(deployment.getSlave())) return;
            this.integrateSlave(new SWSlave(UUID.fromString(deployment.getSlave()), SWSlave.Status.HEALTHY, deployment.getHost()));
        });
    }

    private void checkSlaves() {

        if(this.slaves.size() <= 0) return;

        logger.debug("Checking slaves..");
        this.slaves.values().forEach((slave) -> {
            logger.debug("Checking slave[{}]..", slave.getUuid().toString());
            if (!slave.isAlive()) {
                if (slave.getStatus() == SWSlave.Status.UNHEALTHY) {
                    this.excludeSlave(slave);
                } else {
                    logger.error("Slave[{}] is not alive!", slave.getUuid().toString());
                    slave.setStatus(SWSlave.Status.UNHEALTHY);
                }
            } else if (slave.getStatus() == SWSlave.Status.UNHEALTHY) {
                logger.info("Slave[{}] went back alive.", slave.getUuid().toString());
                slave.setStatus(SWSlave.Status.HEALTHY);
            }
        });
    }

    public void changeDeploymentStatus(String uuid, Deployment.Status status) {
        Optional<Deployment> matchedDeployment = this.deployments
                .stream()
                .parallel()
                .filter(deployment -> uuid.equals(deployment.getUuid()))
                .findAny();
        if (!matchedDeployment.isPresent()) return;

        this.changeDeploymentStatus(matchedDeployment.get(), status);
    }

    public void changeDeploymentStatus(Deployment deployment, Deployment.Status status) {

        if (deployment.getStatus() == status) return;

        deployment.setStatus(status);
        this.updateDeployment(deployment);
        try {
            LoadBalancerModule.BackendServer backendServer = new LoadBalancerModule.BackendServer(deployment.getUuid(), deployment.getHost(), deployment.getPort());
            switch (status) {
                case RUNNING:
                    this.loadBalancerModule.createBackendServer(backendServer);
                    break;
                case TERMINATING:
                case TERMINATED:
                    this.deployments = this.deployments
                            .stream()
                            .filter(d -> !d.getUuid().equals(deployment.getUuid()))
                            .collect(Collectors.toCollection(CopyOnWriteArrayList::new));
                    this.loadBalancerModule.removeBackendServer(backendServer);
            }
        } catch (IOException error) {
            logger.error("Error while adding deployment to load balancer!", error);
        }

        logger.debug("Deployment {} changed status to \"{}\".", deployment.getUuid(), status);
    }

    private void updateLoadBalancerEndpoints() {
        try {
            this.loadBalancerModule.updateBackendServers(this.deployments
                    .stream()
                    .filter(deployment -> deployment.getStatus() == Deployment.Status.RUNNING)
                    .map(deployment -> new LoadBalancerModule.BackendServer(deployment.getUuid(), deployment.getHost(), deployment.getPort()))
                    .collect(Collectors.toList())
            );
        } catch (IOException error) {
            logger.error("Error while adding deployments to load balancer!", error);
        }
    }

    private void updateDeployment(Deployment deployment) {
        this.serializationManager.update(deployment, new Condition("uuid", Condition.Operator.EQUAL, deployment.getUuid()));
    }
}
