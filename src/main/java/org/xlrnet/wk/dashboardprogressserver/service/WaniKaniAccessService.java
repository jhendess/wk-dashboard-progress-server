package org.xlrnet.wk.dashboardprogressserver.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;
import org.xlrnet.wk.dashboardprogressserver.api.wk.WkAssignmentCollection;
import org.xlrnet.wk.dashboardprogressserver.api.wk.WkUser;
import org.xlrnet.wk.dashboardprogressserver.api.wk.WkUserResource;
import org.xlrnet.wk.dashboardprogressserver.common.InvalidApiKeyException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
public class WaniKaniAccessService {

    private static final String USER_ENDPOINT = "user";

    private static final String ASSIGNMENTS_ENDPOINT = "assignments";

    private static final String BASE_URL = "https://api.wanikani.com/v2/";

    /** Thread executor which calls the WK API. */
    private ExecutorService executorService;

    /** Jackson reader for the {@link WkAssignmentCollection} objects. */
    private ObjectReader wkAssignmentReader;

    /** Jackson reader for the {@link WkUserResource} objects. */
    private ObjectReader wkUserReader;

    @PostConstruct
    public void initialize() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        wkAssignmentReader = objectMapper.readerFor(WkAssignmentCollection.class);
        wkUserReader = objectMapper.readerFor(WkUserResource.class);
        executorService = new ThreadPoolExecutor(10, 20,
                120, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(),
                Executors.defaultThreadFactory());
    }

    @PreDestroy
    public void shutdown() {
        executorService.shutdown();
    }

    public Optional<WkUser> findUser(String apiKey) throws InvalidApiKeyException {
        LOGGER.debug("Requesting user info for API key {}", apiKey);
        URLConnection urlConnection = null;
        WkUserResource userWkResource = null;
        try {
            URL url = new URL(BASE_URL + USER_ENDPOINT);
            urlConnection = url.openConnection();
            urlConnection.addRequestProperty("Authorization", "Bearer " + apiKey);
            urlConnection.setConnectTimeout(3000);
            LOGGER.trace("Opening connection for {}", BASE_URL + USER_ENDPOINT);
            urlConnection.connect();
            if (((HttpURLConnection) urlConnection).getResponseCode() == 401) {
                throw new InvalidApiKeyException("Invalid API key: " + apiKey);
            }
            InputStream inputStream = urlConnection.getInputStream();
            userWkResource = wkUserReader.readValue(inputStream);
            LOGGER.trace("Received data for {}", BASE_URL + USER_ENDPOINT);
        } catch (IOException e) {
            LOGGER.error("Unexpected error while trying to fetch user", e);
        } finally {
            IOUtils.close(urlConnection);
        }
        return userWkResource == null ? Optional.empty() : Optional.of(userWkResource.getData());
    }

    public Map<Integer, Integer> fetchLatestSrsLevelData(String apiKey) {
        LOGGER.debug("Fetching latest SRS level data for key {}", apiKey);
        Map<Integer, Integer> levelData = new HashMap<>();

        List<AssignmentFetcher> tasks = IntStream.rangeClosed(0, 9)
                .mapToObj(i -> new AssignmentFetcher(levelData, apiKey, i)).collect(Collectors.toList());
        try {
            executorService.invokeAll(tasks);
        } catch (InterruptedException e) {
            throw new RuntimeException("Unexpected error while fetching assigments", e);
        }

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
            LOGGER.trace("Opening connection for {}", nextUrl);
            urlConnection.connect();
            InputStream inputStream = urlConnection.getInputStream();
            assignmentCollection = wkAssignmentReader.readValue(inputStream);
            LOGGER.trace("Received data for {}", nextUrl);
        } catch (IOException e) {
            LOGGER.error("Unexpected error while trying to fetch assignments", e);
            throw new RuntimeException(e);
        } finally {
            IOUtils.close(urlConnection);
        }
        return assignmentCollection;
    }

    @Data
    private class AssignmentFetcher implements Callable<Void> {
        private final Map<Integer, Integer> targetMap;
        private final String apiKey;
        private final Integer stage;

        @Override
        public Void call() throws Exception {
            try {
                WkAssignmentCollection assignments = fetchAssignments(apiKey, BASE_URL + ASSIGNMENTS_ENDPOINT + "?srs_stages=" + stage);
                targetMap.put(stage, assignments.getTotalCount());
            } catch (RuntimeException e) {
                targetMap.put(stage, -1);
            }
            return null;
        }
    }

}
