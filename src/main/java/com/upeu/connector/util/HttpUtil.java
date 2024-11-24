package com.upeu.connector.util;

import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Utility class for handling HTTP requests.
 */
public class HttpUtil {

    private static final Logger logger = LoggerFactory.getLogger(HttpUtil.class);

    /**
     * Executes an HTTP GET request.
     *
     * @param httpClient The HTTP client instance.
     * @param url The target URL.
     * @return The response body as a string.
     * @throws Exception if an error occurs during the request.
     */
    public static String get(CloseableHttpClient httpClient, String url) throws Exception {
        HttpGet httpGet = new HttpGet(url);
        return executeRequest(httpClient, httpGet);
    }

    /**
     * Executes an HTTP POST request.
     *
     * @param httpClient The HTTP client instance.
     * @param url The target URL.
     * @param payload The payload to send as JSON.
     * @return The response body as a string.
     * @throws Exception if an error occurs during the request.
     */
    public static String post(CloseableHttpClient httpClient, String url, String payload) throws Exception {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(new StringEntity(payload, StandardCharsets.UTF_8));
        httpPost.setHeader("Content-Type", "application/json");
        return executeRequest(httpClient, httpPost);
    }

    /**
     * Executes an HTTP PUT request.
     *
     * @param httpClient The HTTP client instance.
     * @param url The target URL.
     * @param payload The payload to send as JSON.
     * @return The response body as a string.
     * @throws Exception if an error occurs during the request.
     */
    public static String put(CloseableHttpClient httpClient, String url, String payload) throws Exception {
        HttpPut httpPut = new HttpPut(url);
        httpPut.setEntity(new StringEntity(payload, StandardCharsets.UTF_8));
        httpPut.setHeader("Content-Type", "application/json");
        return executeRequest(httpClient, httpPut);
    }

    /**
     * Executes an HTTP DELETE request.
     *
     * @param httpClient The HTTP client instance.
     * @param url The target URL.
     * @return The response body as a string.
     * @throws Exception if an error occurs during the request.
     */
    public static String delete(CloseableHttpClient httpClient, String url) throws Exception {
        HttpDelete httpDelete = new HttpDelete(url);
        return executeRequest(httpClient, httpDelete);
    }

    /**
     * Executes the given HTTP request and returns the response body as a string.
     *
     * @param httpClient The HTTP client instance.
     * @param request The HTTP request to execute.
     * @return The response body as a string.
     * @throws Exception if an error occurs during the request.
     */
    private static String executeRequest(CloseableHttpClient httpClient, HttpUriRequestBase request) throws Exception {
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(entity.getContent(), StandardCharsets.UTF_8))) {
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    return result.toString();
                }
            }
        } catch (Exception e) {
            logger.error("HTTP request failed: {}", e.getMessage(), e);
            throw e;
        }
        return null;
    }
}
