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

class AuthLoginPropertiesTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ConfigurationPropertiesAutoConfiguration.class))
            .withUserConfiguration(AuthLoginPropertiesTestConfiguration.class);

    @Test
    void shouldUseDefaultValuesWhenNoPropertyConfigured() {
        contextRunner.run(context -> {
            AuthLoginProperties properties = context.getBean(AuthLoginProperties.class);
            assertEquals(5, properties.getFailLockThreshold());
            assertEquals(30L, properties.getFailLockMinutes());
        });
    }

    @Test
    void shouldBindConfiguredValues() {
        contextRunner
                .withPropertyValues(
                        "chordsked.security.auth.login.fail-lock-threshold=7",
                        "chordsked.security.auth.login.fail-lock-minutes=45"
                )
                .run(context -> {
                    AuthLoginProperties properties = context.getBean(AuthLoginProperties.class);
                    assertEquals(7, properties.getFailLockThreshold());
                    assertEquals(45L, properties.getFailLockMinutes());
                });
    }

    @Test
    void shouldRejectInvalidThreshold() {
        contextRunner
                .withPropertyValues("chordsked.security.auth.login.fail-lock-threshold=0")
                .run(context -> {
                    assertNotNull(context.getStartupFailure());
                });
    }

    @Test
    void shouldRejectInvalidLockMinutes() {
        contextRunner
                .withPropertyValues("chordsked.security.auth.login.fail-lock-minutes=0")
                .run(context -> {
                    assertNotNull(context.getStartupFailure());
                });
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(AuthLoginProperties.class)
    static class AuthLoginPropertiesTestConfiguration {
        @Bean(name = "configurationPropertiesValidator")
        LocalValidatorFactoryBean configurationPropertiesValidator() {
            return new LocalValidatorFactoryBean();
        }
    }
}
