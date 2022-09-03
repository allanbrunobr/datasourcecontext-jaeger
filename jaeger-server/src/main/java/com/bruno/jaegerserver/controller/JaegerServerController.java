package com.bruno.jaegerserver.controller;

import com.bruno.jaegerserver.service.JaegerServerService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/jaeger/server")
@AllArgsConstructor
public class JaegerServerController {

    private final JaegerServerService jaegerServerService;

    @GetMapping("/{id}")
    public Mono<String> get(@PathVariable("id") Integer id){

        return jaegerServerService.get(id);

    }
}
