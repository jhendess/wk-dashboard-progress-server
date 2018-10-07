package org.xlrnet.wk.dashboardprogressserver.config;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xlrnet.wk.dashboardprogressserver.resource.HistoryResource;

/**
 * Configure Jersey resources.
 */
@Component
public class JerseyConfig extends ResourceConfig {

    private CsvMessageBodyWriter messageBodyWriter;

    @Autowired
    public JerseyConfig(CsvMessageBodyWriter messageBodyWriter) {
        this.messageBodyWriter = messageBodyWriter;
        registerEndpoints();
        registerComponents();
    }

    private void registerComponents() {
        register(this.messageBodyWriter);
    }

    private void registerEndpoints() {
        register(HistoryResource.class);
    }
}