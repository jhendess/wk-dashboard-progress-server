package org.xlrnet.wk.dashboardprogressserver.service;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.xlrnet.wk.dashboardprogressserver.api.entity.User;

import java.util.Optional;

public interface UserRepository extends PagingAndSortingRepository<User, String> {

    Optional<User> findByApiKey(String apiKey);

    Optional<User> findByUserName(String userNme);
}
