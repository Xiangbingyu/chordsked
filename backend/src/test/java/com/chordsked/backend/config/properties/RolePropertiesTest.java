package com.chordsked.backend.config.properties;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.env.PropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RolePropertiesTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ConfigurationPropertiesAutoConfiguration.class))
            .withUserConfiguration(RolePropertiesTestConfiguration.class);

    @Test
    void shouldUseDefaultValuesWhenNoPropertyConfigured() {
        contextRunner.run(context -> {
            RoleProperties properties = context.getBean(RoleProperties.class);
            assertEquals(List.of("SYSTEM_ADMIN"), properties.getProtectedRoleCodes());
        });
    }

    @Test
    void shouldFallbackToDefaultValuesWhenConfiguredValuesAreBlank() {
        contextRunner
                .withPropertyValues(
                        "chordsked.role.protected-role-codes[0]=   "
                )
                .run(context -> {
                    RoleProperties properties = context.getBean(RoleProperties.class);
                    assertEquals(List.of("SYSTEM_ADMIN"), properties.getProtectedRoleCodes());
                });
    }

    @Test
    void shouldBindConfiguredValuesFromCommonConfig() {
        contextRunner
                .withInitializer(context -> loadCommonApplicationConfig(context.getEnvironment().getPropertySources()))
                .run(context -> {
                    RoleProperties properties = context.getBean(RoleProperties.class);
                    assertEquals(List.of("SYSTEM_ADMIN"), properties.getProtectedRoleCodes());
                });
    }

    @Test
    void shouldBindExplicitRoleConfiguration() {
        contextRunner
                .withPropertyValues(
                        "chordsked.role.protected-role-codes[0]=system_admin",
                        "chordsked.role.protected-role-codes[1]=root_admin"
                )
                .run(context -> {
                    RoleProperties properties = context.getBean(RoleProperties.class);
                    assertEquals(List.of("SYSTEM_ADMIN", "ROOT_ADMIN"), properties.getProtectedRoleCodes());
                });
    }

    private void loadCommonApplicationConfig(org.springframework.core.env.MutablePropertySources propertySources) {
        try {
            Resource resource = new ClassPathResource("common/application-common.yml");
            List<PropertySource<?>> propertySourcesFromYaml = new YamlPropertySourceLoader().load("application-common", resource);
            for (int index = propertySourcesFromYaml.size() - 1; index >= 0; index--) {
                propertySources.addLast(propertySourcesFromYaml.get(index));
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Load common application config failed", exception);
        }
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(RoleProperties.class)
    static class RolePropertiesTestConfiguration {
    }
}
