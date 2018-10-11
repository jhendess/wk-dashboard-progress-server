package org.xlrnet.wk.dashboardprogressserver.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.xlrnet.wk.dashboardprogressserver.api.entity.HistoricEntry;
import org.xlrnet.wk.dashboardprogressserver.api.entity.User;
import org.xlrnet.wk.dashboardprogressserver.common.AbstractTransactionalService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;


@Slf4j
@Service
public class HistoricEntryService extends AbstractTransactionalService<HistoricEntry, HistoricEntryRepository> {

    private final Map<Integer, BiConsumer<HistoricEntry, Integer>> SRS_SETTER_MAP = new HashMap<>();

    {
        SRS_SETTER_MAP.put(0, HistoricEntry::setInitiate);
        SRS_SETTER_MAP.put(1, HistoricEntry::setApprentice1);
        SRS_SETTER_MAP.put(2, HistoricEntry::setApprentice2);
        SRS_SETTER_MAP.put(3, HistoricEntry::setApprentice3);
        SRS_SETTER_MAP.put(4, HistoricEntry::setApprentice4);
        SRS_SETTER_MAP.put(5, HistoricEntry::setGuru1);
        SRS_SETTER_MAP.put(6, HistoricEntry::setGuru2);
        SRS_SETTER_MAP.put(7, HistoricEntry::setMaster);
        SRS_SETTER_MAP.put(8, HistoricEntry::setEnlightened);
        SRS_SETTER_MAP.put(9, HistoricEntry::setBurned);
    }

    /** Service for accessing the WK backend. */
    private WaniKaniAccessService waniKaniAccessService;

    /**
     * Constructor for abstract transactional service. Needs always a crud repository for performing operations.
     *
     * @param crudRepository
     *         The crud repository for providing basic crud operations.
     * @param waniKaniAccessService
     *         Service for accessing the WaniKani backend.
     */
    @Autowired
    public HistoricEntryService(HistoricEntryRepository crudRepository, WaniKaniAccessService waniKaniAccessService) {
        super(crudRepository);
        this.waniKaniAccessService = waniKaniAccessService;
    }

    public List<HistoricEntry> findAllByUserAscending(User user) {
        return getRepository().findAllByUserOrderByEpochSecondsAsc(user);
    }

    public void updateHistoricData(User user) {
        Map<Integer, Integer> srsLevelData = waniKaniAccessService.fetchLatestSrsLevelData(user.getApiKey());
        HistoricEntry newEntry = new HistoricEntry();
        boolean requestFailed = false;
        for (Map.Entry<Integer, BiConsumer<HistoricEntry, Integer>> integerBiConsumerEntry : SRS_SETTER_MAP.entrySet()) {
            Integer srsLevel = integerBiConsumerEntry.getKey();
            BiConsumer<HistoricEntry, Integer> consumer = integerBiConsumerEntry.getValue();
            Integer i = srsLevelData.get(srsLevel);
            if (i != null) {
                if (i < 0) {
                    requestFailed = true;
                } else {
                    consumer.accept(newEntry, i);
                }
            }
        }
        HistoricEntry lastEntry = findLastByUser(user);

        if (!requestFailed  && (lastEntry == null || isEntryDifferent(lastEntry, newEntry))) {
            newEntry.setUser(user);
            newEntry.setEpochSeconds(System.currentTimeMillis() / 1000);
            save(newEntry);
            LOGGER.debug("Updated historic data for user {}", user.getUserName());
        } else if (requestFailed) {
            LOGGER.warn("Request for user {} failed", user.getUserName());
        } else {
            LOGGER.debug("No updates for user {}", user.getUserName());
        }
    }

    private boolean isEntryDifferent(HistoricEntry lastEntry, HistoricEntry newEntry) {
        boolean same = lastEntry.getApprentice1() == newEntry.getApprentice1();
        same &= lastEntry.getApprentice2() == newEntry.getApprentice2();
        same &= lastEntry.getApprentice3() == newEntry.getApprentice3();
        same &= lastEntry.getApprentice4() == newEntry.getApprentice4();
        same &= lastEntry.getGuru1() == newEntry.getGuru1();
        same &= lastEntry.getGuru2() == newEntry.getGuru2();
        same &= lastEntry.getMaster() == newEntry.getMaster();
        same &= lastEntry.getEnlightened() == newEntry.getEnlightened();
        same &= lastEntry.getBurned() == newEntry.getBurned();
        return !same;
    }

    private HistoricEntry findLastByUser(User user) {
        List<HistoricEntry> entries = getRepository().findAllByUserOrderByEpochSecondsDesc(user, PageRequest.of(0, 1));
        return !entries.isEmpty() ? entries.get(0) : null;
    }
}
