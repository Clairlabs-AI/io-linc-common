package com.medgenome.common.messaging.jms;

import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

/**
 * Configuration for JMS messaging.
 * Sets up connection factories, templates, and listener containers.
 */
@Slf4j
@Configuration
@EnableJms
@ConditionalOnClass(ActiveMQConnectionFactory.class)
@EnableConfigurationProperties(JmsProperties.class)
@ConditionalOnProperty(prefix = "common.jms", name = "enabled", havingValue = "true")
public class JmsConfiguration {

    private final JmsProperties jmsProperties;

    public JmsConfiguration(JmsProperties jmsProperties) {
        this.jmsProperties = jmsProperties;
        log.info("JMS configuration initialized with broker URL: {}", 
                jmsProperties.getBrokerUrl());
    }

    @Bean
    public ActiveMQConnectionFactory connectionFactory() {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
        connectionFactory.setBrokerURL(jmsProperties.getBrokerUrl());
        connectionFactory.setUserName(jmsProperties.getUsername());
        connectionFactory.setPassword(jmsProperties.getPassword());
        connectionFactory.setTrustedPackages(jmsProperties.getTrustedPackages());
        
        log.info("Created JMS connection factory with broker URL: {}", jmsProperties.getBrokerUrl());
        return connectionFactory;
    }

    @Bean
    public MessageConverter jacksonJmsMessageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        return converter;
    }

    @Bean
    public JmsTemplate jmsTemplate() {
        JmsTemplate template = new JmsTemplate(connectionFactory());
        template.setMessageConverter(jacksonJmsMessageConverter());
        template.setDefaultDestinationName(jmsProperties.getDefaultQueue());
        template.setPubSubDomain(jmsProperties.isPubSubDomain());
        template.setDeliveryPersistent(jmsProperties.isDeliveryPersistent());
        template.setExplicitQosEnabled(jmsProperties.isExplicitQosEnabled());
        
        if (jmsProperties.isExplicitQosEnabled()) {
            template.setTimeToLive(jmsProperties.getTimeToLive());
            template.setPriority(jmsProperties.getPriority());
        }
        
        log.info("Created JMS template with default queue: {}", jmsProperties.getDefaultQueue());
        return template;
    }

    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory() {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory());
        factory.setMessageConverter(jacksonJmsMessageConverter());
        factory.setConcurrency(jmsProperties.getListenerConcurrency());
        factory.setPubSubDomain(jmsProperties.isPubSubDomain());
        
        // Error handling
        factory.setSessionTransacted(jmsProperties.isSessionTransacted());
        factory.setErrorHandler(throwable -> {
            log.error("Error processing JMS message", throwable);
        });
        
        log.info("Created JMS listener container factory with concurrency: {}", 
                jmsProperties.getListenerConcurrency());
        return factory;
    }
}