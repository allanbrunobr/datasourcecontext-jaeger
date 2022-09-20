package com.bruno.jaegerclient.controller;

import static org.springframework.web.servlet.function.RouterFunctions.route;

import com.bruno.jaegerclient.record.Customer;
import com.bruno.jaegerclient.service.JaegerClientService;
import com.bruno.jaegerclient.thread.LocalThreadTest;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/jaeger/client")
@AllArgsConstructor
public class JaegerClientController {

    private final JaegerClientService jaegerClientService;

    @GetMapping("/{id}")
    public Mono<String> get(@PathVariable("id") Integer id){
//    public String get(@PathVariable("id") Integer id){
            return jaegerClientService.get(id);

    }
 @Bean
    RouterFunction<ServerResponse> routes (JdbcTemplate template){
        return route()
            .GET("/customers",  request -> {
                var results = template.query("select * from customer",
                    (rs, rowNum) -> new Customer(rs.getInt("id"), rs.getString("name")));
                return ServerResponse.ok().body(results);
            })
            .build();
    }

}
