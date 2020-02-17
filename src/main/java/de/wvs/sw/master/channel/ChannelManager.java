package de.wvs.sw.master.channel;

import de.progme.thor.client.pub.Publisher;
import de.progme.thor.client.sub.Subscriber;
import de.wvs.sw.master.Master;
import de.wvs.sw.master.channel.impl.SlaveChannel;

/**
 * Created by Marvin Erkes on 11.02.20.
 */
public class ChannelManager {

    private Subscriber subscriber;
    private Publisher publisher;

    public ChannelManager() {

        Master master = Master.getInstance();
        this.subscriber = master.getSubscriber();
        this.publisher = master.getPublisher();
    }

    public void subscribe() {

        this.subscriber.subscribeMulti(SlaveChannel.class);
    }

    public void send(Packet packet) {

        this.publisher.publish(packet.getChannel(), packet.getData());
    }
}
