package com.alexbt.mock.model;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author alexbt
 */
@Component
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "mock")
public class MockConfig {

    private Map<String, String> urlRewrites = new HashMap<>();

    private String responseLocation;

    public String getResponseLocation() {
        return responseLocation;
    }

    public void setResponseLocation(String responseLocation) {
        Assert.isTrue(new File(responseLocation).isDirectory(), "Location must be a valid directory");
        this.responseLocation = responseLocation;
    }

    public Map<String, String> getUrlRewrites() {
        return urlRewrites;
    }

    public void setUrlRewrites(Map<String, String> urlRewrites) {
        this.urlRewrites = urlRewrites;
    }
}
