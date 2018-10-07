package org.xlrnet.wk.dashboardprogressserver.resource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xlrnet.wk.dashboardprogressserver.api.entity.HistoricEntry;
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
        /*HistoricEntry historicEntry = new HistoricEntry();
        historicEntry.setApprentice1(10);
        historicEntry.setApprentice2(9);
        historicEntry.setApprentice3(8);
        historicEntry.setApprentice4(7);
        historicEntry.setGuru1(6);
        historicEntry.setGuru2(5);
        historicEntry.setMaster(4);
        historicEntry.setEnlightened(3);
        historicEntry.setBurned(2);
        historicEntry.setEpochSeconds(System.currentTimeMillis() - 10000);
        HistoricEntry historicEntry2 = new HistoricEntry();
        historicEntry2.setApprentice1(100);
        historicEntry2.setApprentice2(9);
        historicEntry2.setApprentice3(8);
        historicEntry2.setApprentice4(7);
        historicEntry2.setGuru1(6);
        historicEntry2.setGuru2(5);
        historicEntry2.setMaster(4);
        historicEntry2.setEnlightened(3);
        historicEntry2.setBurned(2);
        historicEntry2.setEpochSeconds(System.currentTimeMillis());
        ArrayList<HistoricEntry> objects = new ArrayList<>();
        objects.add(historicEntry);
        objects.add(historicEntry2);*/
        /*Response.ResponseBuilder response = Response.ok("UTCDateTime,EpochSeconds,UserLevel,Total,LeechTotal,Apprentice1,Apprentice2,Apprentice3,Apprentice4,Guru1,Guru2,Master,Enlightened,Burned,LeechApprentice1,LeechApprentice2,LeechApprentice3,LeechApprentice4,LeechGuru1,LeechGuru2,LeechMaster,LeechEnlightened,LeechBurned\n" +
                "2017-10-23 07:09:45,1508742585,6,671,27,0,0,1,6,35,106,200,323,0,0,0,1,4,8,11,3,0,0\n" +
                "2018-10-23 07:09:45,1508742585,6,671,27,0,0,1,6,35,106,200,323,0,0,0,1,4,8,11,3,0,0\n" +
                "\n");*/
        List<HistoricEntry> historicEntries = historyController.fetchHistoryByApiKey(apiKey);
        Response.ResponseBuilder response = Response.ok(historicEntries);
        response.header("Access-Control-Allow-Origin", "https://www.wanikani.com");
        return response.build();
    }

}
