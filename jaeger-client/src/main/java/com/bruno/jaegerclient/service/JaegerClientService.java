package com.bruno.jaegerclient.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class JaegerClientService {

        private WebClient webClient;
//        private final RestTemplate restTemplate = new RestTemplate();

    public JaegerClientService(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<String> get(Integer id) {
//        String uri = "http://localhost:8082/jaeger/server/" + Integer.valueOf(id);
//        return restTemplate.getForObject(uri, String.class);

      return webClient.get()
                .uri("http://localhost:8082/jaeger/server/" + id)
                .retrieve()
                .bodyToMono(String.class);
    }

}
