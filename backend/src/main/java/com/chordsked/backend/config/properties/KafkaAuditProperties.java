package com.chordsked.backend.config.properties;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component("kafkaAuditProperties")
@Validated
@ConfigurationProperties(prefix = "chordsked.kafka.audit")
public class KafkaAuditProperties {
    private boolean enabled = false;

    @NotBlank
    private String topic = "chordsked.audit.log.v1";

    @NotBlank
    private String groupId = "chordsked-audit-log-group";

    @Min(1)
    private int concurrency = 1;

    @Min(0)
    private long backoffMillis = 2000L;

    @Min(0)
    private long maxRetries = 3L;

    @NotBlank
    private String dltSuffix = ".DLT";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public int getConcurrency() {
        return concurrency;
    }

    public void setConcurrency(int concurrency) {
        this.concurrency = concurrency;
    }

    public long getBackoffMillis() {
        return backoffMillis;
    }

    public void setBackoffMillis(long backoffMillis) {
        this.backoffMillis = backoffMillis;
    }

    public long getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(long maxRetries) {
        this.maxRetries = maxRetries;
    }

    public String getDltSuffix() {
        return dltSuffix;
    }

    public void setDltSuffix(String dltSuffix) {
        this.dltSuffix = dltSuffix;
    }
}
