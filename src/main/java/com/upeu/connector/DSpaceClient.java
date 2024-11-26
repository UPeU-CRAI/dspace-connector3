package com.upeu.connector;

import com.upeu.connector.auth.AuthManager;
import com.upeu.connector.util.HttpUtil;

/**
 * DSpaceClient handles API communication with DSpace-CRIS.
 */
public class DSpaceClient {

    private final DSpaceConfiguration config;
    private final HttpUtil httpUtil;

    public DSpaceClient(DSpaceConfiguration config, AuthManager authManager) {
        this.config = config;
        this.httpUtil = new HttpUtil(authManager, org.apache.hc.client5.http.impl.classic.HttpClients.custom()
                .setDefaultRequestConfig(org.apache.hc.client5.http.config.RequestConfig.custom()
                        .setConnectTimeout(org.apache.hc.core5.util.Timeout.ofMilliseconds(config.getConnectTimeout()))
                        .setResponseTimeout(org.apache.hc.core5.util.Timeout.ofMilliseconds(config.getReadTimeout()))
                        .build())
                .build());
    }

    public String get(String endpoint) throws Exception {
        return httpUtil.get(buildUrl(endpoint));
    }

    public String post(String endpoint, String body) throws Exception {
        return httpUtil.post(buildUrl(endpoint), body);
    }

    public String put(String endpoint, String body) throws Exception {
        return httpUtil.put(buildUrl(endpoint), body);
    }

    public void delete(String endpoint) throws Exception {
        httpUtil.delete(buildUrl(endpoint));
    }

    private String buildUrl(String endpoint) {
        return config.getBaseUrl().endsWith("/") ? config.getBaseUrl() + endpoint : config.getBaseUrl() + "/" + endpoint;
    }
}
