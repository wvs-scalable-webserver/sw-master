package de.wvs.sw.master.application.module;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import de.progme.hermes.client.HermesClient;
import de.progme.hermes.client.HermesClientFactory;
import de.progme.hermes.shared.http.Body;
import de.progme.hermes.shared.http.Headers;
import de.progme.hermes.shared.http.Response;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Marvin Erkes on 11.05.20.
 */
public class LoadBalancerModule {

    private Gson gson;
    private HermesClient restClient;

    public LoadBalancerModule(String host) {
        this.gson = new Gson();
        this.restClient = HermesClientFactory.create(host + "/loadbalancer");
    }

    public List<BackendServer> getBackendServers() throws IOException {
        Response response = this.restClient.get("/list", Headers.empty());
        JSONObject body = new JSONObject(response.body().content());
        Type backendServersType = new TypeToken<List<BackendServer>>(){}.getType();
        return gson.fromJson(body.getJSONArray("backendInfo").toString(), backendServersType);
    }

    public void createBackendServer(BackendServer backendServer) throws IOException {
        this.restClient.post("/add/" + backendServer.getName() + "/" + backendServer.getHost() + "/" + backendServer.getPort(), "", Headers.empty());
    }

    public void removeBackendServer(BackendServer backendServer) throws IOException {
        this.restClient.delete("/remove/" + backendServer.getName(), Headers.empty());
    }

    public void updateBackendServers(List<BackendServer> backendServers) throws IOException {
        List<BackendServer> existingBackendServer = this.getBackendServers();
        List<String> existingBackendServerNames = existingBackendServer
                .stream()
                .map(BackendServer::getName)
                .collect(Collectors.toList());
        List<String> backendServerNames = backendServers
                .stream()
                .map(BackendServer::getName)
                .collect(Collectors.toList());
        List<BackendServer> newBackendServers = backendServers
                .stream()
                .filter(backendServer -> !existingBackendServerNames.contains(backendServer.getName()))
                .collect(Collectors.toList());
        List<BackendServer> removedBackendServers = existingBackendServer
                .stream()
                .filter(backendServer -> !backendServerNames.contains(backendServer.getName()))
                .collect(Collectors.toList());

        for (BackendServer backendServer : newBackendServers) {
            this.createBackendServer(backendServer);
        }

        for (BackendServer backendServer : removedBackendServers) {
            this.removeBackendServer(backendServer);
        }
    }

    @RequiredArgsConstructor
    public static class BackendServer {

        @Getter
        private final String name;

        @Getter
        private final String host;

        @Getter
        private final int port;

        @Getter
        private double connectTime;
    }
}
