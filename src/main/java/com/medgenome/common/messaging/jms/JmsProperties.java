package com.medgenome.common.messaging.jms;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Configuration properties for JMS messaging.
 */
@Data
@ConfigurationProperties(prefix = "common.jms")
public class JmsProperties {

    /**
     * Enable/disable JMS configuration.
     */
    private boolean enabled = false;

    /**
     * JMS broker URL.
     */
    private String brokerUrl = "tcp://localhost:61616";

    /**
     * JMS broker username.
     */
    private String username = "";

    /**
     * JMS broker password.
     */
    private String password = "";

    /**
     * Default queue name.
     */
    private String defaultQueue = "default.queue";

    /**
     * Whether to use pub/sub domain (topics instead of queues).
     */
    private boolean pubSubDomain = false;

    /**
     * Whether messages should be persistent.
     */
    private boolean deliveryPersistent = true;

    /**
     * Whether to explicitly set QoS values.
     */
    private boolean explicitQosEnabled = false;

    /**
     * Message time to live in milliseconds.
     */
    private long timeToLive = 0; // never expire

    /**
     * Message priority (0-9).
     */
    private int priority = 4; // default priority

    /**
     * Concurrency setting for JMS listeners (e.g., "3-5").
     */
    private String listenerConcurrency = "1-3";

    /**
     * Whether JMS sessions should be transacted.
     */
    private boolean sessionTransacted = true;

    /**
     * The packages trusted for deserialization.
     */
    private List<String> trustedPackages = List.of("com.medgenome");
}