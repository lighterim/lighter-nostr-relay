package com.prosilion.superconductor.util;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class RestClient {
    private final HttpClient httpClient;
    private final String baseUrl;

    public RestClient() {
        this(null, Duration.ofSeconds(30));
    }

    public RestClient(String baseUrl) {
        this(baseUrl, Duration.ofSeconds(30));
    }

    public RestClient(String baseUrl, Duration timeout) {
        this.baseUrl = baseUrl;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(timeout)
                .version(HttpClient.Version.HTTP_2)
                .build();
    }

    // GET请求
    public CompletableFuture<HttpResponse<String>> get(String endpoint) {
        return get(endpoint, null);
    }

    public CompletableFuture<HttpResponse<String>> get(String endpoint, Map<String, String> headers) {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(buildUri(endpoint))
                .GET();

        addHeaders(requestBuilder, headers);
        return sendRequest(requestBuilder.build());
    }

    // POST请求
    public CompletableFuture<HttpResponse<String>> post(String endpoint, String body) {
        return post(endpoint, body, null);
    }

    public CompletableFuture<HttpResponse<String>> post(String endpoint, String body, Map<String, String> headers) {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(buildUri(endpoint))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .header("Content-Type", "application/json");

        addHeaders(requestBuilder, headers);
        return sendRequest(requestBuilder.build());
    }

    // PUT请求
    public CompletableFuture<HttpResponse<String>> put(String endpoint, String body) {
        return put(endpoint, body, null);
    }

    public CompletableFuture<HttpResponse<String>> put(String endpoint, String body, Map<String, String> headers) {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(buildUri(endpoint))
                .PUT(HttpRequest.BodyPublishers.ofString(body))
                .header("Content-Type", "application/json");

        addHeaders(requestBuilder, headers);
        return sendRequest(requestBuilder.build());
    }

    // DELETE请求
    public CompletableFuture<HttpResponse<String>> delete(String endpoint) {
        return delete(endpoint, null);
    }

    public CompletableFuture<HttpResponse<String>> delete(String endpoint, Map<String, String> headers) {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(buildUri(endpoint))
                .DELETE();

        addHeaders(requestBuilder, headers);
        return sendRequest(requestBuilder.build());
    }

    private URI buildUri(String endpoint) {
        if (baseUrl != null && !baseUrl.isEmpty()) {
            return URI.create(baseUrl + endpoint);
        }
        return URI.create(endpoint);
    }

    private void addHeaders(HttpRequest.Builder requestBuilder, Map<String, String> headers) {
        if (headers != null) {
            headers.forEach(requestBuilder::header);
        }
    }

    private CompletableFuture<HttpResponse<String>> sendRequest(HttpRequest request) {
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

}

