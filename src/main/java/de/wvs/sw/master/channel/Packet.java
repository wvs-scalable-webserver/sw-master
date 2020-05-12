package de.wvs.sw.master.channel;

import lombok.Getter;
import org.json.JSONObject;

/**
 * Created by Marvin Erkes on 11.02.20.
 */
public class Packet {

    @Getter
    private String channel;
    @Getter
    private String subscriber;
    @Getter
    protected JSONObject data;

    public Packet(String channel) {

        this.channel = channel;
        this.data = new JSONObject();
    }

    public Packet(String channel, String subscriber) {

        this.channel = channel;
        this.subscriber = subscriber;
        this.data = new JSONObject();
    }
}
