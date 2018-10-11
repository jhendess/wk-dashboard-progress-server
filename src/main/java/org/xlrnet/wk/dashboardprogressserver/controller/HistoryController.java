package org.xlrnet.wk.dashboardprogressserver.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.xlrnet.wk.dashboardprogressserver.api.entity.HistoricEntry;
import org.xlrnet.wk.dashboardprogressserver.api.entity.User;
import org.xlrnet.wk.dashboardprogressserver.common.InvalidApiKeyException;
import org.xlrnet.wk.dashboardprogressserver.service.HistoricEntryService;
import org.xlrnet.wk.dashboardprogressserver.service.UserService;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Main controller which should be used for interacting with historical progress data.
 */
@Slf4j
@Service
public class HistoryController {

    /** Service for accessing historic data. */
    private final HistoricEntryService historicEntryService;

    /** Service for accessing user data. */
    private final UserService userService;

    /** Cached map of last update times (avoid overloading WKs servers). */
    private ConcurrentHashMap<String, Instant> lastUpdates = new ConcurrentHashMap<>();

    /** Interval after which updates may be performed. */
    @Value("${wk-history.update-interval-seconds}")
    private int updateIntervalSeconds;

    @Autowired
    public HistoryController(HistoricEntryService historicEntryService, UserService userService) {
        this.historicEntryService = historicEntryService;
        this.userService = userService;
    }

    /**
     * Fetches historical data using the given API key. If the history wasn't updated since the configured time interval, it
     *
     * @param apiKey API key which will be used for fetching.
     * @return Historical data for the user.
     */
    public List<HistoricEntry> fetchHistoryByApiKey(String apiKey) throws InvalidApiKeyException {
        Optional<User> user = userService.findByApiKey(apiKey);
        if (!user.isPresent()) {
            user = userService.updateOrCreateUser(apiKey);
        }

        if (!user.isPresent()) {
            throw new InvalidApiKeyException("Invalid API key: " + apiKey);
        }

        try {
            updateHistoryForUser(user.get());
        } catch (RuntimeException e) {
            LOGGER.error("Refreshing history of user {} failed", user.get().getUserName(), e);
        }
        return historicEntryService.findAllByUserAscending(user.get());
    }

    @Scheduled(cron = "0 0 */6 * * *")
    public void performScheduledRefresh() {
        LOGGER.info("Starting scheduled update for all registered users");
        Iterable<User> allUsers = userService.findAll();
        for (User user : allUsers) {
            updateHistoryForUser(user);
        }
        LOGGER.info("Finished scheduled update");
    }

    private void updateHistoryForUser(User user) {
        Instant lastUpdate = lastUpdates.get(user.getUserName());
        if (lastUpdate == null || lastUpdate.plusSeconds(updateIntervalSeconds).isBefore(Instant.now())) {
            historicEntryService.updateHistoricData(user);
            lastUpdates.put(user.getUserName(), Instant.now());
        } else {
            LOGGER.trace("Not performing any update for user {} because last update was {} seconds ago", user.getUserName(), Instant.now().getEpochSecond() - lastUpdate.getEpochSecond());
        }
    }
}
