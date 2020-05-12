package de.wvs.sw.master.channel.impl;

import com.google.gson.Gson;
import de.progme.thor.client.sub.impl.handler.annotation.Channel;
import de.progme.thor.client.sub.impl.handler.annotation.Key;
import de.progme.thor.client.sub.impl.handler.annotation.Value;
import de.wvs.sw.master.Master;
import de.wvs.sw.shared.application.Application;
import de.wvs.sw.shared.application.Deployment;
import de.wvs.sw.shared.application.SWSlave;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Marvin Erkes on 11.02.20.
 */
@Channel("application")
public class ApplicationChannel {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationChannel.class);

    private static final Gson gson = new Gson();

    @Key("deployment")
    @Value("status")
    public void onDeploymentStatus(JSONObject data) {
        String deploymentUuid = data.getString("deploymentUuid");
        Deployment.Status status = Deployment.Status.valueOf(data.getString("status"));
        Master.getInstance().getApplicationManager().changeDeploymentStatus(deploymentUuid, status);
    }
}
