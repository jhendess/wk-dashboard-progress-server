package org.xlrnet.wk.dashboardprogressserver.api.wk;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class WkCountableCollection<T> extends AbstractWkCollection<T> {

    @JsonProperty("total_count")
    private int totalCount;

}
