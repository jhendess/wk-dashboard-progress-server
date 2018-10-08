package org.xlrnet.wk.dashboardprogressserver.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.xlrnet.wk.dashboardprogressserver.api.wk.WkAssignmentCollection;
import org.xlrnet.wk.dashboardprogressserver.api.wk.WkAssignmentResource;
import org.xlrnet.wk.dashboardprogressserver.api.wk.WkUser;
import org.xlrnet.wk.dashboardprogressserver.api.wk.WkUserResource;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

@Slf4j
@Service
public class WaniKaniAccessService {

    private static final String USER_ENDPOINT = "user";

    private static final String ASSIGNMENTS_ENDPOINT = "assignments";

    private static final Duration DEFAULT_WAIT_TIMEOUT = Duration.ofSeconds(5);

    private static final String BASE_URL = "https://api.wanikani.com/v2/";

    @Getter
    private WebClient client;

    /** Jackson reader for the {@link WkAssignmentCollection} objects. */
    private ObjectReader wkAssignmentReader;

    @PostConstruct
    public void initialize() {
        client = WebClient.create(BASE_URL);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        wkAssignmentReader = objectMapper.readerFor(WkAssignmentCollection.class);
    }

    public WkUser findUser(String apiKey) {
        LOGGER.debug("Requesting user info for API key {}", apiKey);
        WebClient.RequestHeadersSpec<?> request = getClient().get().uri(USER_ENDPOINT);
        prepareHeader(request, apiKey);
        WkUserResource userWkResource = request.retrieve().bodyToMono(WkUserResource.class).block();
        return userWkResource.getData();
    }

    public Map<Integer, Integer> fetchLatestSrsLevelData(String apiKey) {
        LOGGER.debug("Fetching latest SRS level data for key {}", apiKey);
        Map<Integer, Integer> levelData = new HashMap<>();

        /*WkAssignmentCollection assignmentCollection = fetchAssignments(apiKey, BASE_URL + ASSIGNMENTS_ENDPOINT);
        updateLevelData(levelData, assignmentCollection);
        
        if (assignmentCollection.getPageInfo() != null && assignmentCollection.getPageInfo().getNextUrl() != null) {
            String nextUrl = assignmentCollection.getPageInfo().getNextUrl();
            do {
                LOGGER.debug("Fetching more available data for key {}", apiKey);

                assignmentCollection = fetchAssignments(apiKey, nextUrl);
                updateLevelData(levelData, assignmentCollection);

                nextUrl = assignmentCollection.getPageInfo() != null ? assignmentCollection.getPageInfo().getNextUrl() : null;
            } while (nextUrl != null);

        }*/

        IntStream.rangeClosed(0, 9)
                .parallel()
                .forEach(i -> {
                    WkAssignmentCollection assignments = fetchAssignments(apiKey, BASE_URL + ASSIGNMENTS_ENDPOINT + "?srs_stages=" + i);
                    levelData.put(i, assignments.getTotalCount());
                });

        return levelData;
    }

    private WkAssignmentCollection fetchAssignments(String apiKey, String nextUrl) {
        WkAssignmentCollection assignmentCollection;
        URLConnection urlConnection = null;
        try {
            URL url = new URL(nextUrl);
            urlConnection = url.openConnection();
            urlConnection.addRequestProperty("Authorization", "Bearer " + apiKey);
            urlConnection.setConnectTimeout(3000);
            urlConnection.connect();
            LOGGER.trace("Opening connection for {}", nextUrl);
            InputStream inputStream = urlConnection.getInputStream();
            assignmentCollection = wkAssignmentReader.readValue(inputStream);
            LOGGER.trace("Received data for {}", nextUrl);
        } catch (IOException e) {
            LOGGER.error("Unexpected error while trying to fetch assignments");
            throw new RuntimeException(e);
        } finally {
            IOUtils.close(urlConnection);
        }
        return assignmentCollection;
    }

    private void updateLevelData(Map<Integer, Integer> levelData, WkAssignmentCollection assignmentCollection) {
        for (WkAssignmentResource assignment : assignmentCollection.getData()) {
            int srsStage = assignment.getData().getSrsStage();
            Integer lastValue = levelData.getOrDefault(srsStage, 0);
            levelData.put(srsStage, lastValue + 1);
        }
    }

    /*@Deprecated
    private void brokenFetch(String apiKey, SrsLevelData levelData) {
        List<Mono<WkCountableCollection>> monos = new ArrayList<>(SRS_SETTER_MAP.size());
        for (Map.Entry<String, BiConsumer<SrsLevelData, Integer>> entry : SRS_SETTER_MAP.entrySet()) {
            String srsLevel = entry.getKey();
            BiConsumer<SrsLevelData, Integer> setter = entry.getValue();
            WebClient.RequestHeadersSpec<?> requestHeadersSpec = getClient().get().uri(uriBuilder -> uriBuilder.path(ASSIGNMENTS_ENDPOINT).queryParam("srs_stages", srsLevel).build());
            prepareHeader(requestHeadersSpec, apiKey);
            LOGGER.trace("Sending request {}", srsLevel);
            Mono<WkCountableCollection> mono = requestHeadersSpec
                    .retrieve()
                    .bodyToMono(WkCountableCollection.class)
                    .doOnSuccess((c) -> {
                        LOGGER.trace("Received result for request {}", srsLevel);
                        setter.accept(levelData, c.getTotalCount());
                    })
                    .doOnError((e) -> {
                        LOGGER.error("Unexpected error while fetching request {}", srsLevel, e);
                    })
                    .timeout(Duration.ofSeconds(5));
            monos.add(mono);
            *//*requestHeadersSpec
                    .retrieve()
                    .bodyToMono(WkCountableCollection.class)
                    .doOnSuccess((c) -> {
                        LOGGER.trace("Received result for request {}", srsLevel);
                        setter.accept(levelData, c.getTotalCount());
                    })
                    .doOnError((e) -> {
                        LOGGER.error("???", e);
                    })
                    .timeout(Duration.ofSeconds(2))
                    .block();*//*
        }

        // Wait for all futures to complete (this is probably not really the best way to implement...)
        Flux.merge(monos).blockLast(Duration.ofSeconds(10));

        LOGGER.trace("Finished?");
    }*/

    private WebClient.RequestHeadersSpec prepareHeader(WebClient.RequestHeadersSpec requestHeadersSpec, String apiKey) {
        return requestHeadersSpec.header("Authorization", "Bearer " + apiKey);
    }

}
