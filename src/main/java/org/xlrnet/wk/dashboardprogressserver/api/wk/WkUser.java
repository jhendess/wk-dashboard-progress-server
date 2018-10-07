package org.xlrnet.wk.dashboardprogressserver.api.wk;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WkUser {

    @JsonProperty("username")
    private String username;

    @JsonProperty("level")
    private Integer level;

    @JsonProperty("max_level_granted_by_subscription")
    private Integer maxLevelGranted;

    @JsonProperty("started_at")
    private Date startedAt;

    @JsonProperty("subscribed")
    private boolean subscribed;

}
