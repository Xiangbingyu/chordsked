package com.chordsked.backend.config.properties;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class InternalAuthLoginPropertiesTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ConfigurationPropertiesAutoConfiguration.class))
            .withUserConfiguration(InternalAuthLoginPropertiesTestConfiguration.class);

    @Test
    void shouldUseDefaultValuesWhenNoPropertyConfigured() {
        contextRunner.run(context -> {
            InternalAuthLoginProperties properties = context.getBean(InternalAuthLoginProperties.class);
            assertEquals(5, properties.getFailLockThreshold());
            assertEquals(30L, properties.getFailLockMinutes());
        });
    }

    @Test
    void shouldBindConfiguredValues() {
        contextRunner
                .withPropertyValues(
                        "chordsked.security.internal-auth.login.fail-lock-threshold=7",
                        "chordsked.security.internal-auth.login.fail-lock-minutes=45"
                )
                .run(context -> {
                    InternalAuthLoginProperties properties = context.getBean(InternalAuthLoginProperties.class);
                    assertEquals(7, properties.getFailLockThreshold());
                    assertEquals(45L, properties.getFailLockMinutes());
                });
    }

    @Test
    void shouldRejectInvalidThreshold() {
        contextRunner
                .withPropertyValues("chordsked.security.internal-auth.login.fail-lock-threshold=0")
                .run(context -> {
                    assertNotNull(context.getStartupFailure());
                });
    }

    @Test
    void shouldRejectInvalidLockMinutes() {
        contextRunner
                .withPropertyValues("chordsked.security.internal-auth.login.fail-lock-minutes=0")
                .run(context -> {
                    assertNotNull(context.getStartupFailure());
                });
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(InternalAuthLoginProperties.class)
    static class InternalAuthLoginPropertiesTestConfiguration {
        @Bean(name = "configurationPropertiesValidator")
        LocalValidatorFactoryBean configurationPropertiesValidator() {
            return new LocalValidatorFactoryBean();
        }
    }
}
