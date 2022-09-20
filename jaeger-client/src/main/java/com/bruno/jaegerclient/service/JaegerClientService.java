package com.bruno.jaegerclient.service;

import com.bruno.jaegerclient.thread.LocalThreadTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class JaegerClientService {

       // private WebClient webClient;
        private final RestTemplate restTemplate = new RestTemplate();

//    public JaegerClientService(WebClient webClient) {
//        this.webClient = webClient;
//    }

    public String get(Integer id) {
        String uri = "http://localhost:8082/jaeger/server/" + Integer.valueOf(id);
        return restTemplate.getForObject(uri, String.class);

//            webClient.get()
//                .uri("http://localhost:8082/jaeger/server/" + Integer.valueOf(id))
//                .retrieve()
//                .bodyToMono(String.class);
    }

}
