package com.medgenome.common.messaging.kafka;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Kafka messaging.
 */
@Data
@ConfigurationProperties(prefix = "common.kafka")
public class KafkaProperties {

    /**
     * Enable/disable Kafka configuration.
     */
    private boolean enabled = false;

    /**
     * Comma-separated list of Kafka bootstrap servers.
     */
    private String bootstrapServers = "localhost:9092";

    /**
     * Default topic name.
     */
    private String defaultTopic = "default-topic";

    /**
     * Number of partitions for the default topic.
     */
    private int defaultTopicPartitions = 3;

    /**
     * Number of replicas for the default topic.
     */
    private int defaultTopicReplicas = 1;

    /**
     * Producer-specific configuration.
     */
    private Producer producer = new Producer();

    /**
     * Consumer-specific configuration.
     */
    private Consumer consumer = new Consumer();

    /**
     * Configuration for Kafka producers.
     */
    @Data
    public static class Producer {
        /**
         * The number of acknowledgments the producer requires.
         */
        private String acks = "all";
        
        /**
         * The number of retries if the producer receives an error.
         */
        private int retries = 3;
        
        /**
         * The producer batch size in bytes.
         */
        private int batchSize = 16384;
        
        /**
         * The time to wait before sending a batch in milliseconds.
         */
        private int lingerMs = 1;
        
        /**
         * The total memory in bytes the producer can use to buffer records.
         */
        private int bufferMemory = 33554432;
    }

    /**
     * Configuration for Kafka consumers.
     */
    @Data
    public static class Consumer {
        /**
         * The consumer group ID.
         */
        private String groupId = "default-group";
        
        /**
         * What to do when there is no initial offset or the offset is out of range.
         */
        private String autoOffsetReset = "latest";
        
        /**
         * Whether to enable auto-commit of offsets.
         */
        private boolean enableAutoCommit = true;
        
        /**
         * The concurrency factor (number of threads) for the listener containers.
         */
        private int concurrency = 3;
        
        /**
         * Whether to enable batch message listening.
         */
        private boolean batchListener = false;
        
        /**
         * The packages trusted for deserialization.
         */
        private String trustedPackages = "*";
    }
}