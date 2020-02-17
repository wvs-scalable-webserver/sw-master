package de.wvs.sw.master.channel.packets.connection;

import de.wvs.sw.master.channel.Packet;

/**
 * Created by Marvin Erkes on 11.02.20.
 */
public class ReconnectPacket extends Packet {

    public ReconnectPacket() {
        super("master");

        this.data.put("connection", "reconnect");
    }
}
