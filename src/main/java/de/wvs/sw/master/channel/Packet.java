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
    protected JSONObject data;

    public Packet(String channel) {

        this.channel = channel;
        this.data = new JSONObject();
    }
}
