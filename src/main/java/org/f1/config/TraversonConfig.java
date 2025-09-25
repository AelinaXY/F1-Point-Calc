package org.f1.config;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.HttpRequestInterceptor;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import uk.co.autotrader.traverson.Traverson;
import uk.co.autotrader.traverson.http.ApacheHttpTraversonClientAdapter;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
public class TraversonConfig {

    @Primary
    @Bean
    public Traverson getTraverson(HttpClientBuilder httpClientBuilder) {
        return new Traverson(new ApacheHttpTraversonClientAdapter(httpClientBuilder.build()));
    }

    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public HttpClientBuilder defaultHttpClient(List<HttpRequestInterceptor> interceptors) {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setDefaultMaxPerRoute(20);
        cm.setMaxTotal(60);

        RequestConfig requestConfig = org.apache.hc.client5.http.config.RequestConfig.custom()
                .setConnectTimeout(Timeout.ofSeconds(10))
                .setProtocolUpgradeEnabled(false)
                .setDefaultKeepAlive(20, TimeUnit.SECONDS)
                .setResponseTimeout(Timeout.ofSeconds(60))
                .build();

        HttpClientBuilder clientBuilder = HttpClientBuilder.create();
        for (HttpRequestInterceptor interceptor : interceptors) {
            clientBuilder.addRequestInterceptorFirst(interceptor);
        }
        return clientBuilder.setDefaultRequestConfig(requestConfig)
                .setConnectionManager(cm);
    }
}
