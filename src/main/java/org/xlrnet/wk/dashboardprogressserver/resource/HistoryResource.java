package org.xlrnet.wk.dashboardprogressserver.resource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xlrnet.wk.dashboardprogressserver.api.entity.HistoricEntry;
import org.xlrnet.wk.dashboardprogressserver.common.InvalidApiKeyException;
import org.xlrnet.wk.dashboardprogressserver.controller.HistoryController;

import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Reverse-engineered resource based on https://community.wanikani.com/t/unsupported-progress-chart-script/20735.
 */
@Slf4j
@Component
@Path("history")
public class HistoryResource {

    private HistoryController historyController;

    @Autowired
    public HistoryResource(HistoryController historyController) {
        this.historyController = historyController;
    }

    @GET
    @Produces("text/csv")
    public Response fetchHistory(@NotNull @QueryParam("api_key") String apiKey) {
        LOGGER.trace("Incoming request using api key: {}", apiKey );
        List<HistoricEntry> historicEntries = null;
        try {
            historicEntries = historyController.fetchHistoryByApiKey(apiKey);
            Response.ResponseBuilder response = Response.ok(historicEntries);
            response.header("Access-Control-Allow-Origin", "https://www.wanikani.com");
            return response.build();
        } catch (InvalidApiKeyException e) {
            LOGGER.error("Invalid api key: {}", apiKey);
            Response.ResponseBuilder response = Response.status(Response.Status.UNAUTHORIZED);
            response.header("Access-Control-Allow-Origin", "https://www.wanikani.com");
            return response.build();
        }
    }

}
