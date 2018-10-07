package org.xlrnet.wk.dashboardprogressserver.service;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.xlrnet.wk.dashboardprogressserver.api.entity.HistoricEntry;
import org.xlrnet.wk.dashboardprogressserver.api.entity.User;

import java.util.List;

public interface HistoricEntryRepository extends PagingAndSortingRepository<HistoricEntry, String> {

    List<HistoricEntry> findAllByUserOrderByEpochSecondsDesc(User user);

    List<HistoricEntry> findAllByUserOrderByEpochSecondsDesc(User user, Pageable pageable);

}
