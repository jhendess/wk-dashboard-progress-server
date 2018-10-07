package org.xlrnet.wk.dashboardprogressserver.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xlrnet.wk.dashboardprogressserver.api.entity.User;
import org.xlrnet.wk.dashboardprogressserver.api.wk.WkUser;
import org.xlrnet.wk.dashboardprogressserver.common.AbstractTransactionalService;

import java.util.Optional;

@Slf4j
@Service
public class UserService extends AbstractTransactionalService<User, UserRepository> {

    /** Service for accessing the WK backend. */
    private WaniKaniAccessService waniKaniAccessService;

    /**
     * Constructor for abstract transactional service. Needs always a crud repository for performing operations.
     *
     * @param crudRepository
     *         The crud repository for providing basic crud operations.
     * @param waniKaniAccessService
     */
    @Autowired
    public UserService(UserRepository crudRepository, WaniKaniAccessService waniKaniAccessService) {
        super(crudRepository);
        this.waniKaniAccessService = waniKaniAccessService;
    }

    public Optional<User> findByApiKey(String apiKey) {
        return getRepository().findByApiKey(apiKey);
    }

    public Optional<User> updateOrCreateUser(String apiKey) {
        WkUser user = waniKaniAccessService.findUser(apiKey);
        String name = user.getUsername();
        Optional<User> persistedUser = getRepository().findByUserName(name);

        if (persistedUser.isPresent()) {
            LOGGER.debug("Updating API key for user {}", name);
            persistedUser.get().setApiKey(apiKey);
            return Optional.of(save(persistedUser.get()));
        } else {
            LOGGER.debug("Creating new persistent user {}", name);
            User newUser = new User();
            newUser.setApiKey(apiKey);
            newUser.setUserName(name);
            return Optional.of(save(newUser));
        }
    }
}
