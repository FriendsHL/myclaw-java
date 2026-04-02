package com.openclaw.api.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Gateway server configuration (port, host, auth).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GatewayConfig {

    private int port = 18789;
    private String host = "0.0.0.0";

    @NestedConfigurationProperty
    private AuthConfig auth;

    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }

    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }

    public AuthConfig getAuth() { return auth != null ? auth : new AuthConfig(); }
    public void setAuth(AuthConfig auth) { this.auth = auth; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AuthConfig {
        private String mode = "none";
        private String token;

        public String getMode() { return mode; }
        public void setMode(String mode) { this.mode = mode; }

        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
    }
}
