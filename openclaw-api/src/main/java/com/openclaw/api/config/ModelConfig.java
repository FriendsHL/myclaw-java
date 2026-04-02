package com.openclaw.api.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Per-model configuration. Spring Boot automatically maps kebab-case
 * properties (e.g. api-key, base-url, model-name) to camelCase fields.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ModelConfig {

    private String provider;
    private String apiKey;
    private String baseUrl;
    private String modelName;

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }
}
