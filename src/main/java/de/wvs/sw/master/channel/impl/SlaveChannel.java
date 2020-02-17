package de.wvs.sw.master.channel.impl;

import de.progme.thor.client.sub.impl.handler.annotation.Channel;
import de.progme.thor.client.sub.impl.handler.annotation.Key;
import de.progme.thor.client.sub.impl.handler.annotation.Value;
import org.json.JSONObject;

/**
 * Created by Marvin Erkes on 11.02.20.
 */
@Channel("slave")
public class SlaveChannel {

    @Key("connection")
    @Value("connect")
    public void onConnectionConnect(JSONObject data) {

        System.out.println("Slave connected");
    }

    @Key("connection")
    @Value("disconnect")
    public void onConnectionDisconnect(JSONObject data) {

        System.out.println("Slave disconnected");
    }
}
