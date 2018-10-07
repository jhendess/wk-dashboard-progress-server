package org.xlrnet.wk.dashboardprogressserver.api.wk;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PageInfo {

    @JsonProperty("next_url")
    private String nextUrl;

    @JsonProperty("previous_url")
    private String previousUrl;

    @JsonProperty("per_page")
    private String perPage;

}
