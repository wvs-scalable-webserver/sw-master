package de.wvs.sw.master;

import ch.qos.logback.classic.Level;
import de.progme.athena.Athena;
import de.progme.athena.db.Type;
import de.progme.athena.db.settings.AthenaSettings;
import de.progme.iris.IrisConfig;
import de.progme.iris.config.Header;
import de.progme.iris.config.Key;
import de.progme.thor.client.pub.Publisher;
import de.progme.thor.client.pub.PublisherFactory;
import de.progme.thor.client.sub.Subscriber;
import de.progme.thor.client.sub.SubscriberFactory;
import de.wvs.sw.master.application.ApplicationManager;
import de.wvs.sw.master.channel.ChannelManager;
import de.wvs.sw.master.channel.packets.connection.ReconnectPacket;
import de.wvs.sw.master.command.Command;
import de.wvs.sw.master.command.CommandManager;
import de.wvs.sw.master.command.impl.DebugCommand;
import de.wvs.sw.master.command.impl.EndCommand;
import de.wvs.sw.master.command.impl.HelpCommand;
import de.wvs.sw.master.command.impl.StatsCommand;
import de.wvs.sw.master.rest.RestServer;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.regex.Pattern;

/**
 * Created by Marvin Erkes on 05.02.20.
 */
public class Master {

    @Getter
    public static Master instance;

    private static final String MASTER_PACKAGE_NAME = "de.wvs.sw.master";
    private static final Pattern ARGS_PATTERN = Pattern.compile(" ");
    private static Logger logger = LoggerFactory.getLogger(Master.class);
    private static ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(MASTER_PACKAGE_NAME);

    @Getter
    private IrisConfig config;

    @Getter
    private ScheduledExecutorService scheduledExecutorService;

    @Getter
    private CommandManager commandManager;

    private Scanner scanner;

    @Getter
    private Athena athena;

    @Getter
    private Publisher publisher;
    @Getter
    private Subscriber subscriber;

    private ChannelManager channelManager;

    private RestServer restServer;

    @Getter
    private ApplicationManager applicationManager;

    public Master(IrisConfig config) {

        Master.instance = this;

        this.config = config;
    }

    public void start() {

        this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

        this.commandManager = new CommandManager();
        commandManager.addCommand(new HelpCommand("help", "List of available commands", "h"));
        commandManager.addCommand(new EndCommand("end", "Stops the load balancer", "stop", "exit"));
        commandManager.addCommand(new DebugCommand("debug", "Turns the debug mode on/off", "d"));
        commandManager.addCommand(new StatsCommand("stats", "Shows live stats", "s"));

        this.setupDatabase();
        this.connectDatabase();

        this.startThor();
        this.startCommunication();

        this.startRestServer();

        this.startApplicationManager();

        this.channelManager.send(new ReconnectPacket());
    }

    public void stop() {

        logger.info("Master is going to be stopped");

        // Close the scanner
        scanner.close();

        this.stopRestServer();

        this.stopThor();

        this.disonnectDatabase();

        this.scheduledExecutorService.shutdown();

        logger.info("Master has been stopped");
    }

    private void startRestServer() {
        restServer = new RestServer(this.config);
        restServer.start();
    }

    private void stopRestServer() {
        try {
            this.restServer.stop();
        } catch (Exception e) {
            logger.warn("RESTful API server already stopped");
        }
    }

    private void setupDatabase() {
        Header mysqlHeader = this.config.getHeader("mysql");
        Key hostKey = mysqlHeader.getKey("host");
        Key databaseKey = mysqlHeader.getKey("database");
        Key userKey = mysqlHeader.getKey("user");
        Key passwordKey = mysqlHeader.getKey("password");
        this.athena = new Athena(new AthenaSettings.Builder()
                .host(hostKey.getValue(0).asString())
                .port(hostKey.getValue(1).asInt())
                .database(databaseKey.getValue(0).asString())
                .user(userKey.getValue(0).asString())
                .password(passwordKey.getValue(0).asString())
                .type(Type.MYSQL)
                .build());
    }

    private void connectDatabase() {
        this.athena.connect();
    }

    private void disonnectDatabase() {
        this.athena.close();
    }

    private void startThor() {
        Header config = this.config.getHeader("thor");
        Key hostKey = config.getKey("host");
        String host = hostKey.getValue(0).asString();
        int port = hostKey.getValue(1).asInt();
        this.publisher = PublisherFactory.create(host, port);
        this.subscriber = SubscriberFactory.create(host, port);

        logger.warn("Thor started");
    }

    private void startCommunication() {
        this.channelManager = new ChannelManager();
        this.channelManager.subscribe();
    }

    private void stopThor() {
        this.publisher.disconnect();
        this.subscriber.disconnect();
    }

    private void startApplicationManager() {

        this.applicationManager = new ApplicationManager();
    }

    public void console() {

        scanner = new Scanner(System.in);

        try {
            String line;
            while ((line = scanner.nextLine()) != null) {
                if (!line.isEmpty()) {
                    String[] split = ARGS_PATTERN.split(line);

                    if (split.length == 0) {
                        continue;
                    }

                    // Get the command name
                    String commandName = split[0].toLowerCase();

                    // Try to get the command with the name
                    Command command = commandManager.findCommand(commandName);

                    if (command != null) {
                        logger.info("Executing command: {}", line);

                        String[] cmdArgs = Arrays.copyOfRange(split, 1, split.length);
                        command.execute(cmdArgs);
                    } else {
                        logger.info("Command not found!");
                    }
                }
            }
        } catch (IllegalStateException ignore) {}
    }

    public void changeDebug(Level level) {

        // Set the log level to debug or info based on the config value
        rootLogger.setLevel(level);

        logger.info("Logger level is now {}", rootLogger.getLevel());
    }

    public void changeDebug() {

        // Change the log level based on the current level
        changeDebug((rootLogger.getLevel() == Level.INFO) ? Level.DEBUG : Level.INFO);
    }
}
