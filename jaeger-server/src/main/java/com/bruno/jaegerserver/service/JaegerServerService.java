package com.bruno.jaegerserver.service;

import com.bruno.jaegerserver.localthread.LocalThreadTest;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class JaegerServerService {

    private WebClient webClient;

//    private final RestTemplate restTemplate = new RestTemplate();

    public JaegerServerService(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<String> get(Integer id) {
//        String uri = "http://numberapi.com/"+id;
//        return restTemplate.getForObject(uri, String.class);
      return webClient.get()
                .uri("http://numberapi.com/" + id)
                .retrieve()
                .bodyToMono(String.class);
    }

}
