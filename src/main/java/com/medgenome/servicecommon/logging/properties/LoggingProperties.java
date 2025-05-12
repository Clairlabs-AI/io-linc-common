package com.medgenome.servicecommon.logging.properties;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "logging")
public class LoggingProperties {

    private final Audit audit = new Audit();
    private final Performance performance = new Performance();
    private final Security security = new Security();



    @Setter
    @Getter
    public static class Audit {
        /**
         * Enable or disable audit logging.
         */
        private boolean enabled = true;

    }

    @Setter
    @Getter
    public static class Performance {
        /**
         * Enable or disable performance logging.
         */
        private boolean enabled = true;

    }

    @Setter
    @Getter
    public static class Security {
        /**
         * Enable or disable security logging.
         */
        private boolean enabled = true;

    }
}
