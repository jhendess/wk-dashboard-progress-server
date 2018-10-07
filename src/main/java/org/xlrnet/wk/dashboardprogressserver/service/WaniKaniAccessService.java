package org.xlrnet.wk.dashboardprogressserver.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.xlrnet.wk.dashboardprogressserver.api.wk.WkAssignmentCollection;
import org.xlrnet.wk.dashboardprogressserver.api.wk.WkAssignmentResource;
import org.xlrnet.wk.dashboardprogressserver.api.wk.WkUser;
import org.xlrnet.wk.dashboardprogressserver.api.wk.WkUserResource;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class WaniKaniAccessService {

    private static final String USER_ENDPOINT = "user";

    private static final String ASSIGNMENTS_ENDPOINT = "assignments";

    private static final Duration DEFAULT_WAIT_TIMEOUT = Duration.ofSeconds(5);

    @Getter
    private WebClient client;

    @PostConstruct
    public void initialize() {
        client = WebClient.create("https://api.wanikani.com/v2/");
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

        WebClient.RequestHeadersSpec<?> headersSpec = getClient().get().uri(ASSIGNMENTS_ENDPOINT);
        prepareHeader(headersSpec, apiKey);
        WkAssignmentCollection assignmentCollection = headersSpec.retrieve().bodyToMono(WkAssignmentCollection.class).block(Duration.ofSeconds(3));

        updateLevelData(levelData, assignmentCollection);
        if (assignmentCollection.getPageInfo() != null && assignmentCollection.getPageInfo().getNextUrl() != null) {
            String nextUrl = assignmentCollection.getPageInfo().getNextUrl();
            do {
                LOGGER.debug("Fetching more available data for key {}", apiKey);

                headersSpec = getClient().get().uri(nextUrl);
                prepareHeader(headersSpec, apiKey);
                assignmentCollection = headersSpec.retrieve().bodyToMono(WkAssignmentCollection.class).block(DEFAULT_WAIT_TIMEOUT);
                updateLevelData(levelData, assignmentCollection);

                nextUrl = assignmentCollection.getPageInfo() != null ? assignmentCollection.getPageInfo().getNextUrl() : null;
            } while (nextUrl != null);

        }

        return levelData;
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
