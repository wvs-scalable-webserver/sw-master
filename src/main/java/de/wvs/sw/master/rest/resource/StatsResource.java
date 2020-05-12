package de.wvs.sw.master.rest.resource;

import com.google.gson.Gson;
import de.progme.hermes.server.http.Request;
import de.progme.hermes.server.http.annotation.Path;
import de.progme.hermes.server.http.annotation.Produces;
import de.progme.hermes.server.http.annotation.method.GET;
import de.progme.hermes.shared.ContentType;
import de.progme.hermes.shared.http.Response;
import de.wvs.sw.master.rest.response.MasterResponse;
import de.wvs.sw.master.rest.response.MasterStatsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Marvin Erkes on 04.02.2020.
 */
@Path("/master/stats")
public class StatsResource {

    private static Logger logger = LoggerFactory.getLogger(StatsResource.class);
    private static Gson gson = new Gson();

    @GET
    @Path("")
    @Produces(ContentType.APPLICATION_JSON)
    public Response getStats(Request httpRequest) {

        return Response
                .ok()
                .content(gson.toJson(new MasterStatsResponse(MasterResponse.Status.OK, "OK"))).build();
    }
}
