package com.bruno.jaegerserver.service;

import com.bruno.jaegerserver.localthread.LocalThreadTest;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class JaegerServerService {

    private WebClient webClient;

    public JaegerServerService(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<String> get(Integer id) {
      return webClient.get()
                .uri("http://numberapi.com/" + id)
                .retrieve()
                .bodyToMono(String.class);
    }

}
