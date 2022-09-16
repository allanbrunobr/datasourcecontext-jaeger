package com.bruno.jaegerclient.service;

import com.bruno.jaegerclient.thread.LocalThreadTest;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class JaegerClientService {

        private WebClient webClient;

    public JaegerClientService(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<String> get(Integer id) {

        return webClient.get()
                .uri("http://localhost:8082/jaeger/server/" + Integer.valueOf(id))
                .retrieve()
                .bodyToMono(String.class);
    }

}
