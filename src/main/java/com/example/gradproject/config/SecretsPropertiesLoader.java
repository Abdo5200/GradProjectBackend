package com.example.gradproject.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;

public class SecretsPropertiesLoader implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        try {
            File secretsFile = new File("secrets.properties");
            if (secretsFile.exists()) {
                Properties properties = new Properties();
                try (FileInputStream fis = new FileInputStream(secretsFile)) {
                    properties.load(fis);
                }

                PropertiesPropertySource propertySource = new PropertiesPropertySource("secrets", properties);
                environment.getPropertySources().addFirst(propertySource);
            }
        } catch (IOException e) {
            // Silently ignore if secrets.properties doesn't exist
        }
    }
}
