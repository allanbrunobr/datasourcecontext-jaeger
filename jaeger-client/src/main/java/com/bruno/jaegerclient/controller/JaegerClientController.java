package com.bruno.jaegerclient.controller;

import com.bruno.jaegerclient.localthread.LocalThreadTest;
import com.bruno.jaegerclient.service.JaegerClientService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/jaeger/client")
@AllArgsConstructor
public class JaegerClientController {

    private final JaegerClientService jaegerClientService;

    @GetMapping("/{id}")
    public Mono<String> get(@PathVariable("id") Integer id){
        LocalThreadTest.setValue(1);


        return jaegerClientService.get(id);

    }
}
