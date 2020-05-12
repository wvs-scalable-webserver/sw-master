package de.wvs.sw.master.channel.packets.application;

import com.google.gson.Gson;
import de.wvs.sw.master.channel.Packet;
import de.wvs.sw.shared.application.Deployment;
import de.wvs.sw.shared.application.SWSlave;

/**
 * Created by Marvin Erkes on 19.02.20.
 */
public class DeployPacket  extends Packet {

    private static Gson gson = new Gson();

    public DeployPacket(SWSlave slave, Deployment deployment) {
        super("master", "slave-" + slave.getUuid().toString());

        this.data.put("application", "deploy");
        this.data.put("deployment", gson.toJson(deployment));
    }
}