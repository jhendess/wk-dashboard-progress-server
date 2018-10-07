package org.xlrnet.wk.dashboardprogressserver.api.entity;

import lombok.Data;
import org.xlrnet.wk.dashboardprogressserver.common.AbstractEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.time.Instant;

@Data
@Entity
@Table(name = "user")
public class User extends AbstractEntity {

    @NotNull
    @Column(name = "api_key")
    private String apiKey;

    @NotNull
    @Column(name = "user_name")
    private String userName;

    @Column(name = "last_request")
    private Instant lastRequest;

}
