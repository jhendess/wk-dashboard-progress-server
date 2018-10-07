package org.xlrnet.wk.dashboardprogressserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class DashboardProgressServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DashboardProgressServerApplication.class, args);
    }
}
