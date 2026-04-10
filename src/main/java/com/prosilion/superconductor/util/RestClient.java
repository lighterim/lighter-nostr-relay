package com.prosilion.superconductor.util;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.*;

@Component
public class RestClient {
    private HttpClient httpClient;

    @Value("${rest.client.base-url:#{null}}")
    private String baseUrl;

    @Value("${rest.client.connect-timeout:30}")
    private int connectTimeout;

    @Value("${rest.client.max-pool-size:20}")
    private int maxPoolSize;

    @Value("${rest.client.keep-alive-time:60}")
    private int keepAliveTime;

    private ExecutorService executorService;

    @PostConstruct
    public void init() {
        // 创建线程池
        executorService = new ThreadPoolExecutor(
                0,
                maxPoolSize,
                keepAliveTime,
                TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

        // 创建HttpClient实例
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(connectTimeout))
                .executor(executorService)
                .version(HttpClient.Version.HTTP_2)
                .build();
    }

    @PreDestroy
    public void destroy() {
        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    // GET请求
    public CompletableFuture<HttpResponse<String>> get(String endpoint) {
        return get(baseUrl, endpoint, null);
    }

    public CompletableFuture<HttpResponse<String>> get(String base, String endpoint) {
        return get(base, endpoint, null);
    }

    public CompletableFuture<HttpResponse<String>> get(String base, String endpoint, Map<String, String> headers) {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(buildUri(base, endpoint))
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
                .timeout(Duration.ofSeconds(60))
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

    private URI buildUri(String base, String endpoint) {
        if (base != null && !base.isEmpty()) {
            return URI.create(base + endpoint);
        }
        return URI.create(endpoint);
    }

    private URI buildUri(String endpoint) {
        return buildUri(baseUrl, endpoint);
    }

    private void addHeaders(HttpRequest.Builder requestBuilder, Map<String, String> headers) {
        if (headers != null) {
            headers.forEach(requestBuilder::header);
        }
    }

    private CompletableFuture<HttpResponse<String>> sendRequest(HttpRequest request) {
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    // 添加同步请求方法（可选）
    public HttpResponse<String> getSync(String endpoint) throws Exception {
        return get(endpoint).get();
    }

    public HttpResponse<String> getSync(String endpoint, Map<String, String> headers) throws Exception {
        return get(baseUrl, endpoint, headers).get();
    }

    public HttpResponse<String> postSync(String endpoint, String body) throws Exception {
        return post(endpoint, body).get();
    }

    public HttpResponse<String> postSync(String endpoint, String body, Map<String, String> headers) throws Exception {
        return post(endpoint, body, headers).get();
    }
}

