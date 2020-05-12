package de.wvs.sw.master.rest.resource;

import com.google.gson.Gson;
import de.progme.hermes.server.http.Request;
import de.progme.hermes.server.http.annotation.Path;
import de.progme.hermes.server.http.annotation.PathParam;
import de.progme.hermes.server.http.annotation.Produces;
import de.progme.hermes.server.http.annotation.method.GET;
import de.progme.hermes.server.http.annotation.method.POST;
import de.progme.hermes.shared.ContentType;
import de.progme.hermes.shared.Status;
import de.progme.hermes.shared.http.Response;
import de.progme.iris.config.Value;
import de.wvs.sw.master.Master;
import de.wvs.sw.master.rest.response.MasterResponse;
import de.wvs.sw.master.rest.response.MasterStatsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

/**
 * Created by Marvin Erkes on 20.04.20.
 */
@Path("/master/application")
public class ApplicationResource {

    private static Logger logger = LoggerFactory.getLogger(ApplicationResource.class);
    private static Gson gson = new Gson();

    @GET
    @Path("/{applicationId}/bundles/{bundleId}")
    @Produces(ContentType.APPLICATION_OCTET_STREAM)
    public Response getBundle(Request httpRequest, @PathParam String applicationId, @PathParam String bundleId) {

        String repository = Master.getInstance().getConfig()
                .getHeader("general")
                .getKey("repository")
                .getValues()
                .stream()
                .map((Value::asString))
                .collect(Collectors.joining(" "));

        File bundle = new File(repository + "/applications/" + applicationId + "/bundles/" + bundleId + ".zip");
        if(!bundle.exists()) return Response.status(Status.NOT_FOUND).content("Bundle not found").build();

        return Response.file(bundle);
    }

    @POST
    @Path("/{applicationId}/bundles/{bundleTag}")
    @Produces(ContentType.APPLICATION_JSON)
    public Response createBundle(Request httpRequest, @PathParam String applicationId, @PathParam String bundleTag) {

        String repository = Master.getInstance().getConfig()
                .getHeader("general")
                .getKey("repository")
                .getValues()
                .stream()
                .map((Value::asString))
                .collect(Collectors.joining(" "));

        try {
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(repository + "/applications/" + applicationId + "/bundles/" + bundleTag + ".zip"), StandardCharsets.UTF_8);
            writer.write(httpRequest.body());
            writer.close();
        } catch (IOException exception) {
            logger.error("Error while writing bundle:", exception);
            return Response
                    .status(Status.INTERNAL_SERVER_ERROR)
                    .content(gson.toJson(new MasterResponse(MasterResponse.Status.ERROR, "Error while creating bundle!")))
                    .build();
        }

        return Response
                .ok()
                .content(gson.toJson(new MasterResponse(MasterResponse.Status.OK, "Bundle created")))
                .build();
    }
}