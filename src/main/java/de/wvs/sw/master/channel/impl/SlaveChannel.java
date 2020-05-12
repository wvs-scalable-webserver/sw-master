package de.wvs.sw.master.channel.impl;

import com.google.gson.Gson;
import de.progme.thor.client.sub.impl.handler.annotation.Channel;
import de.progme.thor.client.sub.impl.handler.annotation.Key;
import de.progme.thor.client.sub.impl.handler.annotation.Value;
import de.wvs.sw.master.Master;
import de.wvs.sw.shared.application.SWSlave;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by Marvin Erkes on 11.02.20.
 */
@Channel("slave")
public class SlaveChannel {

    private static final Gson gson = new Gson();

    @Key("connection")
    @Value("connect")
    public void onConnectionConnect(JSONObject data) {
        SWSlave slave = gson.fromJson(data.getString("slave"), SWSlave.class);
        Master.getInstance().getApplicationManager().integrateSlave(slave);
    }

    @Key("connection")
    @Value("disconnect")
    public void onConnectionDisconnect(JSONObject data) {
        SWSlave slave =  gson.fromJson(data.getString("slave"), SWSlave.class);
        Master.getInstance().getApplicationManager().excludeSlave(slave);
    }

    @Key("connection")
    @Value("heartbeat")
    public void onConnectionHeartbeat(JSONObject data) {
        SWSlave slave =  gson.fromJson(data.getString("slave"), SWSlave.class);
        Master.getInstance().getApplicationManager().onSlaveHeartbeat(slave);
    }
}
