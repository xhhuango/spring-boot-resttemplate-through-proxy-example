package com.xhhuango;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

@Component
@Slf4j
public class PollingTask {
    static class Proxy {
        String ip;
        int port;

        Proxy(String ip, int port) {
            this.ip = ip;
            this.port = port;
        }
    }

    static class AuthProxy extends Proxy {
        String username;
        String password;

        AuthProxy(String ip, int port, String username, String password) {
            super(ip, port);
            this.username = username;
            this.password = password;
        }
    }

    private static final Proxy[] PROXIES = new Proxy[]{
            new AuthProxy("139.199.187.113", 23128, "zrghgfuhd3", "vo0z6enusg"),
    };

    private int count = 0;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    @Scheduled(fixedDelay = 60 * 60 * 1000)
    public void poll() {
        log.info("Count " + count++);

        Arrays.stream(PROXIES).forEach(proxy -> {
            try {
                RestTemplate restTemplate = proxy instanceof AuthProxy
                        ? createRestTemplateWithAuth((AuthProxy) proxy)
                        : createRestTemplate(proxy);
                log.info("[" + getTime() + "] Polling (proxy " + proxy.ip + ":" + proxy.port + ") -> ");
                long from = System.currentTimeMillis();
                String response = restTemplate.getForObject("http://ip.cip.cc/", String.class);
                long to = System.currentTimeMillis();
                log.info(response + " (response time " + (to - from) + ")");
            } catch (Exception e) {
                log.info("FAILED");
            }
        });
    }

    private String getTime() {
        return simpleDateFormat.format(new Date());
    }

    private RestTemplate createRestTemplate(Proxy proxy) {
        SimpleClientHttpRequestFactory simpleClientHttpRequestFactory = new SimpleClientHttpRequestFactory();
        simpleClientHttpRequestFactory.setProxy(new java.net.Proxy(java.net.Proxy.Type.HTTP, new InetSocketAddress(proxy.ip, proxy.port)));
        return new RestTemplate(simpleClientHttpRequestFactory);
    }

    private RestTemplate createRestTemplateWithAuth(AuthProxy proxy) {
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(
                new AuthScope(proxy.ip, proxy.port),
                new UsernamePasswordCredentials(proxy.username, proxy.password)
        );

        HttpHost httpHost = new HttpHost(proxy.ip, proxy.port);
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();

        httpClientBuilder.setProxy(httpHost).setDefaultCredentialsProvider(credentialsProvider).disableCookieManagement();

        HttpClient httpClient = httpClientBuilder.build();
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setHttpClient(httpClient);

        return new RestTemplate(factory);
    }
}
