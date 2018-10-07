package org.xlrnet.wk.dashboardprogressserver.api.wk;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class AbstractWkCollection<T> {

    @JsonProperty("object")
    private String object;

    @JsonProperty("url")
    private String url;

    @JsonProperty("pages")
    private PageInfo pageInfo;

    @JsonProperty("total_count")
    private int totalCount;

    @JsonProperty("data_updated_at")
    private Instant updatedAt;

    @JsonProperty("data")
    private List<T> data;

}
