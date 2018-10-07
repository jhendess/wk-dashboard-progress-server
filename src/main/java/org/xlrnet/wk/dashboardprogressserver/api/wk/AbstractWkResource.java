package org.xlrnet.wk.dashboardprogressserver.api.wk;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.Instant;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class AbstractWkResource<T> {

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("object")
    private String object;

    @JsonProperty("url")
    private String url;

    @JsonProperty("data_updated_at")
    private Instant updatedAt;

    @JsonProperty("data")
    private T data;

}
