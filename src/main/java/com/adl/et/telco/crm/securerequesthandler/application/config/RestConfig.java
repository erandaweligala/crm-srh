package com.adl.et.telco.crm.securerequesthandler.application.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.pool.PoolConcurrencyPolicy;
import org.apache.hc.core5.pool.PoolReusePolicy;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * Configuration class for REST template setup.
 * Configures HTTP client factory and REST template with proper logging and connection pool management.
 */
@Slf4j
@Configuration
@EnableScheduling
public class RestConfig {
    private static final String LOG_PREFIX = "SRH|RestConfig|";

    @Value("${rest-template.read-time-out}")
    private Integer readTimeout;

    @Value("${rest-template.connection-time-out}")
    private Integer connectionTimeout;

    @Value("${tenant-id}")
    private String tenantId;

    // Connection Pool Configuration
    @Value("${rest-template.pool.max-total:200}")
    private Integer maxTotalConnections;

    @Value("${rest-template.pool.max-per-route:50}")
    private Integer maxConnectionsPerRoute;

    @Value("${rest-template.pool.validate-after-inactivity:2000}")
    private Integer validateAfterInactivity;

    @Value("${rest-template.pool.evict-idle-connections:30}")
    private Integer evictIdleConnectionsSeconds;

    @Value("${rest-template.pool.connection-time-to-live:60}")
    private Integer connectionTimeToLiveSeconds;

    @Value("${rest-template.pool.socket-timeout:60000}")
    private Integer socketTimeout;

    // Store connection manager for monitoring
    private PoolingHttpClientConnectionManager connectionManager;

    /**
     * Creates and configures a RestTemplate instance.
     * @return Configured RestTemplate instance
     */
    @Bean
    public RestTemplate restTemplate() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        log.debug("{}restTemplate|Start|ReadTimeout: {}|ConnectionTimeout: {}", 
            LOG_PREFIX, readTimeout, connectionTimeout);
        
        RestTemplate restTemplate = new RestTemplate(simpleClientHttpRequestFactory());
        restTemplate.getMessageConverters().add(new StringHttpMessageConverter(StandardCharsets.UTF_8));
        
        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
        interceptors.add((request, body, execution) -> {
            log.debug("{}restTemplate|Interceptor|Adding tenant header: {}", 
                LOG_PREFIX, tenantId);
            if (!request.getHeaders().containsKey("tenantId")) {
                request.getHeaders().set("tenantId", tenantId);
            }
            return execution.execute(request, body);
        });
        
        restTemplate.setInterceptors(interceptors);
        log.info("{}restTemplate|End|Template created with {} interceptors", 
            LOG_PREFIX, interceptors.size());
        return restTemplate;
    }

    /**
     * Creates and configures an HTTP client request factory with robust connection pool management.
     * @return Configured HttpComponentsClientHttpRequestFactory instance
     */
    @Bean
    @ConfigurationProperties(prefix = "custom.rest.connection")
    public HttpComponentsClientHttpRequestFactory simpleClientHttpRequestFactory() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        log.info("{}Initializing HTTP Client Factory", LOG_PREFIX);
        log.info("{}Connection Pool Config - MaxTotal: {}, MaxPerRoute: {}, ValidateAfterInactivity: {}ms, TTL: {}s",
            LOG_PREFIX, maxTotalConnections, maxConnectionsPerRoute, validateAfterInactivity, connectionTimeToLiveSeconds);

        // Create RequestConfig with timeouts
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofMilliseconds(connectionTimeout))
                .setResponseTimeout(Timeout.ofMilliseconds(readTimeout))
                .setConnectionRequestTimeout(Timeout.ofMilliseconds(5000)) // Timeout for getting connection from pool
                .build();

        // Create SSL context that trusts all certs (for testing only!)
        SSLContext sslContext = SSLContextBuilder.create()
                .loadTrustMaterial(TrustAllStrategy.INSTANCE)
                .build();

        SSLConnectionSocketFactory sslSocketFactory = SSLConnectionSocketFactoryBuilder.create()
                .setSslContext(sslContext)
                .setHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                .build();

        // Create ConnectionConfig for connection-level settings
        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setValidateAfterInactivity(TimeValue.ofMilliseconds(validateAfterInactivity)) // Validate stale connections
                .setSocketTimeout(Timeout.ofMilliseconds(socketTimeout))
                .setTimeToLive(TimeValue.ofSeconds(connectionTimeToLiveSeconds)) // Max connection lifetime
                .build();

        // Create SocketConfig for socket-level settings
        SocketConfig socketConfig = SocketConfig.custom()
                .setSoTimeout(Timeout.ofMilliseconds(socketTimeout))
                .setSoKeepAlive(true) // Enable TCP keep-alive
                .setTcpNoDelay(true) // Disable Nagle's algorithm for better performance
                .build();

        // Create Connection Manager with comprehensive configuration
        connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setSSLSocketFactory(sslSocketFactory)
                .setDefaultConnectionConfig(connectionConfig)
                .setDefaultSocketConfig(socketConfig)
                .setPoolConcurrencyPolicy(PoolConcurrencyPolicy.STRICT) // Thread-safe pool access
                .setConnPoolPolicy(PoolReusePolicy.LIFO) // Reuse most recently used connections
                .setMaxConnTotal(maxTotalConnections) // Maximum total connections across all routes
                .setMaxConnPerRoute(maxConnectionsPerRoute) // Maximum connections per route
                .build();

        log.info("{}Connection Manager created with stats - Available: {}, Leased: {}, Pending: {}, Max: {}",
            LOG_PREFIX,
            connectionManager.getTotalStats().getAvailable(),
            connectionManager.getTotalStats().getLeased(),
            connectionManager.getTotalStats().getPending(),
            connectionManager.getTotalStats().getMax());

        // Create HTTP Client with connection validation and eviction
        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(config)
                .evictIdleConnections(TimeValue.ofSeconds(evictIdleConnectionsSeconds)) // Evict idle connections
                .evictExpiredConnections() // Evict expired connections
                .setConnectionManagerShared(false) // This client owns the connection manager
                // Add retry handler for idempotent requests
                .setRetryStrategy(new org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy(
                    3, // Max retries
                    TimeValue.ofMilliseconds(1000) // Retry interval
                ))
                .build();

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);

        log.info("{}HTTP Client Factory initialized successfully", LOG_PREFIX);
        return factory;
    }

    /**
     * Scheduled task to monitor and log connection pool statistics.
     * Runs every 60 seconds to help detect connection leaks and pool exhaustion.
     */
    @Scheduled(fixedDelay = 60000, initialDelay = 60000)
    public void monitorConnectionPool() {
        if (connectionManager != null) {
            try {
                var stats = connectionManager.getTotalStats();
                log.info("{}ConnectionPool|Stats - Available: {}, Leased: {}, Pending: {}, Max: {}",
                    LOG_PREFIX,
                    stats.getAvailable(),
                    stats.getLeased(),
                    stats.getPending(),
                    stats.getMax());

                // Alert if pool is near exhaustion
                int utilizationPercent = (stats.getLeased() * 100) / stats.getMax();
                if (utilizationPercent > 80) {
                    log.warn("{}ConnectionPool|HighUtilization - {}% of connections in use ({}/{})",
                        LOG_PREFIX, utilizationPercent, stats.getLeased(), stats.getMax());
                }

                // Alert if connections are pending
                if (stats.getPending() > 0) {
                    log.warn("{}ConnectionPool|PendingConnections - {} threads waiting for connections",
                        LOG_PREFIX, stats.getPending());
                }
            } catch (Exception e) {
                log.error("{}ConnectionPool|MonitoringError - {}", LOG_PREFIX, e.getMessage());
            }
        }
    }
}
