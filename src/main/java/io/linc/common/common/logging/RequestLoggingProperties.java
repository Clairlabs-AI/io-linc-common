package io.linc.common.common.logging;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for HTTP request and response logging.
 */
@Data
@ConfigurationProperties(prefix = "common.logging.request")
public class RequestLoggingProperties {

    /**
     * Enable/disable request logging.
     */
    private boolean enabled = true;

    /**
     * Whether to include the query string in the logged message.
     */
    private boolean includeQueryString = true;

    /**
     * Whether to include the request payload (body) in the logged message.
     */
    private boolean includePayload = true;

    /**
     * Maximum length of the payload to be included in the logged message.
     */
    private int maxPayloadLength = 10000;

    /**
     * Whether to include the client information (typically remote address and session id) in the logged message.
     */
    private boolean includeClientInfo = true;

    /**
     * Whether to include HTTP headers in the logged message.
     */
    private boolean includeHeaders = false;

    /**
     * The prefix to use before the request message.
     */
    private String beforeMessagePrefix = "REQUEST [";

    /**
     * The suffix to use after the request message.
     */
    private String beforeMessageSuffix = "]";

    /**
     * The prefix to use before the response message.
     */
    private String afterMessagePrefix = "RESPONSE [";

    /**
     * The suffix to use after the response message.
     */
    private String afterMessageSuffix = "]";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isIncludeQueryString() {
        return includeQueryString;
    }

    public void setIncludeQueryString(boolean includeQueryString) {
        this.includeQueryString = includeQueryString;
    }

    public boolean isIncludePayload() {
        return includePayload;
    }

    public void setIncludePayload(boolean includePayload) {
        this.includePayload = includePayload;
    }

    public int getMaxPayloadLength() {
        return maxPayloadLength;
    }

    public void setMaxPayloadLength(int maxPayloadLength) {
        this.maxPayloadLength = maxPayloadLength;
    }

    public boolean isIncludeClientInfo() {
        return includeClientInfo;
    }

    public void setIncludeClientInfo(boolean includeClientInfo) {
        this.includeClientInfo = includeClientInfo;
    }

    public boolean isIncludeHeaders() {
        return includeHeaders;
    }

    public void setIncludeHeaders(boolean includeHeaders) {
        this.includeHeaders = includeHeaders;
    }

    public String getBeforeMessagePrefix() {
        return beforeMessagePrefix;
    }

    public void setBeforeMessagePrefix(String beforeMessagePrefix) {
        this.beforeMessagePrefix = beforeMessagePrefix;
    }

    public String getBeforeMessageSuffix() {
        return beforeMessageSuffix;
    }

    public void setBeforeMessageSuffix(String beforeMessageSuffix) {
        this.beforeMessageSuffix = beforeMessageSuffix;
    }

    public String getAfterMessagePrefix() {
        return afterMessagePrefix;
    }

    public void setAfterMessagePrefix(String afterMessagePrefix) {
        this.afterMessagePrefix = afterMessagePrefix;
    }

    public String getAfterMessageSuffix() {
        return afterMessageSuffix;
    }

    public void setAfterMessageSuffix(String afterMessageSuffix) {
        this.afterMessageSuffix = afterMessageSuffix;
    }
}